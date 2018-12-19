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
public class Octree {

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

    /** the depth of this tree **/
    protected int depth = 1;

    /**
     * the max depth of this tree,
     * theoretically it shall be less than 21, because every three bits in a Long-type
     * octree-node index is used to locate its position of siblings.
     **/
    private static final int MAX_DEPTH = 10;

    public void buildIndex(List<Point3d> points, int depth) {
        if (depth < 1) {
            throw new IllegalArgumentException("Depth should be larger than 0.");
        }
        this.depth = depth;

        if (points.size() < 1) {
            System.err.println("Warning: input for buildIndex() is an empty list.");
            return;
        }

        this.octreeIndices.clear();

        this.points = points;

        determineRootNode();
        createOctree(1, this.root);

        for (int i = 0; i < points.size(); i ++) {
            Point3d xyz = points.get(i);
            this.root.addPoint(xyz, i);
        }

    }

    /**
     * build spatial index for point cloud
     * note that the length double array in List points must be 3
     * @param points the point cloud
     */
    public void buildIndex(List<Point3d> points) {
        int depth = (int) (Math.ceil((Math.log((points.size() + 1) / 64) / Math.log(8))) + 1);
        depth = min(depth, MAX_DEPTH);
        depth = max(depth, 1);

        this.buildIndex(points, depth);
    }

    /**
     * determine bounding box of data points, expand the box to make it cubic
     */
    private void determineRootNode() {
        Box bbox = BoundingBox.of(points);
        double maxExtent = PcuCommonUtil.max(bbox.getxExtent(), bbox.getyExtent(), bbox.getzExtent());
        this.root = new OctreeNode(bbox.getCenter(), maxExtent);
    }

    /**
     * partition the space recursively
     * @param currentDepth  the depth of current octree node
     * @param currentNode current octree node
     */
    private void createOctree(int currentDepth, OctreeNode currentNode) {
        if (currentDepth == this.depth) {
            currentNode.indices = new ArrayList<>();
            if (currentNode == this.root) this.octreeIndices.put(this.root.index, this.root);
            return;
        }
        currentNode.children = new OctreeNode[8];
        int cnt = 0;
        for (int i : new int[]{-1, 1}) {
            for (int j : new int[]{-1, 1}) {
                for (int k : new int[]{-1, 1}) {
                    long index = (long) (((i + 1) * 2 + (j + 1) * 1 + (k + 1) / 2));
//                    index <<= (currentDepth - 1) * 3;
                    index |= (currentNode.index << 3);

                    double length = currentNode.getxExtent(); // xExtent == yExtent == zExtent
                    Point3d center = new Point3d(currentNode.getCenter().x + i * length / 2, currentNode.getCenter().y + j * length / 2, currentNode.getCenter().z + k * length / 2);
                    OctreeNode node = new OctreeNode(center, length / 2);
                    currentNode.children[cnt] = node;
                    node.index = index;
//                    node.minX = currentNode.minX + (i + 1) / 2 * currentNode.xExtent;
//                    node.maxX = currentNode.maxX + (i - 1) / 2 * currentNode.xExtent;
//                    node.minY = currentNode.minY + (j + 1) / 2 * currentNode.yExtent;
//                    node.maxY = currentNode.maxY + (j - 1) / 2 * currentNode.yExtent;
//                    node.minZ = currentNode.minZ + (k + 1) / 2 * currentNode.zExtent;
//                    node.maxZ = currentNode.maxZ + (k - 1) / 2 * currentNode.zExtent;
                    if (currentDepth + 1 == this.depth) this.octreeIndices.put(index, node);
                    createOctree(currentDepth + 1, node);
                    cnt += 1;
                }
            }

        }
    }

    /**
     * If you want to acquire the k-nearest neighbors of a certain point p, call this function,
     * octree will decrease the time cost
     * @param k the number of nearest neighbors
     * @param index the index of point p
     * @return the indices of nearest neighbors
     */
    public int[] searchNearestNeighbors(int k, int index) {
        if (points == null) throw new IllegalStateException("Octree.buildIndex() must be called before searchNearestNeighbors.");
        if (k >= this.points.size()) throw new IllegalArgumentException("number of nearest neighbors is larger than data size");
        return searchNearestNeighbors(k, points.get(index));
    }

