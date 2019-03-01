package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.DataUtil;
import org.junit.Test;

import javax.vecmath.Point3d;

import java.util.List;

import static org.junit.Assert.*;
import static cn.jimmiez.pcu.CommonAssertions.*;

public class BoundingBoxTest {


    @Test
    public void testBoundingBox() {
        // default bbox
        new BoundingBox();

        // test construct box with illegal argument
        try {
            new BoundingBox(2, 1, 3, 4, 4, 6);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            new BoundingBox(1, 2, 3, 1, 4, 6);
            fail();
        } catch (IllegalArgumentException e) {}
        try {
            new BoundingBox(1, 2, 3, 4, 4, 1);
            fail();
        } catch (IllegalArgumentException e) {}

        // test NaN
        BoundingBox box = new BoundingBox(1, Double.NaN, 3, 4, Double.NaN, 6);
        assertTrue(Double.isNaN(box.getCenter().x));
        assertFalse(Double.isNaN(box.getCenter().y));
        assertTrue(Double.isNaN(box.getCenter().z));
        assertTrue(Double.isNaN(box.getxExtent()));

        // test Inf
        box = new BoundingBox(1, Double.POSITIVE_INFINITY, 3, 4, Double.NEGATIVE_INFINITY, 6);
        assertTrue(Double.isInfinite(box.getCenter().x));
        assertFalse(Double.isInfinite(box.getCenter().y));
        assertTrue(Double.isInfinite(box.getCenter().z));
    }

    @Test
    public void testOf() {
        List<Point3d> randData = DataUtil.generateRandomData(500, 1, 3, 5, 7, 9, 11);
        BoundingBox box = BoundingBox.of(randData);

        assertLessEqualThan(box.getCenter().x + box.getxExtent(), 3);
        assertGreaterEqualThan(box.getCenter().x - box.getxExtent(), 1);
        assertTrue(box.getCenter().y + box.getyExtent() <= 7 && box.getCenter().y - box.getyExtent() >= 5);
        assertTrue(box.getCenter().z + box.getzExtent() <= 11 && box.getCenter().z - box.getzExtent() >= 9);

        box = BoundingBox.of(DataUtil.generateRandomData(0,1, 3, 6, 7, 4, 11));
        assertFalse(Double.isNaN(box.getCenter().x));
        assertFalse(Double.isNaN(box.getyExtent()));

        randData.add(new Point3d(Double.NaN, 1, 2.0));
        box = BoundingBox.of(randData);
        assertFalse(Double.isNaN(box.getCenter().y));
        assertFalse(Double.isNaN(box.getzExtent()));

        randData.add(new Point3d(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, 2.0));
        box = BoundingBox.of(randData);
        assertFalse(Double.isInfinite(box.getCenter().y));
        assertFalse(Double.isNaN(box.getCenter().y));
        assertFalse(Double.isInfinite(box.getCenter().x));
        assertFalse(Double.isNaN(box.getCenter().x));
    }

    @Test
    public void testDiagonalLength() {
        // test a normal bbox
        BoundingBox box = new BoundingBox(0, 1, 0, 1, 0, 1);
        assertEquals(Math.sqrt(3), box.diagonalLength(), 1E-6);

        // test a normal bbox
        box = new BoundingBox(-1, 1.5, -1, 1.5, -1, 1.5);
        assertEquals(2.5 * Math.sqrt(3), box.diagonalLength(), 1E-6);

        // test a bbox whose x-extent is NaN
        box = new BoundingBox(Double.NaN, 1, -1, 1, -1, 1);
        assertTrue(Double.isNaN(box.diagonalLength()));

        // test a bbox whose all extents is NaN
        box = new BoundingBox(Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN, Double.NaN);
        assertTrue(Double.isNaN(box.diagonalLength()));

        // test a bbox whose y-extent is Inf
        box = new BoundingBox(0, 1, -1, Double.POSITIVE_INFINITY, -1, 1);
        assertTrue(Double.isInfinite(box.diagonalLength()));

        // test a bbox whose x-extent is NaN, y-extent is Inf
        box = new BoundingBox(Double.NaN, 1, -1, Double.POSITIVE_INFINITY, -1, 1);
        assertTrue(Double.isNaN(box.diagonalLength()));

        // test a bbox whose all extents is Inf
        box = new BoundingBox(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
        assertTrue(Double.isInfinite(box.diagonalLength()));
    }

}
