package cn.jimmiez.pcu.common.graph;


import cn.jimmiez.pcu.model.Pair;

import java.util.*;

public class ShortestPath {

    /**
     * dijkstra algorithm
     * @param graph abstract graph
     * @param rootIndex index of root vertex
     * @return list of pairs, for each pair, the key is the path, the value is length of shortest path
     */
    public static List<Pair<List<Integer>, Weight>> dijkstra(GraphStatic graph, int rootIndex) {
        List<Pair<List<Integer>, Weight>> result = new Vector<>();
        if (rootIndex < 0 || rootIndex >= graph.vertices().size()) {
            throw new IllegalArgumentException("Invalid root index");
        }
        Set<Integer> sSet = new HashSet<>();
        Set<Integer> sExcluded = new HashSet<>();

        for (int i = 0; i < graph.vertices().size(); i ++) {
            sExcluded.add(i);
            List<Integer> path = new Vector<>();
            path.add(i); // add the index of start point
            result.add(new Pair<>(path, new Weight(graph.edgeWeight(rootIndex, i))));
        }

        while (sSet.size() < graph.vertices().size()) {
//            System.out.println("set size: " + sSet.size());
            int nearestVertexIndex = -1;
            double shortestPathLen = Double.POSITIVE_INFINITY;
            for (int index : sExcluded) {
                if (result.get(index).getValue().val() < shortestPathLen) {
                    shortestPathLen = result.get(index).getValue().val();
                    nearestVertexIndex = index;
                }
            }
            sSet.add(nearestVertexIndex);
            sExcluded.remove(nearestVertexIndex);
            result.get(nearestVertexIndex).getKey().add(nearestVertexIndex);
            for (int index : sExcluded) {
                double weightSum = shortestPathLen + graph.edgeWeight(nearestVertexIndex, index);
                if (weightSum < result.get(index).getValue().val()) {
                    result.get(index).getKey().add(nearestVertexIndex);
                    result.get(index).getValue().set(weightSum);
                }
            }
        }

        return result;
    }
}
