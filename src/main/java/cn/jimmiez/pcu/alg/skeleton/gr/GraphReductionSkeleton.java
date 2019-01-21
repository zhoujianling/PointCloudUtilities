package cn.jimmiez.pcu.alg.skeleton.gr;


import cn.jimmiez.pcu.alg.sampler.OctreeVoxelizer;
import cn.jimmiez.pcu.alg.skeleton.Skeletonization;
import cn.jimmiez.pcu.common.graph.EntityGraph;
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
    }

    private void extractGraph() {

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
            i2i.put(skeletonVi, vi);
        }
        for (int skeletonVi : skeleton.vertices()) {
            int vi = i2i.get(skeletonVi);
            for (int skeletonAi : skeleton.adjacentVertices(skeletonVi)) {
                int ai = i2i.get(skeletonAi);
                double edgeWeight = graph.edgeWeight(vi, ai);
                skeleton.addEdge(skeletonVi, skeletonAi, edgeWeight);
            }
        }
        return skeleton;
    }

    private void init(List<Point3d> pointCloud) {
        this.points.clear();
        this.points.addAll(pointCloud);
    }

    @Override
    public Skeleton skeletonize(List<Point3d> pointCloud) {
        init(pointCloud);
        generateOctree();
        extractGraph();
        graphReduction();
        return obtainSkeleton(graph);
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
