package cn.jimmiez.pcu.alg.skel;

import cn.jimmiez.pcu.common.graph.*;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.io.ply.PlyWriter;
import javafx.util.Pair;

import javax.vecmath.Point3d;
import java.util.*;

/**
 * The implementation of skeletonization method which is proposed in:
 * Verroust, A., & Lazarus, F. (2000). Extracting skeletal curves from 3D scattered data.
 * The Visual Computer, 16(1), 15-25.
 *
 * This method can work on the point cloud of **TREE-LIKE** object.
 */
public class LevelSetSkeleton implements Skeletonization{

    /** point cloud **/
    private List<Point3d> data;

    /** the octree to speed up search nearest neighbors **/
    private Octree octree = null;

    /** constructed by connecting n nearest neighbors of each vertex  **/
    private GraphStatic neighborhoodGraph = null;

    /** composed of shortest paths from source point to each other vertex **/
    private GraphStatic geodesicGraph = null;

    /** each level set is composed of connected subGraphs  **/
    private List<LevelSet> levelSets = null;

    /** the ith value in the list is the length of shortest path from source point to the ith point in the data set **/
    private List<Double> distanceMap = null;

    /** the shortest paths and shortest distance **/
    private List<Pair<List<Integer>, Weight>> paths;

    /** the resulting curve skeleton **/
    private Skeleton skeleton = null;

    /** the initial value of source index **/
    private static final int INVALID_INDEX = -1;

    /** point cloud must contain at least 3 points **/
    private static final int MIN_DATA_SIZE = 3;

    /** the heuristic strategy of choosing random point **/
    private static int RANDOM_SOURCE_INDEX = 0;

    /** the default value of k **/
    private static int DEFAULT_LEVEL_NUM = 20;

    /** the index of source point **/
    private int source = INVALID_INDEX;


    /** parameter n of neighborhood graph **/
    private int n = 5;

    /** the number of level sets **/
    private int k = DEFAULT_LEVEL_NUM;

    /** controls the lower bound of distribution interval of k level sets **/
    private double alpha = 0.03;

    /** controls the upper bound of distribution interval of k level sets **/
    private double beta = 0.97;

    private void init() {
        if (data.size() < MIN_DATA_SIZE) {
            throw new IllegalStateException("Too few points provided.");
        }
        skeleton = new Skeleton();
        distanceMap = new Vector<>();
        paths = new Vector<>();
        octree = new Octree();
        octree.buildIndex(data);
        n = Math.min(data.size(), n);
    }

    private void clean() {
        octree = null;
        distanceMap = null;
        neighborhoodGraph = null;
        geodesicGraph = null;
        paths = null;
        levelSets = null;

        source = INVALID_INDEX;

    }

    /**
     * build the neighborhood graph
     * @param n connect n nearest neighbors
     */
    private void buildNeighborhoodGraph(int n) {
        List<int[]> nnIndices = new Vector<>();
        for (int i = 0; i < data.size(); i ++) {
            int[] neighborIndices = octree.searchNearestNeighbors(n, i);
            nnIndices.add(neighborIndices);
        }
        neighborhoodGraph = Graphs.knnGraph(data, nnIndices);
        checkConnectivity();
    }

    /**
     * a proper n should be chosen to guarantee that the neighborhood graph is
     * a connected graph
     */
    private void checkConnectivity() {
        List<List<Integer>> subGraphs = Graphs.connectedComponents(neighborhoodGraph);
        if (subGraphs.size() != 1) {
            System.out.println("Current n is " + this.n);
            throw new IllegalStateException("The neighborhood graph is not a connected graph." +
                    " You can specify a larger n by calling setN().");
        }
    }

