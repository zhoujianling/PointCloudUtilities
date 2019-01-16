package cn.jimmiez.pcu.common.graph;

import cn.jimmiez.pcu.util.Pair;
import cn.jimmiez.pcu.util.PcuCommonUtil;

import javax.vecmath.Point3d;
import java.util.*;

/**
 * This class provides some static methods for constructing a graph from matrix, operating
 * on graph.
 */
public class Graphs {

    /**
     * obtain connected components of a **UNDIRECTED GRAPH**.
     * @param graph the undirected graph
     * @return return a list, each element in it represents a sub-graph, which is a collection of
     * indices of vertices
     */
    public static List<List<Integer>> connectedComponents(BaseGraph graph) {
        if (graph.isDirected()) throw new UnsupportedOperationException("Currently this method cannot operate on directed graph.");
        List<List<Integer>> subGraphs = new Vector<>();
        Set<Integer> visited = new HashSet<>();
//        System.out.println("num of vertices: " + graph.vertices().size());
        for (int i : graph.vertices()) {
            if (visited.contains(i)) continue;
            List<Integer> subGraph = new ArrayList<>();

            List<Integer> visitQueue = new Vector<>();
            visitQueue.add(i);
            for (int ptr = 0; ptr < visitQueue.size(); ptr ++) {
                int visiting = visitQueue.get(ptr);
                if (! visited.contains(visiting)) {
                    visited.add(visiting);
                    subGraph.add(visiting);
                    for (int adjacentVertex : graph.adjacentVertices(visiting)) visitQueue.add(adjacentVertex);
                }
            }
            subGraphs.add(subGraph);
        }
        return subGraphs;
    }

    /**
     * determine if a graph contains cycles.
     * self-loop is not regarded as a cycle here.
     * for:
     * <ul>
     *     <li>Directed Graph: a cycle must contains at least two vertices.</li>
     *     <li>Undirected Graph: a cycle must contains at least three vertices.</li>
     * </ul>
     * @param graph a base graph
     * @return if this graph has a cycle
     */
    public static boolean containsCycle(BaseGraph graph) {
        if (graph.vertices().size() < 1) return false;
        if (graph.isDirected()) {
            Set<Integer> visited = new HashSet<>(graph.vertices().size());
            List<Integer> queue = new ArrayList<>(graph.vertices().size());
            int i = 0;
            for (int vi : graph.vertices()) {
                if (visited.contains(vi)) continue;
                // new sub graph, vi is the root vertex
                Set<Integer> subVisited = new HashSet<>(graph.vertices().size());
                queue.add(vi);
                for (; i < queue.size(); i ++) {
                    int v = queue.get(i);
                    subVisited.add(vi);
                    for (int ai : graph.adjacentVertices(v)) {
                        if (subVisited.contains(ai)) return true;
                        queue.add(ai);
                    }
                }
                visited.addAll(subVisited);
            }
        } else {
            Set<Integer> visited = new HashSet<>(graph.vertices().size());
            List<Pair<Integer, Integer>> queue = new ArrayList<>();
            int i = 0;
            for (int vi : graph.vertices()) {
                if (visited.contains(vi)) continue;
                // new sub graph, vi is the root vertex
                for (int ai : graph.adjacentVertices(vi)) queue.add(new Pair<>(vi, ai));
                visited.add(vi);
                for (; i < queue.size(); i ++) {
                    int pi = queue.get(i).getKey();
                    int pj = queue.get(i).getValue();
                    for (int ai : graph.adjacentVertices(pj)) {
                        if (ai == pi) continue;
                        if (visited.contains(ai)) return true;
                        queue.add(new Pair<>(pj, ai));
                    }
                    visited.add(pj);
                }
            }
        }
        return false;
    }

    /**
     * get a graph where the vertices is a sub-set of another graph
     * @param graph another graph
     * @param vertices indices of vertices
     * @return the sub-graph
     */
    public static BaseGraph subGraph(final BaseGraph graph, final Set<Integer> vertices) {
        final Map<Integer, List<Integer>> adjacencyMap = new HashMap<>();
        final boolean isDirecetd = graph.isDirected();
        for (int vertexIndex : vertices) {
            Collection<Integer> adjacents = graph.adjacentVertices(vertexIndex);
            List<Integer> adjacentsInSubGraph = new ArrayList<>();
            for (int index : adjacents) {
                if (vertices.contains(index)) {
                    adjacentsInSubGraph.add(index);
                }
            }
            adjacencyMap.put(vertexIndex, adjacentsInSubGraph);
        }
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                if (vertices.contains(i) && vertices.contains(j))
                    return graph.edgeWeight(i, j);
                throw new IllegalArgumentException("Invalid index of vertex: " + i + " " + j + ".");
            }

            @Override
            public Collection<Integer> adjacentVertices(int i) {
                return adjacencyMap.get(i);
            }

            @Override
            public Collection<Integer> vertices() {
                return vertices;
            }

