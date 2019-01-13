package cn.jimmiez.pcu.common.graph;

import org.junit.Test;

import javax.vecmath.Point3d;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class EntityGraphTest {

    @Test
    public void testAddVertex() throws Exception {
        // test directed graph
        EntityGraph<Point3d> eg = new EntityGraph<>(true);
        assertEquals(0, eg.vertices().size());

        eg.addVertex(new Point3d());
        assertEquals(1, eg.vertices().size());
        eg.addVertex(new Point3d());
        assertEquals(2, eg.vertices().size());

        // test undirected graph
        eg = new EntityGraph<>(false);
        assertEquals(0, eg.vertices().size());

        int vn = 10038;
        for (int i = 0; i < vn; i ++)
            eg.addVertex(new Point3d());
        assertEquals(vn, eg.vertices().size());

        // test null ptr
        try {
            eg.addVertex(null);
            fail();
        } catch (NullPointerException e) {}
    }

    @Test
    public void testRemoveVertex() throws Exception {
        // test directed graph
        EntityGraph<Point3d> eg = new EntityGraph<>(true);
        assertEquals(0, eg.vertices().size());

        // test remove un-existing vertex
        eg.removeVertex(new Point3d());
        assertEquals(0, eg.vertices().size());

        Point3d p = new Point3d();
        eg.addVertex(p);
        assertEquals(1, eg.vertices().size());
        eg.removeVertex(p);
        assertEquals(0, eg.vertices().size());
        // test remove non-existing object
        eg.removeVertex(p);
        assertEquals(0, eg.vertices().size());

        // test undirected graph
        eg = new EntityGraph<>(false);
        assertEquals(0, eg.vertices().size());

        p = new Point3d(1, 2, 3);
        eg.addVertex(p);
        assertEquals(1, eg.vertices().size());
        eg.removeVertex(p);
        assertEquals(0, eg.vertices().size());

        // test null ptr
        try {
            eg.removeVertex(null);
            fail();
        } catch (NullPointerException e) {}

    }

    @Test
    public void testRemoveVertexByIndex() throws Exception {
        // test directed graph
        EntityGraph<Point3d> eg = new EntityGraph<>(true);

        // test remove un-existing vertex
        int vi = eg.addVertex(new Point3d());
        eg.removeVertex(vi);
        assertEquals(0, eg.vertices().size());
        // test remove non-existing vertex
        eg.removeVertex(vi);
        assertEquals(0, eg.vertices().size());


        // test undirected graph
        eg = new EntityGraph<>(false);
        assertEquals(0, eg.vertices().size());

        Point3d p = new Point3d(1, 2, 3);
        eg.addVertex(p);
        assertEquals(1, eg.vertices().size());
        eg.removeVertex(p);
        assertEquals(0, eg.vertices().size());


        // test remove invalid index
        eg.removeVertex(Integer.MIN_VALUE);
    }

    @Test
    public void testGetVertex() throws Exception {
        // test undirected graph
        EntityGraph<Point3d> eg = new EntityGraph<>(false);

        Point3d p = new Point3d();
        Point3d p2 = new Point3d();
        Point3d p3 = new Point3d();
        assertNotEquals(p, eg.getVertex(0));
        assertNull(eg.getVertex(0));

        int vi = eg.addVertex(p);
        int vj = eg.addVertex(p2);
        int vk = eg.addVertex(p3);
        assertEquals(p, eg.getVertex(vi));
        assertEquals(p2, eg.getVertex(vj));
        assertEquals(p3, eg.getVertex(vk));

        // test directed graph
        eg = new EntityGraph<>(true);

        Point3d p4 = new Point3d();
        assertNotEquals(p, eg.getVertex(0));
        assertNull(eg.getVertex(0));

        int vl = eg.addVertex(p4);
        assertEquals(p4, eg.getVertex(vl));

    }

    @Test
    public void testAddEdge() throws Exception {
        // test undirected graph
        EntityGraph<Point3d> eg = new EntityGraph<>(false);

        int v1 = eg.addVertex(new Point3d());
        int v2 = eg.addVertex(new Point3d());
        eg.addEdge(v1, v2, 1.5);
        assertEquals(1.5, eg.edgeWeight(v1, v2), 1E-5);

        // test directed graph
        eg = new EntityGraph<>(true);

        int v3 = eg.addVertex(new Point3d());
        int v4 = eg.addVertex(new Point3d());
        eg.addEdge(v3, v4, 2.3);
        assertEquals(1, eg.adjacentVertices(v3).size());
        assertEquals(0, eg.adjacentVertices(v4).size());
        assertEquals(2.3, eg.edgeWeight(v3, v4), 1E-5);
        assertEquals(Graph.N, eg.edgeWeight(v4, v3), 1E-5);
        try {
            eg.addEdge(v3, v4, -5.1);
            fail();
        } catch (IllegalArgumentException e){}
    }

    @Test
    public void testRemoveEdge() throws Exception {
        // test undirected graph
        EntityGraph<Point3d> eg = new EntityGraph<>(false);

        int v1 = eg.addVertex(new Point3d());
        int v2 = eg.addVertex(new Point3d());
        eg.addEdge(v1, v2, 1.5);
        assertEquals(1, eg.adjacentVertices(v1).size());
        eg.removeEdge(v1, v2);
        assertEquals(0, eg.adjacentVertices(v1).size());
        assertEquals(0, eg.adjacentVertices(v2).size());
        assertEquals(Graph.N, eg.edgeWeight(v1, v2), 1E-5);

        // test directed graph
        eg = new EntityGraph<>(true);

        int v3 = eg.addVertex(new Point3d());
        int v4 = eg.addVertex(new Point3d());
        eg.addEdge(v3, v4, 3.5);
        eg.addEdge(v4, v3, 3.5);
        assertEquals(1, eg.adjacentVertices(v3).size());
        assertEquals(1, eg.adjacentVertices(v4).size());
        eg.removeEdge(v3, v4);
        assertEquals(0, eg.adjacentVertices(v3).size());
        assertEquals(1, eg.adjacentVertices(v4).size());
        assertEquals(Graph.N, eg.edgeWeight(v3, v4), 1E-5);
    }

    @Test
    public void testClear() throws Exception {
        EntityGraph<Point3d> graph = new EntityGraph<>(true);
        assertEquals(0, graph.vertices().size());
        // test clear a graph with no vertex
        graph.clear();
        assertEquals(0, graph.vertices().size());

        graph.addVertex(new Point3d());
        graph.clear();
        assertEquals(0, graph.vertices().size());

        // test undirected graph
        graph = new EntityGraph<>(false);
        // test clear a graph with no vertex
        graph.clear();
        assertEquals(0, graph.vertices().size());

        graph = new EntityGraph<>(false);
        Point3d p1 = new Point3d();
        Point3d p2 = new Point3d();
        int vi = graph.addVertex(p1);
        int vj = graph.addVertex(p2);
        graph.addEdge(vi, vj, 1);
        graph.clear();
        assertEquals(0, graph.vertices().size());
        int vk = graph.addVertex(p2);
        assertEquals(0, graph.adjacentVertices(vk).size());
    }

}