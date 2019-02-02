package cn.jimmiez.pcu.alg.projector;

import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class WeightedLOProjectorTest {

    @Test
    public void testProject() {
        Random r = new Random(System.currentTimeMillis());
        List<Point3d> points = new ArrayList<>();
        points.add(new Point3d());
        points.add(new Point3d());



        // test null ptr 1
        try {
            new WeightedLocallyOptimalProjector(null);
            fail("Should fail with a null pointer exception.");
        } catch (NullPointerException e){}

        // test null ptr 2
        try {
            WeightedLocallyOptimalProjector wlop = new WeightedLocallyOptimalProjector(new ArrayList<Point3d>());
            wlop.project(null, 1);
            fail("Should fail with a null pointer exception.");
        } catch (NullPointerException e){}

        // test invalid iteration number
        try {
            WeightedLocallyOptimalProjector wlop = new WeightedLocallyOptimalProjector(new ArrayList<Point3d>());
            wlop.project(points, -1);
            fail("Should fail with an illegal argument exception.");
        } catch (IllegalArgumentException e){}

        points.clear();
        int iter = 10;
        WeightedLocallyOptimalProjector wlop = new WeightedLocallyOptimalProjector(points);

        // test project points onto empty points set
        List<Point3d> toBeProjected = new ArrayList<>();
        for (int i = 0; i < 15; i ++) toBeProjected.add(new Point3d(r.nextDouble(), r.nextDouble(), r.nextDouble()));
        wlop.project(toBeProjected, iter);

        // test project points onto points set that has 5 points
        for (int i = 0; i < 5; i ++) points.add(new Point3d(r.nextDouble(), r.nextDouble(), r.nextDouble()));
        wlop = new WeightedLocallyOptimalProjector(points);
        wlop.project(toBeProjected, iter);
        for (Point3d p : toBeProjected) {
            assertFalse(Double.isNaN(p.x));
            assertFalse(Double.isNaN(p.y));
            assertFalse(Double.isNaN(p.z));
        }

    }
}