    /**
     * compute geodesic graph using dijkstra algorithm
     * the source point must be specified
     */
    private void buildGeodesicGraph() {
        if (source < 0 || source >= data.size()) {
            throw new IllegalStateException("The index of source point is invalid.");
        }
        paths = ShortestPath.dijkstra(neighborhoodGraph, source);
        final List<List<Integer>> edges = new Vector<>();
        final Set<Pair<Integer, Integer>> edgesSet = new HashSet<>();
        for (int i = 0; i < data.size(); i ++) edges.add(new Vector<Integer>());
        for (int i = 0; i < paths.size(); i ++) {
            Pair<List<Integer>, Weight> pair = paths.get(i);
            distanceMap.add(pair.getValue().val());
            List<Integer> path = pair.getKey();
            for (int j = 1; j < path.size(); j ++) {
                int prevNodeIndex = path.get(j - 1);
                int currNodeIndex = path.get(j);
                if (prevNodeIndex == currNodeIndex) continue;
                Pair<Integer, Integer> edge1 = new Pair<>(prevNodeIndex, currNodeIndex);
                Pair<Integer, Integer> edge2 = new Pair<>(currNodeIndex, prevNodeIndex);
                if (! edgesSet.contains(edge1)) {
                    edges.get(prevNodeIndex).add(currNodeIndex);
                    edgesSet.add(edge1);
                }
                if (! edgesSet.contains(edge2)) {
                    edges.get(currNodeIndex).add(prevNodeIndex);
                    edgesSet.add(edge2);
                }
            }
        }
        geodesicGraph = new GraphStatic() {
            @Override
            public double edgeWeight(int i, int j) {
                return data.get(i).distance(data.get(j));
            }

            @Override
            public int verticesCount() {
                return data.size();
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return edges.get(i);
            }
        };
    }

    /**
     * in this procedure, all data points are divided into k level sets
     * the parameter k should be given by user, here a default k = 30 is provided.
     *
     * the k level sets is uniformly distributed over the interval [dα， dβ],
     * where d is the furthest geodesic distance
     */
    private void divideLevelSets() {
        int furthestPointIndex = 0;
        for (int i = 0; i < data.size(); i ++) {
            if (distanceMap.get(furthestPointIndex) < distanceMap.get(i)) {
                furthestPointIndex = i;
            }
        }
        computeEmpiricalK(furthestPointIndex);
        // following code divides level sets
        if (k < 1 || k >= data.size()) {
            throw new IllegalStateException("Improper k is set.");
        }
        double intervalLowerBound = distanceMap.get(furthestPointIndex) * alpha;
        double intervalUpperBound = distanceMap.get(furthestPointIndex) * beta;
        double subInterval = (intervalUpperBound - intervalLowerBound) / k;
        levelSets = new Vector<>();
        for (int i = 0; i < k; i ++) {
            levelSets.add(new LevelSet());
        }
        for (int i = 0; i < data.size(); i ++) {
            double geodesicDistance = distanceMap.get(i);
            if (geodesicDistance <= intervalLowerBound || geodesicDistance >= intervalUpperBound) continue;
            int levelSetPosition = (int) ((geodesicDistance - intervalLowerBound) / subInterval);
            levelSets.get(levelSetPosition).addPoint(data.get(i), i);
        }
    }

    /**
     * in practice, k can be chosen as the ratio of the distances of the furthest
     * point to the average edge length
     */
    private void computeEmpiricalK(int furthestPointIndex) {
        //todo automatically compute k
    }

    private void generateResultingSkeleton() {
//        System.out.println("num of level set: " + levelSets.size());
        // skeleton node -> ( level set, sub graph )
        List<Pair<LevelSet, List<Integer>>> node2LS = new Vector<>();
        for (LevelSet levelSet : levelSets) {
            levelSet.partition();
//            System.out.println("num of sub graphs: " + levelSet.subGraphs.size());
            for (List<Integer> subGraph : levelSet.subGraphs) {
                double x = 0;
                double y = 0;
                double z = 0;
                for (Integer index : subGraph) {
                    x += levelSet.points.get(index).x;
                    y += levelSet.points.get(index).y;
                    z += levelSet.points.get(index).z;
                }
                // the barycenter of each connected component is the skeleton node
                Point3d skeletonNode = new Point3d(x / subGraph.size(), y / subGraph.size(), z / subGraph.size());
                skeleton.addNode(skeletonNode);
                node2LS.add(new Pair<>(levelSet, subGraph));
            }
        }
        // connect the skeleton points
        for (int nodeIndex = 0; nodeIndex < skeleton.getSkeletonNodes().size(); nodeIndex ++) {
            LevelSet levelSet = node2LS.get(nodeIndex).getKey();
            List<Integer> subGraph = node2LS.get(nodeIndex).getValue();
            int randomPointIndex = levelSet.indexInPointCloud.get(subGraph.get(0));
            List<Integer> path = paths.get(randomPointIndex).getKey();
            for (int i = path.size() - 1; i >= 0; i --) {
                int pointIndex = path.get(i);
                // find S_j-1
            }
        }
    }


