package cn.jimmiez.pcu.common;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static cn.jimmiez.pcu.util.PcuCommonUtil.*;

public class Octree {

    private OctreeNode root = null;
    private List<double[]> points = null;
    private Map<Long, OctreeNode> octreeIndices = new HashMap<>();
    private int depth = 1;
    private static final int MAX_DEPTH = 10;

    /**
     * build space index for point cloud
     * @param points the point cloud
     * note that the length double array in List points must be 3
     */
    public void buildIndex(List<double[]> points) {
        this.depth = (int) (Math.ceil((Math.log((points.size() + 1) / 64) / Math.log(8))) + 1);
        this.depth = min(this.depth, MAX_DEPTH);
        this.octreeIndices.clear();
        this.depth = this.depth > 0 ? this.depth : 1;
        this.points = points;
        this.root = new OctreeNode();
        if (points.size() < 1) {
            System.err.println("Warning: input for buildIndex() is empty list.");
            return;
        }
        this.root.minX = points.get(0)[0];
        this.root.maxX = points.get(0)[0];
        this.root.minY = points.get(0)[1];
        this.root.maxY = points.get(0)[1];
        this.root.minZ = points.get(0)[2];
        this.root.maxZ = points.get(0)[2];
        for (double[] xyz: points) {
            this.root.minX = min(xyz[0], this.root.minX);
            this.root.maxX = max(xyz[0], this.root.maxX);
            this.root.minY = min(xyz[1], this.root.minY);
            this.root.maxY = max(xyz[1], this.root.maxY);
            this.root.minZ = min(xyz[2], this.root.minZ);
            this.root.maxZ = max(xyz[2], this.root.maxZ);
        }

        createOctree(1, this.root);

        for (int i = 0; i < points.size(); i ++) {
            double[] xyz = points.get(i);
            this.root.addPoint(xyz, i);
        }
    }

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
                    index <<= (currentDepth - 1) * 3;
                    index |= currentNode.index;
                    OctreeNode node = new OctreeNode();
                    currentNode.children[cnt] = node;
                    node.index = index;
                    node.minX = currentNode.minX + (i + 1) / 2 * (currentNode.maxX - currentNode.minX) / 2;
                    node.maxX = currentNode.maxX + (i - 1) / 2 * (currentNode.maxX - currentNode.minX) / 2;
                    node.minY = currentNode.minY + (i + 1) / 2 * (currentNode.maxY - currentNode.minY) / 2;
                    node.maxY = currentNode.maxY + (i - 1) / 2 * (currentNode.maxY - currentNode.minY) / 2;
                    node.minZ = currentNode.minZ + (i + 1) / 2 * (currentNode.maxZ - currentNode.minZ) / 2;
                    node.maxZ = currentNode.maxZ + (i - 1) / 2 * (currentNode.maxZ - currentNode.minZ) / 2;
                    this.octreeIndices.put(index, node);
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
        if (points == null) {
            throw new IllegalStateException("Octree.buildIndex() must be called before searchNearestNeighbors.");
        }
        int[] indices = new int[k];
        double[] point = points.get(index);

        return indices;
    }

    public class OctreeNode {
        Long index = 0L; // default value is root index
        List<Integer> indices = null;
        OctreeNode[] children = null;
        double minX = Double.NaN;
        double maxX = Double.NaN;
        double minY = Double.NaN;
        double maxY = Double.NaN;
        double minZ = Double.NaN;
        double maxZ = Double.NaN;

        void addPoint(double[] point, int index) {
            if (children == null) {
                if (contains(point)) {
                    indices.add(index);
                }
            } else {
                for (OctreeNode node : children) {
                    node.addPoint(point, index);
                }
            }
        }

        boolean contains(double[] point) {
            return point[0] >= minX && point[0] <= maxX &&
                    point[1] >= minY && point[1] <= minY &&
                    point[2] >= minZ && point[2] <= maxZ;
        }

        public List<Integer> getIndices() {
            return indices;
        }
    }

    public int getDepth() {
        return depth;
    }

    public Map<Long, OctreeNode> getOctreeIndices() {
        return octreeIndices;
    }
}
