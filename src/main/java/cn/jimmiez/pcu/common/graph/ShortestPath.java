package cn.jimmiez.pcu.common.graph;

import javafx.util.Pair;
import java.util.*;

public class ShortestPath {

    /**
     * dijkstra algorithm
     * @param graph abstract graph
     * @param rootIndex index of root vertex
     * @return list of pairs, for each pair, the key is the path, the value is length of shortest path
     */
    public static List<Pair<List<Integer>, Double>> dijkstra(Graph graph, int rootIndex) {
        List<Pair<List<Integer>, Double>> result = new Vector<>();
        if (rootIndex < 0 || rootIndex >= graph.verticesCount()) {
            throw new IllegalArgumentException("Invalid root index");
        }
        Set<Integer> sSet = new HashSet<>();
        Set<Integer> sExcluded = new HashSet<>();

        for (int i = 0; i < graph.verticesCount(); i ++) {
            sExcluded.add(i);
            result.add(new Pair<List<Integer>, Double>(new Vector<Integer>(), graph.edgeWeight(rootIndex, i)));
        }

        while (sSet.size() <= graph.verticesCount()) {
            int nearestVertexIndex = sExcluded.iterator().next();
            double shortestPathLen = Double.POSITIVE_INFINITY;
            for (int index : sExcluded) {
                if (result.get(index).getValue() < shortestPathLen) {
                    shortestPathLen = result.get(index).getValue();
                    nearestVertexIndex = index;
                }
            }
            sSet.add(nearestVertexIndex);
            sExcluded.remove(nearestVertexIndex);
            result.get(nearestVertexIndex).getKey().add(nearestVertexIndex);
            for (int index : sExcluded) {
                double weightSum = shortestPathLen + graph.edgeWeight(nearestVertexIndex, index);
                if (weightSum < graph.edgeWeight(rootIndex, index)) {
                    List<Integer> path = result.get(index).getKey();
                    path.add(nearestVertexIndex);
                    result.add(index, new Pair<>(path, weightSum));
                }
            }
        }

        return result;
    }
}
