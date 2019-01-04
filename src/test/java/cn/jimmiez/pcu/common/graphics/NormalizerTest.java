package cn.jimmiez.pcu.common.graphics;

import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.*;

public class NormalizerTest {

    @Test
    public void testNormalize() {
        Normalizer normalizer = new Normalizer();

        BoundingBox box = new BoundingBox(-1, 3, 2, 4, 3, 5);
        List<Point3d> data = genData(150, box);
        normalizer.normalize(data);
        BoundingBox newBox = BoundingBox.of(data);
        assertTrue(2 * newBox.getxExtent() <= normalizer.getLength());
        assertTrue(2 * newBox.getyExtent() <= normalizer.getLength());
        assertTrue(2 * newBox.getzExtent() <= normalizer.getLength());

    }

    private List<Point3d> genData(int n, BoundingBox box) {
        List<Point3d> data = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < n; i ++) {
            double rx = box.getCenter().x + box.getxExtent() * 2 * (random.nextDouble() - 0.5);
            double ry = box.getCenter().y + box.getyExtent() * 2 * (random.nextDouble() - 0.5);
            double rz = box.getCenter().z + box.getzExtent() * 2 * (random.nextDouble() - 0.5);
            Point3d point = new Point3d(rx, ry, rz);
            data.add(point);
        }
        return data;
    }
}
