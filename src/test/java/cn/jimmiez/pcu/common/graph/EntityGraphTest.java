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

//    @Test
//    public void testGetVertex() throws Exception {
//
//    }
//
//    @Test
//    public void testAddEdge() throws Exception {
//    }
//
//    @Test
//    public void testRemoveEdge() throws Exception {
//    }
//
//    @Test
//    public void testClear() throws Exception {
//    }

}