            @Override
            public boolean isDirected() {
                return isDirecetd;
            }
        };
    }

    /**
     * compute the number of edges in a graph.
     *
     * e_ij e_ji are seen as two different edges despite the directness.
     * @param graph a graph
     * @return number of edges(
     */
    public static int edgesCountOf(BaseGraph graph) {
        int result = 0;
        for (int vertexIndex : graph.vertices()) {
            result += graph.adjacentVertices(vertexIndex).size();
        }
        return result;
    }

    public static BaseGraph fullConnectedGraph(final List<Point3d> vertices, final boolean isDirected) {
        final List<Integer> vt = new Vector<>();
        for (int i = 0; i < vertices.size(); i ++) vt.add(i);
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                if (i < 0 || i >= vt.size() || j < 0 || j >= vt.size())
                    throw new IllegalArgumentException("Invalid index of vertex: " + i + " " + j + ".");
                return vertices.get(i).distance(vertices.get(j));
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                if (i < 0 || i >= vt.size())
                    throw new IllegalArgumentException("Invalid index of vertex: " + i + " " + ".");
                return vt;
            }

            @Override
            public Collection<Integer> vertices() {
                return vt;
            }

            @Override
            public boolean isDirected() {
                return isDirected;
            }
        };
    }


    /**
     * construct k-nearest-neighbor graph
     * @param vertices the vertices
     * @param knnIndices the k-nearest neighbors of vertices
     * @return a undirected graph
     */
    public static BaseGraph knnGraph(final List<Point3d> vertices, final List<int[]> knnIndices) {
        final Set<Pair<Integer, Integer>> knnEdges = new HashSet<>();
        final List<Integer> verticesIndices = new ArrayList<>();
        for (int i = 0; i < knnIndices.size(); i ++) {
            verticesIndices.add(i);
            for (int j : knnIndices.get(i)) {
                knnEdges.add(new Pair<>(i, j));
            }
        }
        final List<List<Integer>> knnIndicesList = adjacentMatrix2List(knnIndices);
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                if (i < 0 || i >= vertices.size() || j < 0 || j >= vertices.size())
                    throw new IllegalArgumentException("Invalid index of vertex: " + i + " " + j + ".");
                Pair<Integer, Integer> p = new Pair<>(i, j);
                if (knnEdges.contains(p)) {
                    return vertices.get(i).distance(vertices.get(j));
                }
                return Double.POSITIVE_INFINITY;
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                if (i < 0 || i >= vertices.size())
                    throw new IllegalArgumentException("Invalid index of vertex: " + i + ".");
                return knnIndicesList.get(i);
            }

            @Override
            public Collection<Integer> vertices() {
                return verticesIndices;
            }

            @Override
            public boolean isDirected() {
                return false;
            }
        };
    }

    private static List<List<Integer>> adjacentMatrix2List(final double[][] edgesMatrix, boolean directed) {
        List<List<Integer>> adjacencies = new Vector<>();
        int vn = edgesMatrix.length;
        for (int i = 0; i < vn; i ++) adjacencies.add(new ArrayList<Integer>());
        for (int i = 0; i < vn; i ++) {
            double[] edges = edgesMatrix[i];
            if (edges.length != vn) throw new IllegalArgumentException("Invalid adjacent matrix.");
            for (int j = 0; j < vn; j ++) {
                if (i == j) continue;
                if (! directed && i > j) continue;
                if (edges[j] != Graph.N) {
                    adjacencies.get(i).add(j);
                    if (! directed) adjacencies.get(j).add(i);
                }
            }
        }
        return adjacencies;
    }

    @SuppressWarnings("Duplicates")
    private static List<List<Integer>> adjacentMatrix2List(final List<int[]> adjacency) {
        List<List<Integer>> adjacencies = new Vector<>();
        for (int[] adjacencyArray : adjacency) {
            List<Integer> vec = new Vector<>();
            for (int index : adjacencyArray) vec.add(index);
            adjacencies.add(vec);
        }
        return adjacencies;
    }


    /**
     * construct a directed graph from given matrix
     * @param edges the edges, a matrix
     * @param directed if this graph is a directed graph
     * @return a graph
     */
    public static BaseGraph graph(final double[][]edges, final boolean directed) {
        final List<List<Integer>> adjacencies = adjacentMatrix2List(edges, directed);
        final List<Integer> vertices = PcuCommonUtil.incrementalIntegerList(edges.length);
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                if (i < 0 || i >= vertices.size() || j < 0 || j >= vertices.size())
                    throw new IllegalArgumentException("Invalid index of vertex: " + i + " " + j + ".");
                return edges[i][j];
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                if (i < 0 || i >= vertices.size())
                    throw new IllegalArgumentException("Invalid index of vertex: " + i + ".");
                return adjacencies.get(i);
            }

            @Override
            public Collection<Integer> vertices() {
                return vertices;
            }

            @Override
            public boolean isDirected() {
                return directed;
            }
        };
    }

    /**
     * construct a graph with no vertex and edge
     * @return a undirected graph
     */
    public static BaseGraph empty() {
        final List<Integer> vertices = new ArrayList<>();
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                throw new IllegalArgumentException("Invalid index of vertex.");
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                throw new IllegalArgumentException("Invalid index of vertex.");
            }

            @Override
            public Collection<Integer> vertices() {
                return vertices;
            }

            @Override
            public boolean isDirected() {
                return false;
            }
        };
    }

}