    public int[] searchNearestNeighbors(int k, final Point3d point) {
        long leafNode = locateOctreeNode(this.root, point);
        List<Long> adjacentLeaves = obtainAdjacent26Indices(leafNode);
        List<Long> candidateLeaves = new ArrayList<>();
//        System.out.println("adjacent leaves: " + adjacentLeaves.size());
        candidateLeaves.add(leafNode);
        candidateLeaves.addAll(adjacentLeaves);

        adjacentOctreeNodesIndices(candidateLeaves, k);
//        System.out.println("candidate leaves: " + candidateLeaves.size());
        PriorityQueue<Integer> queue = searchNeighborsInNodes(candidateLeaves, point);

        int[] indices = new int[k];
        for (int i = 0; i < k; i ++) {
            indices[i] = queue.poll();
        }
        return indices;
    }

    private PriorityQueue<Integer> searchNeighborsInNodes(List<Long> candidateLeaves, final Point3d point) {
        int capacity = 0;
        for (long leafIndex : candidateLeaves) capacity += this.octreeIndices.get(leafIndex).indices.size();
        PriorityQueue<Integer> queue = new PriorityQueue<>(capacity, new Comparator<Integer>() {
            @Override
            public int compare(Integer pointIndex1, Integer pointIndex2) {
                Point3d p1 = points.get(pointIndex1);
                Point3d p2 = points.get(pointIndex2);
                return Double.compare(p1.distance(point), p2.distance(point));
            }
        });
        for (long leafIndex : candidateLeaves) {
            queue.addAll(this.octreeIndices.get(leafIndex).indices);
        }
        return queue;
    }

    public List<Integer> searchNeighborsInSphere(Point3d point, double radius) {
        List<Integer> neighborIndices = new ArrayList<>();
        List<Long> candidateLeaves = new ArrayList<>();
        helpDetermineCandidatesWithRadius(radius, point, candidateLeaves);

        PriorityQueue<Integer> queue = searchNeighborsInNodes(candidateLeaves, point);

        while (queue.size() > 0) {
            Integer nextIndex = queue.poll();
            Point3d neighboringPoint = points.get(nextIndex);
            if (point.distance(neighboringPoint) >= radius) {
                break;
            } else {
                neighborIndices.add(nextIndex);
            }
        }
        return neighborIndices;

    }

    /**
     * @param index the index of a point
     * @param radius radius of neighborhood
     * @return indices of neighboring points of this point
     */
    public List<Integer> searchNeighborsInSphere(int index, double radius) {
        return searchNeighborsInSphere(points.get(index), radius);
    }

