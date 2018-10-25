package cn.jimmiez.pcu.common.graph;

import javafx.util.Pair;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.*;

public class Graphs {


    public static List<List<Integer>> connectedComponents(Graph graph) {
        List<List<Integer>> subGraphs = new Vector<>();
        boolean[] visited = new boolean[graph.verticesCount()];
        for (int i = 0; i < graph.verticesCount(); i ++) visited[i] = false;
        for (int i = 0; i < graph.verticesCount(); i ++) {
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

    public static Graph fullConnectedGraph(final List<Point3d> vertices) {
        final List<Integer> vt = new Vector<>();
        for (int i = 0; i < vertices.size(); i ++) vt.add(i);
        return new Graph() {
            @Override
            public double edgeWeight(int i, int j) {
                return vertices.get(i).distance(vertices.get(j));
            }

            @Override
            public int verticesCount() {
                return vertices.size();
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return vt;
            }
        };
    }


    public static Graph knnGraph(final List<Point3d> vertices, final List<int[]> knnIndices) {
        final Set<Pair<Integer, Integer>> knnEdges = new HashSet<>();
        for (int i = 0; i < knnIndices.size(); i ++) {
            for (int j : knnIndices.get(i)) {
                knnEdges.add(new Pair<>(i, j));
            }
        }
        final List<List<Integer>> knnIndicesList = adjacentMatrix2List(knnIndices);
        return new Graph() {
            @Override
            public double edgeWeight(int i, int j) {
                Pair<Integer, Integer> p = new Pair<>(i, j);
                if (knnEdges.contains(p)) {
                    return vertices.get(i).distance(vertices.get(j));
                }
                return Double.POSITIVE_INFINITY;
            }

            @Override
            public int verticesCount() {
                return vertices.size();
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return knnIndicesList.get(i);
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


    public static Graph graph(final double[][]edges, final int[][]adjacency) {
        final List<List<Integer>> adjacencies = adjacentMatrix2List(adjacency);
        return new Graph() {
            @Override
            public double edgeWeight(int i, int j) {
                return edges[i][j];
            }

            @Override
            public int verticesCount() {
                return edges.length;
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return adjacencies.get(i);
            }
        };
    }

    public static Graph empty() {
        return new Graph() {
            @Override
            public double edgeWeight(int i, int j) {
                return Graph.N;
            }

            @Override
            public int verticesCount() {
                return 0;
            }

            @Override
            public List<Integer> adjacentVertices(int i) {
                return new Vector<>();
            }
        };
    }
}
