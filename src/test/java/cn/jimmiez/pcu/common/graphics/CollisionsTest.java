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

}
