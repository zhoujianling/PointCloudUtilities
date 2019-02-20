package cn.jimmiez.pcu.alg.skeleton.gr;

import cn.jimmiez.pcu.common.graphics.BoundingBox;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.util.VectorUtil;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

import static java.lang.StrictMath.pow;

public class GraphReductionOctree extends Octree {

    /** the minimal acceptable size of octree cells **/
    private double minimalSize = - 1.0;

    /** the intersection threshold **/
    private double touchThreshold = - 1.0;

    /**
     * The constructor of OctreeVoxelizer.
     * automatically compute the parameter according to the point cloud
     */
    public GraphReductionOctree() {
    }

    /**
     * The constructor of OctreeVoxelizer.
     * Two parameter can be specified to control the process of recursive
     * division.
     * @param ma the minimal acceptable size of octree cells
     * @param it the threshold to determine if the point cloud is intersecting a cell inside
     */
    public GraphReductionOctree(double ma, double it) {
        this.minimalSize = ma;
        this.touchThreshold = it;
    }

    /**
     * infer parameter from point cloud information.
     * Note that this process might be time-consuming.
     */
    private void inferParameter(List<Point3d> pointCloud) {
        if (minimalSize > 0 || touchThreshold > 0) return;
        if (pointCloud == null) throw new NullPointerException("point cloud is null");
        if (pointCloud.size() < 1) throw new IllegalArgumentException("The point cloud contains no points!");
        BoundingBox box = BoundingBox.of(pointCloud);
        minimalSize = box.diagonalLength() / pow(pointCloud.size(), .333333) / 5.0;
        touchThreshold = minimalSize / 3.0;
    }

    /**
     *
     * @param data the point cloud
     * @return the list of octree cells
     */
    public List<OctreeNode> voxelize(List<Point3d> data) {
        inferParameter(data);
        super.buildIndex(data);
        List<OctreeNode> leaves = new ArrayList<>();
        for (Long index : super.octreeIndices.keySet()) {
            leaves.add(super.octreeIndices.get(index));
        }
        return leaves;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void createOctree(int currentDepth, OctreeNode currentNode) {
        if (currentNode.getIndices().size() < 1) return;
        if (currentDepth >= MAX_DEPTH
                || currentNode.getIndices().size() <= getMaxPointsPerNode()
                || currentNode.getxExtent() * 2 <= minimalSize  // condition 3
                ) {
            this.octreeIndices.put(currentNode.getIndex(), currentNode);
            return;
        }
        currentNode.setChildren(new OctreeNode[8]);
        int cnt = 0;
        for (int i : new int[]{-1, 1}) {
            for (int j : new int[]{-1, 1}) {
                for (int k : new int[]{-1, 1}) {
                    long index = (long) (((i + 1) * 2 + (j + 1) * 1 + (k + 1) / 2));
                    index = currentNode.getIndex() | (index << (3 * currentDepth + 3));
                    double length = currentNode.getxExtent(); // xExtent == yExtent == zExtent
                    Point3d center = new Point3d(currentNode.getCenter().x + i * length / 2, currentNode.getCenter().y + j * length / 2, currentNode.getCenter().z + k * length / 2);
                    OctreeNode node = new OctreeNode(center, length / 2, currentDepth + 1);
                    currentNode.getChildren()[cnt] = node;
                    node.setIndex(index);
                    node.setIndices(new ArrayList<Integer>(currentNode.getIndices().size() / 8 + 10));
                    cnt += 1;
                }
            }
        }
        for (int index : currentNode.getIndices()) {
            Point3d point = points.get(index);
            if (! VectorUtil.validPoint(point)) continue;
            Point3d center = currentNode.getCenter();
            int xi = point.x < center.x ? 0 : 1;
            int yj = point.y < center.y ? 0 : 1;
            int zk = point.z < center.z ? 0 : 1;
            int childIndex = xi * 4 + yj * 2 + zk * 1;
            currentNode.getChildren()[childIndex].getIndices().add(index);
        }
        currentNode.getIndices().clear();
        for (OctreeNode node : currentNode.getChildren()) {
            createOctree(currentDepth + 1, node);
        }

    }

    public double getMinimalSize() {
        return minimalSize;
    }

    public void setMinimalSize(double ma) {
        this.minimalSize = ma;
    }

    public double getTouchThreshold() {
        return touchThreshold;
    }

    public void setTouchThreshold(double it) {
        this.touchThreshold = it;
    }

}
