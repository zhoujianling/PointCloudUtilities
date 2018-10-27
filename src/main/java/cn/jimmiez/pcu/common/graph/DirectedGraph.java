package cn.jimmiez.pcu.common.graph;

import java.util.HashMap;
import java.util.Map;

public class DirectedGraph implements Graph{

    private Map<Integer, Map<Integer, Double>> edges = new HashMap<>();


    @Override
    public void addEdge(int vi, int vj, double weight) {
        if (vi >= edges.size() || vj >= edges.size()) {
            throw new IllegalArgumentException("Invalid vertex index.");
        }
        edges.get(vi).put(vj, weight);
    }

    @Override
    public void removeEdge(int vi, int vj) {
        if (vi >= edges.size() || vj >= edges.size()) {
            throw new IllegalArgumentException("Invalid vertex index.");
        }
        edges.get(vi).remove(vj);
    }

    @Override
    public void updateEdge(int vi, int vj, double weight) {
        addEdge(vi, vj, weight);
    }

    @Override
    public void addVertex(int vi) {
        edges.put(vi, new HashMap<Integer, Double>());
    }

    @Override
    public void removeVertex(int vi) {
        edges.remove(vi);
    }

    @Override
    public Iterable<Integer> vertices() {
        return edges.keySet();
    }

    @Override
    public double edgeWeight(int i, int j) {
        Double weight = edges.get(i).get(j);
        if (weight == null) {
            return N;
        }
        return weight;
    }

    @Override
    public int verticesCount() {
        return edges.size();
    }

    @Override
    public Iterable<Integer> adjacentVertices(int i) {
        return edges.get(i).keySet();
    }
}
