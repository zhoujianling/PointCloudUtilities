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

    /**
     * Construct the entity graph
     * @param directed if true, construct a directed graph, false, an undirected graph
     */
    public EntityGraph(boolean directed) {
        if (directed) {
            delegate = new DirectedGraph();
        } else {
            delegate = new UndirectedGraph();
        }
    }

    /**
     * add a vertex
     * @param e the value of vertex
     * @return the index of newly-added vertex
     */
    public int addVertex(E e) {
        if (e == null) throw new NullPointerException("The value of vertex cannot be null.");
        int id = idGenerator.incrementAndGet();
        values.put(id, e);
        delegate.addVertex(id);
        return id;
    }

    /**
     * remove the vertex if this graph contains this vertex.
     * If not contain, the graph remain unchanged.
     * @param id the index of the vertex which is to be removed
     */
    public void removeVertex(int id) {
        values.remove(id);
        delegate.removeVertex(id);
    }

    /**
     * remove the vertex if this graph contains this vertex.
     * If not contain, the graph remain unchanged.
     * @param e the value of the vertex which is to be removed
     */
    public void removeVertex(E e) {
        if (e == null) throw new NullPointerException("The value of vertex cannot be null.");
        for (int key : values.keySet()) {
            if (values.get(key) == e) {
                this.removeVertex(key); return;
            }
        }
        System.err.println("Skeleton::removeNode() Cannot find the point.");
    }

    /**
     * get the value of vertex by id
     * @param id the index of vertex
     * @return the value of vertex
     */
    public E getVertex(int id) {
        return values.get(id);
    }

    /**
     * add an edge to the graph
     * @param i the index of vertex i, if vi equals vj, do nothing
     * @param j the index of vertex j, if vj equals vi, no nothing
     * @param weight the weight of the edge to be added, must be non-negative
     */
    public void addEdge(int i, int j, double weight) {
        delegate.addEdge(i, j, weight);
    }

    /**
     * remove the edge that is specified by ordered index pair {@literal <i, j>}
     * @param i the index of vertex i, if vi equals vj, do nothing
     * @param j the index of vertex j, if vj equals vi, no nothing
     */
    public void removeEdge(int i, int j) {
        delegate.removeEdge(i, j);
    }

    /**
     * clear the vertices and edges in the graph
     */
    public void clear() {
        values.clear();
        delegate.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double edgeWeight(int i, int j) {
        return delegate.edgeWeight(i, j);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Integer> adjacentVertices(int i) {
        return delegate.adjacentVertices(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<Integer> vertices() {
        return delegate.vertices();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isDirected() {
        return delegate.isDirected();
    }

}
