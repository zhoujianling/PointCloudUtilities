package cn.jimmiez.pcu.common.graphics.curve;

import cn.jimmiez.pcu.common.graph.EntityGraph;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.vecmath.Point3d;

import static org.junit.Assert.*;
import static cn.jimmiez.pcu.CommonAssertions.*;

public class CardinalInterpolatorTest {

    private static final double COMPARE_DOUBLE_TOLERANCE = 1E-5;

    @Test
    public void testInterpolate() throws Exception {

        CardinalInterpolator interpolator = new CardinalInterpolator();
        EntityGraph<Point3d> graph = new EntityGraph<>(false);

        // test on empty graph, nothing happen
        interpolator.interpolate(graph);

        // new some points
        Point3d p1 = new Point3d();
        Point3d p2 = new Point3d(3, 4, 5);
        Point3d p3 = new Point3d(4, 4.5, 7);
        Point3d p4 = new Point3d(6, 8, 9);
        Point3d p5 = new Point3d(7, 12.3, 11);
        Point3d p6 = new Point3d(7.9, 13.7, 11.3);
        Point3d p7 = new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
        Point3d p8 = new Point3d(Double.NaN, 0, Double.NaN);
        // test on graph that has one vertex, nothing happen
        int v1 = graph.addVertex(p1);
        interpolator.interpolate(graph);
        assertSamePoint(p1, graph.getVertex(v1));

        // test on graph that has two vertex, nothing happen
        int v2 = graph.addVertex(p2);
        graph.addEdge(v1, v2, p1.distance(p2));
        interpolator.interpolate(graph);
        assertSamePoint(p1, graph.getVertex(v1));
        assertSamePoint(p2, graph.getVertex(v2));
        assertEquals(2 + interpolator.getNum(), graph.vertices().size());

        // test specify the illegal number
        try {
            new CardinalInterpolator(0);
            fail("should throw exception");
        } catch (IllegalArgumentException e) {}

        // test specify the param number
        int n = 6;
        interpolator = new CardinalInterpolator(n);
        graph = new EntityGraph<>(false);
        v1 = graph.addVertex(p1);
        v2 = graph.addVertex(p2);
        int v3 = graph.addVertex(p3);
        int v4 = graph.addVertex(p4);
        int v5 = graph.addVertex(p5);
        graph.addEdge(v1, v2, p1.distance(p2));
        graph.addEdge(v2, v3, p2.distance(p3));
        graph.addEdge(v3, v4, p3.distance(p4));
        graph.addEdge(v4, v5, p4.distance(p5));
        interpolator.interpolate(graph);
        assertSamePoint(p1, graph.getVertex(v1));
        assertSamePoint(p2, graph.getVertex(v2));
        assertSamePoint(p3, graph.getVertex(v3));
        assertSamePoint(p4, graph.getVertex(v4));
        assertSamePoint(p5, graph.getVertex(v5));
        assertEquals(5 + (5 - 1) * n, graph.vertices().size());

        // test on graph that contains cycle
        graph = new EntityGraph<>(false);
        v1 = graph.addVertex(p1);
        v2 = graph.addVertex(p2);
        v3 = graph.addVertex(p3);
        v4 = graph.addVertex(p4);
        v5 = graph.addVertex(p5);
        graph.addEdge(v1, v2, p1.distance(p2));
        graph.addEdge(v2, v3, p2.distance(p3));
        graph.addEdge(v3, v4, p3.distance(p4));
        graph.addEdge(v4, v5, p4.distance(p5));
        graph.addEdge(v5, v1, p5.distance(p1));
        interpolator.interpolate(graph);
        assertEquals(5 + 5 * n, graph.vertices().size());
        for (int vi : graph.vertices()) {
            Point3d pi = graph.getVertex(vi);
            assertFalse(Double.isNaN(pi.x));
            assertFalse(Double.isNaN(pi.y));
            assertFalse(Double.isNaN(pi.z));
        }

        // test on graph that contains two connected components
        graph = new EntityGraph<>(false);
        v1 = graph.addVertex(p1);
        v2 = graph.addVertex(p2);
        v3 = graph.addVertex(p3);
        v4 = graph.addVertex(p4);
        v5 = graph.addVertex(p5);
        graph.addEdge(v1, v2, p1.distance(p2));
        graph.addEdge(v2, v3, p2.distance(p3));
        graph.addEdge(v4, v5, p4.distance(p5));
        interpolator.interpolate(graph);
        assertEquals(5 + 3 * n, graph.vertices().size());

        // test on binary tree
        graph = new EntityGraph<>(false);
        v1 = graph.addVertex(p1);
        v2 = graph.addVertex(p2);
        v3 = graph.addVertex(p3);
        v4 = graph.addVertex(p4);
        v5 = graph.addVertex(p5);
        int v6 = graph.addVertex(p6);
        graph.addEdge(v1, v2, p1.distance(p2));
        graph.addEdge(v2, v3, p2.distance(p3));
        graph.addEdge(v3, v4, p3.distance(p4));
        graph.addEdge(v2, v5, p2.distance(p5));
        graph.addEdge(v5, v6, p5.distance(p6));
        interpolator.interpolate(graph);
        assertEquals(6 + 5 * n, graph.vertices().size());

        // test on graph that has INF points
        graph = new EntityGraph<>(false);
        v5 = graph.addVertex(p5);
        v6 = graph.addVertex(p6);
        int v7 = graph.addVertex(p7);
        graph.addEdge(v5, v6, p5.distance(p6));
        graph.addEdge(v6, v7, p6.distance(p7));
        interpolator.interpolate(graph);
        assertEquals(3 +  n, graph.vertices().size());

        // test on graph that has NaN points
        graph = new EntityGraph<>(false);
        v5 = graph.addVertex(p5);
        v6 = graph.addVertex(p6);
        int v8 = graph.addVertex(p8);
        graph.addEdge(v5, v6, p5.distance(p6));
        graph.addEdge(v6, v8, p6.distance(p8));
        interpolator.interpolate(graph);
        assertEquals(3 +  n, graph.vertices().size());

        // test on graph whose vertices all have invalid positions
        graph = new EntityGraph<>(false);
        v7 = graph.addVertex(p7);
        v8 = graph.addVertex(p8);
        graph.addEdge(v7, v8, p7.distance(p8));
        interpolator.interpolate(graph);
        assertEquals(2, graph.vertices().size());
    }

}