    /**
     * determine the source point according to two-step heuristic strategy in Section.3
     * of original level set paper.
     * 1. a point x0 is chosen at random
     * 2. the source point is defined as the furthest point from x0 on the neighborhood graph
     */
    private void determineSourcePoint() {
        if (source != INVALID_INDEX) return;
        List<Pair<List<Integer>, Weight>> paths = ShortestPath.dijkstra(neighborhoodGraph, RANDOM_SOURCE_INDEX);
        double maximalDistance = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < paths.size(); i ++) {
            Pair<List<Integer>, Weight> pair = paths.get(i);
            if (pair.getValue().val() > maximalDistance) {
                source = i;
            }
        }
    }

    /**
     * after instantiate the LevelSetSkeleton, you can call this method to generate skeleton.
     * The parameter n is set to 5 by default.
     * The source point can be specified by user before calling skeletonize().
     * @param pointCloud the scattered data points
     * @return the curve skeleton, represented as an undirected acyclic graph (UAG)
     */
    @Override
    public Skeleton skeletonize(List<Point3d> pointCloud) {
        this.data = pointCloud;
        init();
        buildNeighborhoodGraph(n);
        determineSourcePoint();
        buildGeodesicGraph();
        divideLevelSets();
        generateResultingSkeleton();
        clean();
        return skeleton;
    }

    private static class LevelSet {

        private List<Point3d> points = new Vector<>();

        // the index of point in the point cloud
        private List<Integer> indexInPointCloud = new Vector<>();

        List<List<Integer>> subGraphs = null;

        public void addPoint(Point3d p, int i) {
            points.add(p);
            indexInPointCloud.add(i);
        }

        /**
         * edges longer than w_i will be removed
         * in practice, w_i is computed using the median of the distribution of the
         * distance values of the second nearest neighbor of each point inside level set
         * @param graph 2-nearest neighbor graph
         * @param sum the sum of distance between each point and its second nearest neighbor
         */
        void removeEdges(Graph graph, double sum) {
            double wi = sum / graph.verticesCount();
            List<int[]> edges = new Vector<>();
            for (Integer vi : graph.vertices()) {
                for (Integer vj : graph.adjacentVertices(vi)) {
                    double weight = graph.edgeWeight(vi, vj);
                    if (weight >= wi) {
                        edges.add(new int[] {vi, vj});
                        edges.add(new int[] {vj, vi});
                    }
                }
            }
            for (int[] edge : edges) {
                graph.removeEdge(edge[0], edge[1]);
            }
        }

        /**
         * a two-nearest neighbor graph is needed for partitioning level set
         */
        void partition() {
            Octree octree = new Octree();
            octree.buildIndex(points);
            Graph graph = new DirectedGraph();
            double secondaryEdgeSum = 0.0;
            for (int i = 0; i < points.size(); i ++) graph.addVertex(i);
            for (int i = 0; i < points.size(); i ++) {
                int[] indices = octree.searchNearestNeighbors(5, i);
                for (int j = 0; j < indices.length; j ++) {
                    int index = indices[j];
                    double dis = points.get(i).distance(points.get(index));
                    if (j == indices.length - 1) secondaryEdgeSum += dis;
                    graph.addEdge(i, index, dis);
                    graph.addEdge(index, i, dis);
                }
            }
//            removeEdges(graph, secondaryEdgeSum);
            subGraphs = Graphs.connectedComponents(graph);
        }
    }

    public void setRoot(int p) {
        this.source = p;
    }

    public int getRoot() {
        return source;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getN() {
        return this.n;
    }

    public void setK(int k ) {
        this.k = k;
    }

    public int getK() {return this.k;}
}
