package cn.jimmiez.pcu.common.graph;

import javafx.util.Pair;

import javax.vecmath.Point3d;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Graphs {

    public static List<List<Integer>> connectedComponents(Graph graph) {
        //TODO  obtain connected components of a graph
        return null;
    }

    public static Graph fullConnectedGraph(final List<Point3d> vertices) {
        return new Graph() {
            @Override
            public double edgeWeight(int i, int j) {
                return vertices.get(i).distance(vertices.get(j));
            }

            @Override
            public int verticesCount() {
                return vertices.size();
            }
        };
    }


    public static Graph knnGraph(final List<Point3d> vertices, List<int[]> knnIndices) {
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
        };
    }
}
