package cn.jimmiez.pcu.alg.sampler;

import cn.jimmiez.pcu.common.graphics.BoundingBox;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.common.graphics.shape.Box;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.*;

public class GridVoxelizer {

    /**
     * the key is index of {@link GridCell}, {@link GridVoxelizer} use this
     * map to quickly find the points in a voxel
     **/
    private Map<Long, GridCell> cellMap = new HashMap<>();

    /** the raw point cloud **/
    private List<Point3d> points;

    private Box gridBox = null;

    /** the number of cells in x-direction **/
    private int xCount = -1;

    /** the number of cells in y-direction **/
    private int yCount = -1;

    /** the number of cells in z-direction **/
    private int zCount = -1;

    private double cellSize = -1;

    private double cellRatio = -1;

    private boolean specifyCellSize = false;

    public GridVoxelizer() {
        this(2.5);
    }

    public GridVoxelizer(double ratio) {
        this.cellRatio = ratio;
    }

    /**
     * Perform the voxelizing.
     * The call of this method will divide the space into m x n voxel.
     * The size of cells are automatically determined according to the {@link GridVoxelizer#cellRatio} and average
     * length of edges in three-nearest-neighbor graph.
     * @param points the point cloud to be voxelized
     * @return the list of grid cells
     */
    public List<GridCell> voxelize(List<Point3d> points) {
        return this.voxelize(points, -1);
    }

    /**
     * Perform the voxelizing.
     * @param points the point cloud to be voxelized
     * @param cellSize the size of each cell
     * @return the list of gird cells
     */
    public List<GridCell> voxelize(List<Point3d> points, double cellSize) {
        this.points = points;
        if (cellSize > 0) this.specifyCellSize = true;
        this.cellSize = cellSize;
        determineVoxelNumber();
        divideVoxelImpl();
        List<GridCell> list = new ArrayList<>();
        list.addAll(cellMap.values());
        return list;
    }

    private void determineVoxelNumber() {
        BoundingBox tightBox = BoundingBox.of(points);

        if (! specifyCellSize) {
//        List<Double> threeNNEdgeLength = new ArrayList<>();
            Octree octree = new Octree();
            octree.buildIndex(points);

            double lengthSum = 0.0;
            int edgeCnt = 0;
            int sampleCnt = Math.min(points.size(), Math.max(points.size() / 15, 1000));
            Random random = new Random(System.currentTimeMillis());
            for (int i = 0; i < sampleCnt; i ++) {
                int randomIndex = random.nextInt(points.size());
                Point3d point = points.get(randomIndex);
                for (int index : octree.searchNearestNeighbors(3, randomIndex)) {
                    Point3d neighbor = points.get(index);
                    Vector3d vector = new Vector3d(neighbor.x - point.x, neighbor.y - point.y, neighbor.z - point.z);
                    lengthSum += vector.length();
                    edgeCnt += 1;
                }
            }
            double averageLength = lengthSum / edgeCnt;
            cellSize = averageLength * cellRatio;
            gridBox = new Box(tightBox.getCenter(), tightBox.getxExtent() + averageLength, tightBox.getyExtent() + averageLength, tightBox.getzExtent() + averageLength);
        } else {
            double tolerance = cellSize * 0.3;
            gridBox = new Box(tightBox.getCenter(), tightBox.getxExtent() + tolerance, tightBox.getyExtent() + tolerance, tightBox.getzExtent() + tolerance);
        }

        xCount = (int) Math.ceil(gridBox.getxExtent() * 2 / cellSize);
        yCount = (int) Math.ceil(gridBox.getyExtent() * 2 / cellSize);
        zCount = (int) Math.ceil(gridBox.getzExtent() * 2 / cellSize);
//        System.out.println("xc, yc, zc: " + xCount + " " + yCount + " " + zCount);
    }

    private void divideVoxelImpl() {
        for (int i = 0; i < points.size(); i ++) {
            Point3d p = points.get(i);
            long index = findVoxel(p);
            if (cellMap.get(index) == null) cellMap.put(index, new GridCell(index));
            cellMap.get(index).indices.add(i);
        }
    }

    private long findVoxel(Point3d point) {
        int x, y, z;
        x = (int) ((point.x - gridBox.minX()) / cellSize);
        y = (int) ((point.y - gridBox.minY()) / cellSize);
        z = (int) ((point.z - gridBox.minZ()) / cellSize);
        x = Math.min(x, xCount - 1);
        y = Math.min(y, yCount - 1);
        z = Math.min(z, zCount - 1);
        return indexOfCell(x, y, z);
    }

    public Long indexOfCell(int xRow, int yRow, int zRow) {
        long index = zRow;
        index <<= 20;
        index |= yRow;
        index <<= 20;
        index |= xRow;
        return index;
    }

    // return [xRow, yRow, zRow]
    public int[] parseIndex(long index) {
        int[] coordinates = new int[3];
        long mask = 0x000F_FFFF;
        coordinates[0] = (int) (index & mask);
        coordinates[1] = (int) ((index >> 20) & mask);
        coordinates[2] = (int) ((index >> 40) & mask);
        return coordinates;
    }

    public double getCellSize() {
        return this.cellSize;
    }

    public class GridCell {

        /**
         * the index of this cell,
         * the right-most 20 bits(x) indices the y-z plane in the voxel grid,
         * from right to left, the 21th to 40th bits(y) indices the x-z plane,
         * from right to left, the 41th to 60th bits(z) indices the x-y plane,
         * each index indices the unique grid cell in the voxel grid.
         **/
        private Long index;

        /**
         * the indices of points which are located in this cell
         */
        private List<Integer> indices = new ArrayList<>();

        public GridCell(Long index) {
            this.index = index;
        }

        public Long getIndex() {
            return index;
        }

        public List<Integer> getIndices() {
            return indices;
        }
    }
}

