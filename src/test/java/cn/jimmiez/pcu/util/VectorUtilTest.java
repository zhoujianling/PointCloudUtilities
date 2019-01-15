package cn.jimmiez.pcu.util;

import org.junit.Test;

import javax.vecmath.Point3d;

import static org.junit.Assert.*;

public class VectorUtilTest {

    @Test
    public void testValidPoint() throws Exception {
        Point3d p0 = new Point3d(0, 0, 0);
        assertTrue(VectorUtil.validPoint(p0));

        Point3d p1 = new Point3d(1, 2, 3);
        assertTrue(VectorUtil.validPoint(p1));

        Point3d p2 = new Point3d(Double.NaN, 2, 3);
        assertFalse(VectorUtil.validPoint(p2));

        Point3d p3 = new Point3d(Double.NaN, Double.NaN, 3);
        assertFalse(VectorUtil.validPoint(p3));

        Point3d p4 = new Point3d(Double.NaN, Double.NaN, Double.NaN);
        assertFalse(VectorUtil.validPoint(p4));

        Point3d p5 = new Point3d(-1, Double.NaN, 3);
        assertFalse(VectorUtil.validPoint(p5));

        Point3d p6 = new Point3d(-1, -5, Double.NaN);
        assertFalse(VectorUtil.validPoint(p6));

        Point3d p7 = new Point3d(Double.NaN, Double.POSITIVE_INFINITY, 3);
        assertFalse(VectorUtil.validPoint(p7));

        Point3d p8 = new Point3d(3, Double.POSITIVE_INFINITY, 3);
        assertFalse(VectorUtil.validPoint(p8));

        Point3d p9 = new Point3d(3, 2, Double.NEGATIVE_INFINITY);
        assertFalse(VectorUtil.validPoint(p9));
    }

}