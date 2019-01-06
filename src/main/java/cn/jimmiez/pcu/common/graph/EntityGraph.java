package cn.jimmiez.pcu.common.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Entity graph here is a graph whose vertices are Java objects.
 */
public class EntityGraph<E> implements BaseGraph {

    protected Map<Integer, E> values = new HashMap<>();

    private AtomicInteger idGenerator = new AtomicInteger();

    private Graph delegate;

    public EntityGraph(boolean directed) {
        if (directed) {
            delegate = new DirectedGraph();
        } else {
            delegate = new UndirectedGraph();
        }
    }


    public int addVertex(E e) {
        int id = idGenerator.incrementAndGet();
        values.put(id, e);
        delegate.addVertex(id);
        return id;
    }

    public void removeVertex(int id) {
        values.remove(id);
        delegate.removeVertex(id);
    }

    public void removeVertex(E e) {
        for (int key : values.keySet()) {
            if (values.get(key) == e) {
                this.removeVertex(key); return;
            }
        }
        System.err.println("Skeleton::removeNode() Cannot find the point.");
    }

    public E getVertex(int id) {
        return values.get(id);
    }

    public void addEdge(int i, int j, double weight) {
        delegate.addEdge(i, j, weight);
    }

    public void removeEdge(int i, int j) {
        delegate.removeEdge(i, j);
    }

    public void clear() {
        values.clear();
        delegate.clear();
    }

    @Override
    public double edgeWeight(int i, int j) {
        return delegate.edgeWeight(i, j);
    }

    @Override
    public Collection<Integer> adjacentVertices(int i) {
        return delegate.adjacentVertices(i);
    }

    @Override
    public Collection<Integer> vertices() {
        return delegate.vertices();
    }

}
