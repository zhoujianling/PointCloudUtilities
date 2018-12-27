package cn.jimmiez.pcu.common.graphics;


import cn.jimmiez.pcu.common.graphics.shape.Box;
import cn.jimmiez.pcu.common.graphics.shape.Sphere;
import cn.jimmiez.pcu.util.PcuCommonUtil;

import javax.vecmath.Point3d;
import java.util.*;

import static java.lang.Math.*;


/**
 * An octree can be used to build spatial index for point cloud.
 */
@SuppressWarnings("Duplicates")
public class Octree2 {

    /**
     * the root node of the octree,
     * the size of root node is the bounding box of point cloud
     **/
    protected OctreeNode root = null;

    protected List<Point3d> points = null;

    /**
     * used to find an octree node by its index
     * the index of an octree node is generated according to its spatial position
     */
    protected Map<Long, OctreeNode> octreeIndices = new HashMap<>();

    /**
     * the max depth of this tree,
     * theoretically it shall be less than 21, because every three bits in a Long-type
     * octree-node index is used to locate its position of siblings.
     **/
    private static final int MAX_DEPTH = 10;

    private static final int MAX_POINTS_PER_NODE = 63;

    /**
     * build spatial index for point cloud
     * note that the length double array in List points must be 3
     * @param points the point cloud
     */
    public void buildIndex(List<Point3d> points) {
        if (points.size() < 1) {
            System.err.println("Warning: input for buildIndex() is an empty list.");
            return;
        }

        this.octreeIndices.clear();
        this.points = points;

        determineRootNode();
        createOctree(0, this.root);

    }

    /**
     * determine bounding box of data points, expand the box to make it cubic
     */
    private void determineRootNode() {
        Box bbox = BoundingBox.of(points);
        double maxExtent = PcuCommonUtil.max(bbox.getxExtent(), bbox.getyExtent(), bbox.getzExtent());
        this.root = new OctreeNode(bbox.getCenter(), maxExtent, 0);
        this.root.indices.addAll(PcuCommonUtil.incrementalIntegerList(points.size()));
    }

    /**
     * partition the space recursively
     * @param currentDepth  the depth of current octree node
     * @param currentNode current octree node
     */
    private void createOctree(int currentDepth, OctreeNode currentNode) {
        if (currentNode.indices.size() < 1) return;
        if (currentNode.indices.size() <= MAX_POINTS_PER_NODE || currentDepth >= MAX_DEPTH) {
            this.octreeIndices.put(currentNode.index, currentNode);
            return;
        }
        currentNode.children = new OctreeNode[8];
        int cnt = 0;
        for (int i : new int[]{-1, 1}) {
            for (int j : new int[]{-1, 1}) {
                for (int k : new int[]{-1, 1}) {
                    long index = (long) (((i + 1) * 2 + (j + 1) * 1 + (k + 1) / 2));
//                    index <<= (currentDepth - 1) * 3;
                    index = currentNode.index | (index << (3 * currentDepth + 3));
                    double length = currentNode.getxExtent(); // xExtent == yExtent == zExtent
                    Point3d center = new Point3d(currentNode.getCenter().x + i * length / 2, currentNode.getCenter().y + j * length / 2, currentNode.getCenter().z + k * length / 2);
                    OctreeNode node = new OctreeNode(center, length / 2, currentDepth + 1);
                    currentNode.children[cnt] = node;
                    node.index = index;
                    cnt += 1;
                }
            }
        }
        for (int index : currentNode.indices) {
            Point3d point = points.get(index);
            Point3d center = currentNode.getCenter();
            int xi = point.x < center.x ? 0 : 1;
            int yj = point.y < center.y ? 0 : 1;
            int zk = point.z < center.z ? 0 : 1;
            int childIndex = xi * 4 + yj * 2 + zk * 1;
            currentNode.children[childIndex].indices.add(index);
        }
        for (OctreeNode node : currentNode.children) {
            createOctree(currentDepth + 1, node);
        }
    }


    /**
     * find the index of octree node in which the target point is located
     * @param node the root octree node
     * @param point the target point
     * @return the index of leaf node
     */
    protected Long locateOctreeNode(OctreeNode node, Point3d point) {
        if (node.children == null) {
            if (node.contains(point)) {
                return node.index;
            } else {
                throw new IllegalStateException("Search a point exceeding octree bounds.");
            }
        } else {
            int xi = point.x < node.getCenter().x ? 0 : 1;
            int yj = point.y < node.getCenter().y ? 0 : 1;
            int zk = point.z < node.getCenter().z ? 0 : 1;
            int childIndex = xi * 4 + yj * 2 + zk * 1;
            return locateOctreeNode(node.children[childIndex], point);
        }
    }




    public class OctreeNode extends Box {

        /**
         * default value is root index
         * the index is generated in createOctree()
         **/
        Long index = 0L;

        /**
         * an octree node holds the indices of 3d points in the List
         * in a non-leaf node, field indices is null
         **/
        List<Integer> indices = null;

        /** in a non-leaf node, field indices is null **/
        OctreeNode[] children = null;

        int depth = 0;

        OctreeNode(Point3d center, double length, int depth) {
            this.center = new Point3d(center);
            this.xExtent = length;
            this.yExtent = length;
            this.zExtent = length;
            this.depth = depth;
        }

        boolean contains(Point3d point) {
            for (int index : indices) {
                if (points.get(index) == point) return true;
            }
            return abs(point.x - center.x) <= xExtent + 1e-4  &&
                    abs(point.y - center.y) <= yExtent + 1e-4 &&
                    abs(point.z - center.z) <= zExtent + 1e-4;
        }

        boolean contains(Point3d point, double tolerance) {
            return abs(abs(point.x - center.x) - xExtent) <= tolerance &&
                    abs(abs(point.y - center.y) - yExtent) <= tolerance &&
                    abs(abs(point.z - center.z) - zExtent) <= tolerance;
        }

        public List<Integer> getIndices() {
            return indices;
        }

        public OctreeNode[] getChildren() {
            return children;
        }

        public int getDepth() {
            return depth;
        }
    }

}

