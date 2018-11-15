package cn.jimmiez.pcu.common.graph;


import cn.jimmiez.pcu.model.Pair;
import cn.jimmiez.pcu.util.PcuCommonUtil;

import javax.vecmath.Point3d;
import java.util.*;

public class Graphs {

    public static List<List<Integer>> connectedComponents(GraphStatic graph) {
        List<List<Integer>> subGraphs = new Vector<>();
        boolean[] visited = new boolean[graph.vertices().size()];
        for (int i = 0; i < graph.vertices().size(); i ++) visited[i] = false;
        for (int i = 0; i < graph.vertices().size(); i ++) {
//            System.out.println("i = " + i);
            if (visited[i]) continue;
            List<Integer> subGraph = new ArrayList<>();

            List<Integer> visitQueue = new Vector<>();
            visitQueue.add(i);
            for (int ptr = 0; ptr < visitQueue.size(); ptr ++) {
                int visiting = visitQueue.get(ptr);
                if (! visited[visiting]) {
                    visited[visiting] = true;
                    subGraph.add(visiting);
                    for (int adjacentVertex : graph.adjacentVertices(visiting)) visitQueue.add(adjacentVertex);
                }
            }
            subGraphs.add(subGraph);
        }
        return subGraphs;
    }

    /**
     * compute the number of edges in a graph
     * @param graph a graph
     * @return number of edges( e_ij e_ji are seen as two different edges )
     */
    public static int edgesCountOf(GraphStatic graph) {
        int result = 0;
        for (int vertexIndex : graph.vertices()) {
            result += graph.adjacentVertices(vertexIndex).size();
        }
        return result;
    }

    public static GraphStatic fullConnectedGraph(final List<Point3d> vertices) {
        final List<Integer> vt = new Vector<>();
        for (int i = 0; i < vertices.size(); i ++) vt.add(i);
        return new GraphStatic() {
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


    public static GraphStatic knnGraph(final List<Point3d> vertices, final List<int[]> knnIndices) {
        final Set<Pair<Integer, Integer>> knnEdges = new HashSet<>();
        final List<Integer> verticesIndices = new ArrayList<>();
        for (int i = 0; i < knnIndices.size(); i ++) {
            verticesIndices.add(i);
            for (int j : knnIndices.get(i)) {
                knnEdges.add(new Pair<>(i, j));
            }
        }
        final List<List<Integer>> knnIndicesList = adjacentMatrix2List(knnIndices);
        return new GraphStatic() {
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


    public static GraphStatic graph(final double[][]edges, final int[][]adjacency) {
        final List<List<Integer>> adjacencies = adjacentMatrix2List(adjacency);
        final List<Integer> vertices = PcuCommonUtil.incrementalIntegerList(edges.length);
        return new GraphStatic() {
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

    public static GraphStatic empty() {
        final List<Integer> vertices = new ArrayList<>();
        return new GraphStatic() {
            @Override
            public double edgeWeight(int i, int j) {
                return GraphStatic.N;
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
