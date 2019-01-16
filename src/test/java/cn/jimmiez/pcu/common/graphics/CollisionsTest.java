package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.common.graphics.shape.Box;
import cn.jimmiez.pcu.common.graphics.shape.Sphere;
import org.junit.Test;

import javax.vecmath.Point3d;
import static org.junit.Assert.*;

public class CollisionsTest {

    @Test
    public void testBoxIntersectsSphere() {
        Box box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        Sphere sphere = new Sphere(new Point3d(0, 0, 0), 2);
        assertTrue(Collisions.intersect(box, sphere));

        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(0, 0, 0), 7);
        assertTrue(Collisions.intersect(box, sphere));

        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(11, 0, 0), 7);
        assertFalse(Collisions.intersect(box, sphere));

        // test sphere with invalid radius
        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(11, 0, 0), Double.NaN);
        assertFalse(Collisions.intersect(box, sphere));

        // test sphere with positive infinite radius
        box = new Box(new Point3d(0, 0, 0), 1, 4, 5);
        sphere = new Sphere(new Point3d(11, 0, 0), Double.POSITIVE_INFINITY);
        assertTrue(Collisions.intersect(box, sphere));

    }

    @Test
    public void testBoxContainsSphere() {
        Box box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        Sphere sphere = new Sphere(new Point3d(0, 0, 0), 2);
        assertTrue(Collisions.contains(box, sphere));

        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(0, 0, 0), 7);
        assertFalse(Collisions.contains(box, sphere));

        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(11, 0, 0), 7);
        assertFalse(Collisions.contains(box, sphere));

    }

    @Test
    public void testSphereContainsBox() {
        Box box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        Sphere sphere = new Sphere(new Point3d(0, 0, 0), 2);
        assertFalse(Collisions.contains(sphere, box));

        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(0, 0, 0), 8);
        assertTrue(Collisions.contains(sphere, box));

        box = new Box(new Point3d(-0.3, 0.2, -1.9), 3, 4, 5);
        sphere = new Sphere(new Point3d(0.5, -3.5, -4.5), 2);
        assertFalse(Collisions.contains(sphere, box));

        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(0, 3.5, 4.5), 12.48);
        assertTrue(Collisions.contains(sphere, box));

        // test sphere with positive infinite radius
        box = new Box(new Point3d(0, 0, 0), 3, 4, 5);
        sphere = new Sphere(new Point3d(0, 3.5, 4.5), Double.POSITIVE_INFINITY);
        assertTrue(Collisions.contains(sphere, box));

    }

}
