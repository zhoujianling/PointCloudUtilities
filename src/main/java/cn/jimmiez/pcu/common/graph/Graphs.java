package cn.jimmiez.pcu.common.graph;

import javafx.util.Pair;

import javax.vecmath.Point3d;
import java.util.*;

public class Graphs {

    public static List<List<Integer>> connectedComponents(Graph graph) {
        List<List<Integer>> subGraphs = new Vector<>();
        boolean[] visited = new boolean[graph.verticesCount()];
        for (int i = 0; i < graph.verticesCount(); i ++) visited[i] = false;
        for (int i = 0; i < graph.verticesCount(); i ++) {
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
        final int[] vt = new int[vertices.size()];
        for (int i = 0; i < vertices.size(); i ++) vt[i] = i;
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
            public int[] adjacentVertices(int i) {
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
            public int[] adjacentVertices(int i) {
                return knnIndices.get(i);
            }
        };
    }
}
