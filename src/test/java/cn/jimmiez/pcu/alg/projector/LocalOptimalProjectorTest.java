package cn.jimmiez.pcu.alg.projector;

import org.junit.Test;

import javax.vecmath.Point3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class LocalOptimalProjectorTest {

    @Test
    public void testProject() {
        Random r = new Random(System.currentTimeMillis());
        List<Point3d> points = new ArrayList<>();
        points.add(new Point3d());


        // test invalid number of iterations
        try {
            new LocallyOptimalProjector(points, 0);
            fail("Should fail with an illegal argument exception.");
        } catch (IllegalArgumentException e){}
        try {
            new LocallyOptimalProjector(points, -1);
            fail("Should fail with an illegal argument exception.");
        } catch (IllegalArgumentException e){}

        // test null ptr 1
        try {
            new LocallyOptimalProjector(null, 1);
            fail("Should fail with an illegal argument exception.");
        } catch (NullPointerException e){}

        // test null ptr 2
        try {
            LocallyOptimalProjector lop = new LocallyOptimalProjector(new ArrayList<Point3d>(), 1);
            lop.project(null);
            fail("Should fail with an illegal argument exception.");
        } catch (NullPointerException e){}

        points.clear();
        int iter = 10;
        LocallyOptimalProjector lop = new LocallyOptimalProjector(points, iter);

        // test project points onto empty points set
        List<Point3d> toBeProjected = new ArrayList<>();
        for (int i = 0; i < 15; i ++) toBeProjected.add(new Point3d(r.nextDouble(), r.nextDouble(), r.nextDouble()));
        List<Point3d> ps = lop.project(toBeProjected);
        assertNotNull(ps);
        assertEquals(toBeProjected.size(), ps.size());

        // test project points onto points set that has 5 points
        for (int i = 0; i < 5; i ++) points.add(new Point3d(r.nextDouble(), r.nextDouble(), r.nextDouble()));
        lop = new LocallyOptimalProjector(points, iter);
        ps = lop.project(toBeProjected);
        assertNotNull(ps);
        assertEquals(toBeProjected.size(), ps.size());
        for (Point3d p : ps) {
            assertFalse(Double.isNaN(p.x));
            assertFalse(Double.isNaN(p.y));
            assertFalse(Double.isNaN(p.z));
        }

        // test project points onto points set that Nan and Inf
//        points.add(new Point3d(Double.NaN, Double.NaN, Double.NaN));
//        ps = lop.project(toBeProjected);
//        assertNotNull(ps);
//        assertEquals(toBeProjected.size(), ps.size());
//        for (Point3d p : ps) {
//            assertFalse(Double.isNaN(p.x));
//            assertFalse(Double.isNaN(p.y));
//            assertFalse(Double.isNaN(p.z));
//        }
//
//        points.add(new Point3d(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY));
//        ps = lop.project(toBeProjected);
//        assertNotNull(ps);
//        assertEquals(toBeProjected.size(), ps.size());
//        for (Point3d p : ps) {
//            assertFalse(Double.isInfinite(p.x));
//            assertFalse(Double.isInfinite(p.y));
//            assertFalse(Double.isInfinite(p.z));
//        }

        // test project zero points onto points set
//        toBeProjected.clear();
//        ps = lop.project(toBeProjected);
//        assertNotNull(ps);
//        assertEquals(toBeProjected.size(), ps.size());

        // // TODO: 2019/1/15 Add test: input point set has NaN & INF
    }
}
