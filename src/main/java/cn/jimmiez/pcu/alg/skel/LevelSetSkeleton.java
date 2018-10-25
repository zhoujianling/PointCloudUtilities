package cn.jimmiez.pcu.alg.skel;

import cn.jimmiez.pcu.common.graph.ShortestPath;
import cn.jimmiez.pcu.common.graph.Weight;
import cn.jimmiez.pcu.common.graphics.Octree;
import cn.jimmiez.pcu.common.graph.Graph;
import cn.jimmiez.pcu.common.graph.Graphs;
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

    /** constructed by connecting k nearest neighbors of each vertex  **/
    private Graph neighborhoodGraph = null;

    /** composed of shortest paths from source point to each other vertex **/
    private Graph geodesicGraph = null;

    /** each level set is composed of connected subgraphs  **/
    private List<LevelSet> levelSets = null;

    /** the resulting curve skeleton **/
    private Skeleton skeleton = null;

    /** the initial value of source index **/
    private static final int INVALID_INDEX = -1;

    /** point cloud must contain at least 3 points **/
    private static final int MIN_DATA_SIZE = 3;

    /** the heuristic strategy of choosing random point **/
    private static int RANDOM_SOURCE_INDEX = 0;

    /** the index of source point **/
    private int source = INVALID_INDEX;

    /** k of neighborhood graph **/
    private int k = 5;

    private void init() {
        if (data.size() < MIN_DATA_SIZE) {
            throw new IllegalStateException("Too few points provided.");
        }
        skeleton = new Skeleton();
        octree = new Octree();
        octree.buildIndex(data);
        k = Math.min(data.size(), k);
    }

    private void clean() {
        octree = null;
        source = INVALID_INDEX;
    }

    /**
     * build the neighborhood graph
     * @param k connect k nearest neighbors
     */
    private void buildNeighborhoodGraph(int k) {
        List<int[]> nnIndices = new Vector<>();
        for (int i = 0; i < data.size(); i ++) {
            int[] neighborIndices = octree.searchNearestNeighbors(k, i);
            nnIndices.add(neighborIndices);
        }
        neighborhoodGraph = Graphs.knnGraph(data, nnIndices);
        checkConnectivity();
    }

    /**
     * a proper k should be chosen to guarantee that the neighborhood graph is
     * a connected graph
     */
    private void checkConnectivity() {
        List<List<Integer>> subGraphs = Graphs.connectedComponents(neighborhoodGraph);
        if (subGraphs.size() != 1) {
            System.out.println("Current k is " + this.k);
            throw new IllegalStateException("The neighborhood graph is not a connected graph." +
                    " You can specify a larger k by calling setK().");
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
        List<Pair<List<Integer>, Weight>> paths = ShortestPath.dijkstra(neighborhoodGraph, source);
        final List<List<Integer>> edges = new Vector<>();
        final Set<Pair<Integer, Integer>> edgesSet = new HashSet<>();
        for (int i = 0; i < data.size(); i ++) edges.add(new Vector<Integer>());
        for (int i = 0; i < paths.size(); i ++) {
            Pair<List<Integer>, Weight> pair = paths.get(i);
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
        geodesicGraph = new Graph() {
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

    private void divideLevelSets() {

    }

    private void generateCurveSkeleton() {

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
     * The parameter k is set to 5 by default.
     * The source point can be specified by user before calling skeletonize().
     * @param pointCloud the scattered data points
     * @return the curve skeleton, represented as an undirected acyclic graph (UAG)
     */
    @Override
    public Skeleton skeletonize(List<Point3d> pointCloud) {
        this.data = pointCloud;
        init();
        buildNeighborhoodGraph(k);
        determineSourcePoint();
        buildGeodesicGraph();
        divideLevelSets();
        generateCurveSkeleton();
        clean();
        return skeleton;
    }

    private static class LevelSet {
        List<List<Integer>> subgraphs;
    }

    public void setRoot(int p) {
        this.source = p;
    }

    public int getRoot() {
        return source;
    }

    public void setK(int k) {
        this.k = k;
    }

    public int getK() {
        return this.k;
    }
}