    private void helpDetermineCandidatesWithRadius(double radius, Point3d point, List<Long> candidates) {
        Sphere sphere = new Sphere(point, radius);
        long coreNodeIndex = locateOctreeNode(root, point);
        candidates.add(coreNodeIndex);

        Box coreBox = octreeIndices.get(coreNodeIndex);
        if (Collisions.contains(coreBox, sphere)) {
            return;
        }
        // ===========================================
        Set<Long> visited = new HashSet<>();
        visited.add(coreNodeIndex);

        for (int pointer = 0; pointer < candidates.size(); pointer ++) {
            long octreeNodeIndex = candidates.get(pointer);
            Vector<Long> adjacentNodes = obtainAdjacent26Indices(octreeNodeIndex);
            for (Long childIndex : adjacentNodes) {
                OctreeNode octreeNodeBox = octreeIndices.get(childIndex);
                if (visited.contains(childIndex)) continue;
                if (Collisions.intersect(octreeNodeBox, sphere)) {
                    candidates.add(childIndex);
                    visited.add(childIndex);
                }
            }
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

    /**
     * when searching k nearest neighbors of one point in point cloud, the point
     * may be `closed to` the boundary of the octree leaf node, so we need to search
     * adjacent nodes as well.
     *
     *
     * @param leafIndices index of leaf node in which the point is located
     * @param k number of nearest neighbors
     */
    private void adjacentOctreeNodesIndices(List<Long> leafIndices, int k) {
        int num = 0;
        for (long leafIndex : leafIndices) {
            num += this.octreeIndices.get(leafIndex).indices.size();
        }
//        System.out.println("num: " + num);
        if (num > k) return;
        Set<Long> indicesSet = new HashSet<>(leafIndices);
        for (Long leafIndex : leafIndices) {
            List<Long> adjacent26 = obtainAdjacent26Indices(leafIndex);
//            System.out.println("adjacent: " + adjacent26.size());
            indicesSet.addAll(adjacent26);
        }
        leafIndices.clear();
        leafIndices.addAll(indicesSet);
        adjacentOctreeNodesIndices(leafIndices, k);
    }

    /**
     *
     * interpreter the index to three-dimensional Cartesian coordinates
     * high bits   ----   ---- low bits
     * root index  ----   ---- leaf index
     * eg. if index is (101 011 001 101)
     * the coord is [(1001), (0100), (1111)]
     * @param index index of an octree node
     * @return  3-d coordinate of octree node
     **/
    protected int[] index2Coordinates(Long index) {
        int []coord = new int[3];//x y z
        coord[0] = 0; coord[1] = 0; coord[2] = 0;
        /** from left to right **/
        for (int i = depth; i > 1; i --) {
            Long temp = (index >> (i - 2) * 3) & 7L;
            coord[0] += ((temp >> 2) & 1L) << (i - 2);
            coord[1] += ((temp >> 1) & 1L) << (i - 2);
            coord[2] += ((temp >> 0) & 1L) << (i - 2);
        }
        return coord;
    }

    /**
     *
     * root index  ----   ---- leaf index
     * if index is (101 011 001 101)_2
     * the coord is [(1001), (0100), (1111)]
     * @param coord 3-d coordinate
     * @return index of an octree node
     **/
    protected Long coordinates2Index(int []coord) {
        Long index = 0L;
        for (int i = this.depth; i > 1; i --) {
            index |= (((coord[0] >> (i - 2)) & 1) << 2);
            index |= (((coord[1] >> (i - 2)) & 1) << 1);
            index |= (((coord[2] >> (i - 2)) & 1) << 0);
            index <<= 3;
        }
        index >>= 3;
        return index;
    }

    protected Vector<Long> obtainAdjacent26Indices(Long index) {
        Vector<Long> result = new Vector<>();
        int []coord = index2Coordinates(index);
        for (int i : new int[] {-1, 0, 1}) {
            for (int j : new int[] {-1, 0, 1}) {
                for (int k : new int[] {-1, 0, 1}) {
                    if (i == 0 && j == 0 && k == 0) continue;
                    if (coord[0] == 0 && i < 0) continue;
                    if (coord[1] == 0 && j < 0) continue;
                    if (coord[2] == 0 && k < 0) continue;
                    if (coord[0] == Math.pow(2, this.depth - 1) - 1 && i > 0) continue;
                    if (coord[1] == Math.pow(2, this.depth - 1) - 1 && j > 0) continue;
                    if (coord[2] == Math.pow(2, this.depth - 1) - 1 && k > 0) continue;

                    int []newCoord = new int [] {coord[0] + i, coord[1] + j, coord[2] + k};
                    if (isValidCoordinates(newCoord))
                        result.add(coordinates2Index(newCoord));
                }
            }
        }

        return result;
    }

    private boolean isValidCoordinates(int []coord) {
        if (coord.length < 3) return false;
        return coord[0] >= 0 && coord[0] < (int) pow(2, this.depth - 1)
                && coord[1] >= 0 && coord[1] < (int) pow(2, this.depth - 1)
                && coord[2] >= 0 && coord[2] < (int) pow(2, this.depth - 1);
    }

    public int getDepth() {
        return depth;
    }

    public class OctreeNode extends Box{

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

        OctreeNode(Point3d center, double length) {
            this.center = new Point3d(center);
            this.xExtent = length;
            this.yExtent = length;
            this.zExtent = length;
        }

        void addPoint(Point3d point, int index) {
            if (children == null) {
                indices.add(index);
            } else {
                int xi = point.x < center.x ? 0 : 1;
                int yj = point.y < center.y ? 0 : 1;
                int zk = point.z < center.z ? 0 : 1;
                int childIndex = xi * 4 + yj * 2 + zk * 1;
                children[childIndex].addPoint(point, index);
            }
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

    }

}
