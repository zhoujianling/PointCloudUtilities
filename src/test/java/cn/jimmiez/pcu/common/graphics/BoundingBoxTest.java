package cn.jimmiez.pcu.common.graphics;

import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import static org.junit.Assert.*;

public class BoundingBoxTest {

    @Test
    public void ofTest() {
        BoundingBox box = BoundingBox.of(genData(500,1, 3, 5, 7, 9, 11));
        assertTrue(box.minX() <= box.maxX());
        assertTrue(box.minY() <= box.maxY());
        assertTrue(box.minZ() <= box.maxZ());
        assertTrue(box.minX() >= 1);
        assertTrue(box.maxX() <= 3);
        assertTrue(box.minY() >= 5);
        assertTrue(box.maxY() <= 7);
        assertTrue(box.minZ() >= 9);
        assertTrue(box.maxZ() <= 11);

        box = BoundingBox.of(genData(500,
                100_000_000, 100_000_000,
                100_000_000, 100_000_000,
                100_000_000, 100_000_000));
        assertTrue(box.minX() <= box.maxX());
        assertTrue(box.minY() <= box.maxY());
        assertTrue(box.minZ() <= box.maxZ());
        assertEquals(100_000_000, box.minX(), 1e-5);
        assertEquals(100_000_000, box.maxX(), 1e-5);
        assertEquals(100_000_000, box.minY(), 1e-5);
        assertEquals(100_000_000, box.maxY(), 1e-5);
        assertEquals(100_000_000, box.minZ(), 1e-5);
        assertEquals(100_000_000, box.maxZ(), 1e-5);

        box = BoundingBox.of(genData(0,1, 3, 5, 7, 9, 11));
        assertTrue(Double.isNaN(box.minX()));
        assertTrue(Double.isNaN(box.maxX()));
        assertTrue(Double.isNaN(box.minY()));
        assertTrue(Double.isNaN(box.maxY()));
        assertTrue(Double.isNaN(box.minZ()));
        assertTrue(Double.isNaN(box.maxZ()));
    }

    private List<Point3d> genData(int n, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
        List<Point3d> data = new Vector<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < n; i ++) {
            data.add(new Point3d(
                minX + (maxX - minX) * random.nextDouble(),
                    minY + (maxY - minY) * random.nextDouble(),
                    minZ + (maxZ - minZ) * random.nextDouble()
            ));
        }
        return data;
    }
}
