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

        assertTrue(box.getCenter().x + box.getxExtent() <= 3 && box.getCenter().x - box.getxExtent() >= 1);
        assertTrue(box.getCenter().y + box.getyExtent() <= 7 && box.getCenter().y - box.getyExtent() >= 5);
        assertTrue(box.getCenter().z + box.getzExtent() <= 11 && box.getCenter().z - box.getzExtent() >= 9);


        box = BoundingBox.of(genData(0,1, 3, 6, 7, 4, 11));
        assertTrue(Double.isNaN(box.getCenter().x));
        assertTrue(Double.isNaN(box.getCenter().y));
        assertTrue(Double.isNaN(box.getCenter().z));
        assertTrue(Double.isNaN(box.getxExtent()));
        assertTrue(Double.isNaN(box.getyExtent()));
        assertTrue(Double.isNaN(box.getzExtent()));
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
