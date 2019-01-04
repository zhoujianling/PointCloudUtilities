package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.alg.normal.HoppeEstimator;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class HoppeEstimatorTest {

    private static double THRESHOLD = Math.PI / 4;

    @Test
    public void testEstimateNormals() {
        HoppeEstimator estimator = new HoppeEstimator();
        List<Point3d> data = generatePlaneData();
        List<Vector3d> normals = estimator.estimateNormals(data);
        Vector3d theoreticalNormal = new Vector3d(0, 0, 1);
        assertTrue(normals.size() == data.size());
        for (Vector3d normal : normals) {
            double angle = normal.angle(theoreticalNormal);
//            System.out.println("angle: " + angle);
            assertTrue(angle < THRESHOLD || Math.abs(angle - Math.PI) < THRESHOLD);
        }
    }

    private List<Point3d> generatePlaneData() {
        List<Point3d> data = new Vector<>();
        Random random = new Random(System.currentTimeMillis());
        double minX = -2;
        double maxX = 3;
        double minY = 1;
        double maxY = 3;
        double minZ = 0.22;
        double maxZ = 0.25;
        for (int i = 0; i < 1000; i ++) {
            Point3d point = new Point3d(
                    minX + (maxX - minX) * random.nextDouble(),
                    minY + (maxY - minY) * random.nextDouble(),
                    minZ + (maxZ - minZ) * random.nextDouble()
                    );
            data.add(point);
        }
        return data;
    }
}
