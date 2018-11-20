package cn.jimmiez.pcu.alg.skeleton;

import cn.jimmiez.pcu.common.graph.*;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.model.Pair;
import cn.jimmiez.pcu.util.PcuCommonUtil;

import javax.vecmath.Point3d;
import java.util.*;

/**
 * The implementation of skeletonization method which is proposed in:
 * Verroust, A., &amp; Lazarus, F. (2000). Extracting skeletal curves from 3D scattered data.
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
    private Map<Integer, Pair<List<Integer>, Double>> paths;

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
        paths = new HashMap<>();
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
        final List<Integer> vertices = PcuCommonUtil.incrementalIntegerList(data.size());
        final Set<Pair<Integer, Integer>> edgesSet = new HashSet<>();
        for (int i = 0; i < data.size(); i ++) edges.add(new Vector<Integer>());
        for (int i = 0; i < paths.size(); i ++) {
            Pair<List<Integer>, Double> pair = paths.get(i);
            distanceMap.add(pair.getValue());
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
            public List<Integer> adjacentVertices(int i) {
                return edges.get(i);
            }

            @Override
            public Collection<Integer> vertices() {
                return vertices;
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
//        System.out.println("k is " + k);
        // following code divides level sets
        if (k < 1 || k >= data.size()) {
            throw new IllegalStateException("Improper k is set.");
        }
        double intervalLowerBound = distanceMap.get(furthestPointIndex) * alpha;
        double intervalUpperBound = distanceMap.get(furthestPointIndex) * beta;
        double subInterval = (intervalUpperBound - intervalLowerBound) / k;
        levelSets = new Vector<>();
        for (int i = 0; i < k + 2; i ++) {
            levelSets.add(new LevelSet());
        }
        for (int i = 0; i < data.size(); i ++) {
            double geodesicDistance = distanceMap.get(i);
            if (geodesicDistance <= intervalLowerBound) {
                levelSets.get(0).addPoint(data.get(i), i);
            } else if (geodesicDistance >= intervalUpperBound) {
                levelSets.get(k + 1).addPoint(data.get(i), i);
            } else {
                int levelSetPosition = (int) ((geodesicDistance - intervalLowerBound) / subInterval) + 1;
                levelSets.get(levelSetPosition).addPoint(data.get(i), i);
            }
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
        // System.out.println("num of level set: " + levelSets.size());
        // divide a level set into multiple connected components
        for (LevelSet levelSet : levelSets) {
            levelSet.partition();
            // System.out.println("num of sub graphs: " + levelSet.subGraphs.size());
        }
        // connect the skeleton points
        for (int levelSetIndex = 0; levelSetIndex < levelSets.size(); levelSetIndex ++) {
            LevelSet levelSet = levelSets.get(levelSetIndex);
            for (int subGraphIndex = 0; subGraphIndex < levelSet.subGraphs.size(); subGraphIndex ++) {
                Pair<Integer, Integer> pair = searchAdjacentSkeletonPoint(levelSetIndex, subGraphIndex);
                levelSet.adjacency.add(pair);
            }
        }
        // compute position of skeleton point
        for (int levelSetIndex = 0; levelSetIndex < levelSets.size(); levelSetIndex ++) {
            LevelSet levelSet = levelSets.get(levelSetIndex);
            for (int subGraphIndex = 0; subGraphIndex < levelSet.subGraphs.size(); subGraphIndex ++) {
                List<Integer> subGraph = levelSet.subGraphs.get(subGraphIndex);
                // the barycenter of each connected component is the skeleton node
                skeleton.addNode(baryCenter(levelSet.points, subGraph));
                levelSet.skeletonNodeIndices.add(skeleton.getSkeletonNodes().size() - 1);
//                Point3d p = skeleton.getSkeletonNodes().get(skeleton.getSkeletonNodes().size() - 1);
//                System.out.println("" + p.x + " " + p.y + " " + p.z);
            }
        }
        for (int levelSetIndex = 0; levelSetIndex < levelSets.size(); levelSetIndex ++) {
            LevelSet levelSet = levelSets.get(levelSetIndex);
            for (int subGraphIndex = 0; subGraphIndex < levelSet.subGraphs.size(); subGraphIndex ++) {
                // the barycenter of each connected component is the skeleton node
                int v1 = levelSet.skeletonNodeIndices.get(subGraphIndex);
                int adjacentLevelSetIndex = levelSet.adjacency.get(subGraphIndex).getKey();
                int adjacentSubGraphIndex = levelSet.adjacency.get(subGraphIndex).getValue();
                int v2 = levelSets.get(adjacentLevelSetIndex).skeletonNodeIndices.get(adjacentSubGraphIndex);
                double weight = skeleton.getSkeletonNodes().get(v1).distance(skeleton.getSkeletonNodes().get(v2));
                skeleton.addEdge(v1, v2, weight);
            }
        }
    }

    private Point3d baryCenter(List<Point3d> points, List<Integer> indices) {
        double x = 0;
        double y = 0;
        double z = 0;
        for (Integer index : indices) {
            x += points.get(index).x;
            y += points.get(index).y;
            z += points.get(index).z;
        }
        return new Point3d(x / indices.size(), y / indices.size(), z / indices.size());
    }

    /**
     * given a sub graph(connected component) in point cloud, search the next skeleton point along the shortest path
     * @param levelSetIndex the index of level set where the data point is located
     * @param subGraphIndex the index of subGraph
     */
    private Pair<Integer, Integer> searchAdjacentSkeletonPoint(int levelSetIndex, int subGraphIndex) {
        if (levelSetIndex < 0) throw new IllegalArgumentException("Negative index of level set.");
        if (levelSetIndex == 0) return new Pair<>(levelSetIndex, subGraphIndex);
        LevelSet levelSet = levelSets.get(levelSetIndex);
        List<Integer> subGraph = levelSet.subGraphs.get(subGraphIndex);
        int randomPointIndex = levelSet.indexInPointCloud.get(subGraph.get(0));

        List<Integer> path = paths.get(randomPointIndex).getKey();
        for (int i = path.size() - 1; i >= 0; i --) {
            int nextPointIndex = path.get(i);
            Point3d nextPoint = data.get(nextPointIndex);
            if (levelSet.indexInPointCloud.contains(nextPointIndex)) {
                continue;
            }
            // search in S_j-1
            LevelSet lowerLevelSet = levelSets.get(levelSetIndex - 1);
            for (int j = 0; j < lowerLevelSet.subGraphs.size(); j ++) {
                List<Integer> lowerSubGraph = lowerLevelSet.subGraphs.get(j);
                for (int pointLocalIndex : lowerSubGraph) {
                    if (lowerLevelSet.points.get(pointLocalIndex) == nextPoint) {
                        return new Pair<>(levelSetIndex - 1, j);
                    }
                }
            }
        }
        System.err.println("LevelSetSkeleton::searchAdjacentSkeletonPoint(): " +
                "Cannot find a connected component containing the point on the shortest path.");
        return new Pair<>(levelSetIndex, subGraphIndex);
    }


    /**
     * determine the source point according to two-step heuristic strategy in Section.3
     * of original level set paper.
     * 1. a point x0 is chosen at random
     * 2. the source point is defined as the furthest point from x0 on the neighborhood graph
     */
    private void determineSourcePoint() {
        if (source != INVALID_INDEX) return;
        Map<Integer, Pair<List<Integer>, Double>> paths = ShortestPath.dijkstra(neighborhoodGraph, RANDOM_SOURCE_INDEX);
        double maximalDistance = Double.MIN_VALUE;
        for (int i = 0; i < paths.size(); i ++) {
            Pair<List<Integer>, Double> pair = paths.get(i);
            if (pair.getValue() > maximalDistance) {
                maximalDistance = pair.getValue();
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

        /** the points from point cloud **/
        List<Point3d> points = new Vector<>();

        /** the index of point in the point cloud **/
        List<Integer> indexInPointCloud = new Vector<>();

        /** index of point in LevelSet.points **/
        List<List<Integer>> subGraphs = null;

        /**
         * a subGraph in subGraphs correspond to a skeleton node, this list store the edges
         * [ (levelSetIndex, subGraphIndex), ..., () ]
         * adjacency.size() == subGraph.size() == skeletonNodeIndices.size()
         **/
        List<Pair<Integer, Integer>> adjacency = new Vector<>();

        List<Integer> skeletonNodeIndices = new Vector<>();

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
            double wi = sum / graph.vertices().size();
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
                int[] indices = octree.searchNearestNeighbors(10, i);
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

    public GraphStatic getNeighborhoodGraph() {
        return neighborhoodGraph;
    }
}
