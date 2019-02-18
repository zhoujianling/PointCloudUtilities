package cn.jimmiez.pcu.alg.skeleton.gr;


import cn.jimmiez.pcu.alg.sampler.OctreeVoxelizer;
import cn.jimmiez.pcu.alg.skeleton.Skeletonization;
import cn.jimmiez.pcu.common.graph.EntityGraph;
import cn.jimmiez.pcu.common.graphics.Adjacency;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.model.Skeleton;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GraphReductionSkeleton implements Skeletonization {

    /** raw point cloud **/
    private List<Point3d> points = new ArrayList<>();

    /** the list of octree cells **/
    private List<Octree.OctreeNode> cells = null;

    /** the dual star graph **/
    private DualStarGraph graph = null;

    /** the octree **/
    private GraphReductionOctree octree = null;

    /**
     * compute the coordinates of M-Vertex/T-Vertex
     * @param indices the list of indices of points in the cell/cell-face
     * @param points the list of point cloud
     * @return the mid-point
     */
    private Point3d meanPoint(List<Integer> indices, List<Point3d> points) {
        double x = 0, y = 0, z = 0;
        for (int index : indices) {
            x += points.get(index).x;
            y += points.get(index).y;
            z += points.get(index).z;
        }
        return new Point3d(x / indices.size(), y / indices.size(), z / indices.size());
    }

    private void generateOctree() {
        octree = new GraphReductionOctree();
        octree.buildIndex(points);
        cells = octree.voxelize(points);
    }

    /**
     * extract graph from octree
     */
    private void extractGraph() {
        // map the index of cell of the index of GRVertex
        Map<Long, Integer> i2i = new HashMap<>();
        graph = new DualStarGraph();

        // add M-Vertex
        for (Octree.OctreeNode cell : cells) {
            if (cell.getIndices().size() < 1) continue;
            Point3d point = meanPoint(cell.getIndices(), points);
            GRVertex vertex = new GRVertex(VertexType.M, point, cell.getIndex());
            int vi = graph.addVertex(vertex);
            i2i.put(vertex.cellIndex, vi);
        }

        // add T-Vertex
        for (Octree.OctreeNode cell : cells) {
            if (cell.getIndices().size() < 1) continue;
            int vi = i2i.get(cell.getIndex());
            List<Octree.OctreeNode> adjacentCells = octree.adjacentNodes(cell.getIndex(), Adjacency.FACE);
            for (Octree.OctreeNode adjacentCell : adjacentCells) {
                int vj = i2i.get(adjacentCell.getIndex());
                Point3d position = new Point3d(cell.getCenter());
                position.add(adjacentCell.getCenter());
                position.scale(0.50);
                GRVertex vertex = new GRVertex(VertexType.T, position, -1L);
                int vv = graph.addVertex(vertex);
                graph.addEdge(vi, vv, position.distance(cell.getCenter()));
                graph.addEdge(vj, vv, position.distance(adjacentCell.getCenter()));
            }
        }
    }

    /**
     * reduce the dual star graph into skeletal structure
     */
    private void graphReduction() {

    }

    /**
     * transform the graph to the skeleton
     * @param graph the graph(skeletal structure)
     * @return the curve skeleton(undirected graph)
     */
    private Skeleton obtainSkeleton(DualStarGraph graph) {
        Skeleton skeleton = new Skeleton();
        Map<Integer, Integer> i2i = new HashMap<>();
        for (int vi : graph.vertices()) {
            GRVertex vertex = graph.getVertex(vi);
            int skeletonVi = skeleton.addVertex(vertex);
            i2i.put(vi, skeletonVi);
        }
        for (int vi : graph.vertices()) {
            int skeletonVi = i2i.get(vi);
            for (int vj : graph.adjacentVertices(vi)) {
                int skeletonVj = i2i.get(vj);
                double edgeWeight = graph.edgeWeight(vi, vj);
                skeleton.addEdge(skeletonVi, skeletonVj, edgeWeight);
            }
        }
        return skeleton;
    }

    private void init(List<Point3d> pointCloud) {
        this.points.clear();
        this.points.addAll(pointCloud);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Skeleton skeletonize(List<Point3d> pointCloud) {
        init(pointCloud);
        generateOctree();
        extractGraph();
        graphReduction();
        return obtainSkeleton(graph);
    }

    public DualStarGraph getGraph() {
        return graph;
    }

    private enum VertexType {

        /** geometric mid-point in vertex in the cell **/
        M,
        /** the center of the touched cell-side **/
        T

    }

    /**
     * the vertex in the Dual-Star-Graph
     */
    private static class GRVertex extends Point3d {

        /** the type of vertex **/
        private VertexType type = VertexType.M;

        /**
         * the index of vertex of original octree node,
         * if this is a merge vertex, the index is -1L
         * **/
        private Long cellIndex = -1L;

        /** the default empty constructor **/
        public GRVertex() {}

        public GRVertex(VertexType vt, Point3d point, Long index) {
            super(point);
            this.type = vt;
            this.cellIndex = index;
        }

    }

    private static class DualStarGraph extends EntityGraph<GRVertex> {

        /**
         * Construct the dual star graph
         */
        public DualStarGraph() {
            super(false);
        }

    }

}
