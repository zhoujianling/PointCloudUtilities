package cn.jimmiez.pcu.common.graphics.shape;

import org.junit.Test;

import javax.vecmath.Point3d;

import static org.junit.Assert.*;

public class BoxTest {

    @Test
    public void testBox() throws Exception {
        // test create boxes
        new Box(new Point3d(0.111, 0.3, 0), 0.5, 1, 0.9);
        new Box(new Point3d(3.111, 0.3, 0), Double.MAX_VALUE, 1, 0.9);
        new Box(new Point3d(0.111, 0.3, 0), Double.MIN_VALUE, 1, 0.9);

        try {
            new Box(new Point3d(0.111, 0.3, 0), Double.NaN, 1, 0.9);
            fail("should throw exceptions");
        } catch (IllegalArgumentException e) {}

        try {
            new Box(new Point3d(0.111, 0.3, 0), 0, Double.NaN, 0.9);
            fail("should throw exceptions");
        } catch (IllegalArgumentException e) {}

        try {
            new Box(new Point3d(0.111, 0.3, 0), 0, 3, -2);
            fail("should throw exceptions");
        } catch (IllegalArgumentException e) {}

        try {
            new Box(new Point3d(0.111, Double.NaN, 0), 0, 3, 2);
            fail("should throw exceptions");
        } catch (IllegalArgumentException e) {}
    }

    @Test
    public void testContains() throws Exception {
        Box box = new Box(new Point3d(0, 0, 0), 1, 1, 1);

        Point3d p = new Point3d(0.0, 0.0, 0.0);
        assertTrue(box.contains(p));

        p = new Point3d(0.5, 0.5, 0.5);
        assertTrue(box.contains(p));

        p = new Point3d(0.5, 0.5, 0.5);
        assertTrue(box.contains(p));

        p = new Point3d(0.99999, 0.99999, 0.99999);
        assertTrue(box.contains(p));

        p = new Point3d(1.0000000, 1.000000, 1.000000000001);
        assertTrue(box.contains(p));

        p = new Point3d(1.0000000, 1.000000, 1.200000000001);
        assertFalse(box.contains(p));

        p = new Point3d(-1.09999, 0.99999, 0.99999);
        assertFalse(box.contains(p));

        // test point that has NaN/Inf coordinates
        p = new Point3d(Double.POSITIVE_INFINITY, 1.000000, 1.000000000001);
        assertFalse(box.contains(p));

        p = new Point3d(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1.000000000001);
        assertFalse(box.contains(p));

        p = new Point3d(Double.NaN, 0.3, 1.000000000001);
        assertFalse(box.contains(p));


        // test other boxes
        box = new Box(new Point3d(0.111, 0.3, 0), 0.5, 1, 0.9);

        p = new Point3d(0.606, -.58, 0.66666);
        assertTrue(box.contains(p));

        p = new Point3d(0.66, -.78, 0.66666);
        assertFalse(box.contains(p));

        // test box whose extents are Inf
        box = new Box(new Point3d(0.111, 0.3, 0), Double.POSITIVE_INFINITY, 1, 0.9);

        p = new Point3d(0.606, -.58, 0.66666);
        assertTrue(box.contains(p));

        p = new Point3d(Double.MAX_VALUE, -.28, 0.66666);
        assertTrue(box.contains(p));

        p = new Point3d(Double.MIN_VALUE, -.28, 0.66666);
        assertTrue(box.contains(p));

        p = new Point3d(Double.NaN, -.28, 0.66666);
        assertFalse(box.contains(p));

        // test box whose center has Inf coordinates
        box = new Box(new Point3d(0.111, Double.NEGATIVE_INFINITY, 0), 2, 1, 0.9);
        p = new Point3d(0.111, Double.MIN_VALUE, 0.66666);
        assertFalse(box.contains(p));

        // test box whose center has Inf coordinates
        box = new Box(new Point3d(0.111, Double.NEGATIVE_INFINITY, 0), 2, 1, 0.9);
        p = new Point3d(0.111, Double.NEGATIVE_INFINITY, 0.66666);
        assertTrue(box.contains(p));

        box = new Box(new Point3d(0.111, Double.POSITIVE_INFINITY, 0), 2, 1, 0.9);
        p = new Point3d(0.111, Double.NEGATIVE_INFINITY, 0.66666);
        assertFalse(box.contains(p));
    }


}