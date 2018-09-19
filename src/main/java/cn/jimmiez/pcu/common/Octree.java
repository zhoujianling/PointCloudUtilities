package cn.jimmiez.pcu.common;


import java.util.List;

public class Octree {

    private OctreeNode root = null;
    private List<double[]> points = null;
    private int depth = 1;

    /**
     * build space index for point cloud
     * @param points the point cloud
     * note that the length double array in List points must be 3
     */
    public void buildIndex(List<double[]> points) {
        this.depth = (int) (Math.ceil((Math.log((points.size() + 1) / 64) / Math.log(8))) + 1);
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
            this.root.minX = xyz[0] < this.root.minX ? xyz[0] : this.root.minX;
            this.root.maxX = xyz[0] > this.root.maxX ? xyz[0] : this.root.maxX;
            this.root.minY = xyz[1] < this.root.minY ? xyz[1] : this.root.minY;
            this.root.maxY = xyz[1] > this.root.maxY ? xyz[1] : this.root.maxY;
            this.root.minZ = xyz[2] < this.root.minZ ? xyz[2] : this.root.minZ;
            this.root.maxZ = xyz[2] > this.root.maxZ ? xyz[2] : this.root.maxZ;
        }

        OctreeNode current = this.root;
        for (int currentDepth = 1; currentDepth <= depth; currentDepth ++) {
            if (currentDepth < depth) {

            } else {

            }
        }

//        for (int i = 0; i < points.size(); i ++) {
//            double[] xyz = points.get(i);
//            this.root.addPoint(xyz, i);
//        }
    }

    /**
     * If you want to acquire the k-nearest neighbors of a certain point p,
     * you can call this function, octree will decrease the time cost
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

    private class OctreeNode {
        List<Integer> indices = null;
        OctreeNode[] children = null;
        double minX = Double.NaN;
        double maxX = Double.NaN;
        double minY = Double.NaN;
        double maxY = Double.NaN;
        double minZ = Double.NaN;
        double maxZ = Double.NaN;

        public void addPoint(double[] point, int index) {

        }

        public boolean contains(double[] point) {
            return point[0] >= minX && point[0] <= maxX &&
                    point[1] >= minY && point[1] <= minY &&
                    point[2] >= minZ && point[2] <= maxZ;
        }
    }

    public int getDepth() {
        return depth;
    }
}
