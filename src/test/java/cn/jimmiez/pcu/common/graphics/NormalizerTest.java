package cn.jimmiez.pcu.common.graphics;

import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class NormalizerTest {

    @Test
    public void normalizeTest() {
        Normalizer normalizer = new Normalizer();

        BoundingBox box = new BoundingBox(-1, 3, 2, 4, 3, 5);
        List<Point3d> data = genData(150, box);
        normalizer.normalize(data);
        BoundingBox newBox = BoundingBox.of(data);
        assertTrue(newBox.maxX() - newBox.minX() <= normalizer.getLength());
        assertTrue(newBox.maxY() - newBox.minY() <= normalizer.getLength());
        assertTrue(newBox.maxZ() - newBox.minZ() <= normalizer.getLength());

    }

    private List<Point3d> genData(int n, BoundingBox box) {
        List<Point3d> data = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < n; i ++) {
            double rx = box.minX() + (box.maxX() - box.minX()) * random.nextDouble();
            double ry = box.minY() + (box.maxY() - box.minY()) * random.nextDouble();
            double rz = box.minZ() + (box.maxZ() - box.minZ()) * random.nextDouble();
            Point3d point = new Point3d(rx, ry, rz);
            data.add(point);
        }
        return data;
    }
}
