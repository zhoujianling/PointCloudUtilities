package cn.jimmiez.pcu;

import javax.vecmath.Point3d;

import static org.junit.Assert.*;

public class CommonAssertions {

    private static final double COMPARE_DOUBLE_TOLERANCE = 1E-5;

    public static void assertLessThan(double v1, double v2) {
        String message = "The value " + v1 + " should be less than " + v2 + ".";
        assertTrue(message, v1 <= v2);
    }

    public static void assertLessEqualThan(double v1, double v2) {
        assertLessEqualThan(v1, v2, COMPARE_DOUBLE_TOLERANCE);
    }

    public static void assertLessEqualThan(double v1, double v2, double tolerance) {
        String message = "The value " + v1 + " should be less than " + v2 + ".";
        assertTrue(message, v1 < v2 || Math.abs(v1 - v2) <= tolerance);
    }

    public static void assertLessThan(int v1, int v2) {
        String message = "The value " + v1 + " should be less than " + v2 + ".";
        assertTrue(message, v1 < v2);
    }

    public static void assertLessEqualThan(int v1, int v2) {
        String message = "The value " + v1 + " should be not bigger than " + v2 + ".";
        assertTrue(message, v1 <= v2);
    }

    public static void assertGreaterThan(int v1, int v2) {
        String message = "The value " + v1 + " should be larger than " + v2 + ".";
        assertTrue(message, v1 > v2);
    }

    public static void assertGreaterEqualThan(int v1, int v2) {
        String message = "The value " + v1 + " should be not less than " + v2 + ".";
        assertTrue(message, v1 >= v2);
    }

    public static void assertGreaterEqualThan(double v1, double v2) {
        assertGreaterEqualThan(v1, v2, COMPARE_DOUBLE_TOLERANCE);
    }

    public static void assertGreaterEqualThan(double v1, double v2, double tolerance) {
        String message = "The value " + v1 + " should be less than " + v2 + ".";
        assertTrue(message, v1 > v2 || Math.abs(v1 - v2) >= tolerance);
    }

    public static void assertSamePoint(Point3d p1, Point3d p2) {
        assertEquals(p1.x, p2.x, COMPARE_DOUBLE_TOLERANCE);
        assertEquals(p1.y, p2.y, COMPARE_DOUBLE_TOLERANCE);
        assertEquals(p1.z, p2.z, COMPARE_DOUBLE_TOLERANCE);
    }

}
