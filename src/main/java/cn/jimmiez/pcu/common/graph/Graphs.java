package cn.jimmiez.pcu.common.graph;

import cn.jimmiez.pcu.util.Pair;
import cn.jimmiez.pcu.util.PcuCommonUtil;

import javax.vecmath.Point3d;
import java.util.*;

public class Graphs {

    public static List<List<Integer>> connectedComponents(BaseGraph graph) {
        List<List<Integer>> subGraphs = new Vector<>();
        Set<Integer> visited = new HashSet<>();
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
     * get a graph where the vertices is a sub-set of another graph
     * @param graph another graph
     * @param vertices indices of vertices
     * @return the sub-graph
     */
    public static BaseGraph subGraph(final BaseGraph graph, final Set<Integer> vertices) {
        final Map<Integer, List<Integer>> adjacencyMap = new HashMap<>();
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
                return Graph.N;
            }

            @Override
            public Collection<Integer> adjacentVertices(int i) {
                return adjacencyMap.get(i);
            }

            @Override
            public Collection<Integer> vertices() {
                return vertices;
            }
        };
    }

    /**
     * compute the number of edges in a graph
     * @param graph a graph
     * @return number of edges( e_ij e_ji are seen as two different edges )
     */
    public static int edgesCountOf(BaseGraph graph) {
        int result = 0;
        for (int vertexIndex : graph.vertices()) {
            result += graph.adjacentVertices(vertexIndex).size();
        }
        return result;
    }

    public static BaseGraph fullConnectedGraph(final List<Point3d> vertices) {
        final List<Integer> vt = new Vector<>();
        for (int i = 0; i < vertices.size(); i ++) vt.add(i);
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                return vertices.get(i).distance(vertices.get(j));
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return vt;
            }

            @Override
            public Collection<Integer> vertices() {
                return vt;
            }
        };
    }


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
                Pair<Integer, Integer> p = new Pair<>(i, j);
                if (knnEdges.contains(p)) {
                    return vertices.get(i).distance(vertices.get(j));
                }
                return Double.POSITIVE_INFINITY;
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return knnIndicesList.get(i);
            }

            @Override
            public Collection<Integer> vertices() {
                return verticesIndices;
            }
        };
    }

    @SuppressWarnings("Duplicates")
    private static List<List<Integer>> adjacentMatrix2List(final int[][] adjacency) {
        List<List<Integer>> adjacencies = new Vector<>();
        for (int[] adjacencyArray : adjacency) {
            List<Integer> vec = new Vector<>();
            for (int index : adjacencyArray) vec.add(index);
            adjacencies.add(vec);
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


    public static BaseGraph graph(final double[][]edges, final int[][]adjacency) {
        final List<List<Integer>> adjacencies = adjacentMatrix2List(adjacency);
        final List<Integer> vertices = PcuCommonUtil.incrementalIntegerList(edges.length);
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                return edges[i][j];
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return adjacencies.get(i);
            }

            @Override
            public Collection<Integer> vertices() {
                return vertices;
            }
        };
    }

    public static Graph knnGraph2(List<int[]> knnIndices, List<Point3d> data) {
        DirectedGraph graph = new DirectedGraph();
        for (int i = 0; i < knnIndices.size(); i ++) graph.addVertex(i);
        for (int i = 0; i < knnIndices.size(); i ++) {
            int[] indices = knnIndices.get(i);
            for (int index : indices) {
                double dis = data.get(i).distance(data.get(index));
                graph.addEdge(i, index, dis);
                graph.addEdge(index, i, dis);
            }
        }
        return graph;
    }

    public static BaseGraph empty() {
        final List<Integer> vertices = new ArrayList<>();
        return new BaseGraph() {
            @Override
            public double edgeWeight(int i, int j) {
                return BaseGraph.N;
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return new Vector<>();
            }

            @Override
            public Collection<Integer> vertices() {
                return vertices;
            }
        };
    }
}
