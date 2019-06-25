package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.DataUtil;
import cn.jimmiez.pcu.alg.normal.HoppeEstimator;
import cn.jimmiez.pcu.util.VectorUtil;
import org.junit.Test;
import static org.junit.Assert.*;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.*;

public class HoppeEstimatorTest {

    private static double THRESHOLD = Math.PI / 4;

    @Test
    public void testEstimateNormals() {
        HoppeEstimator estimator = new HoppeEstimator();

        // test empty list
        assertNull(estimator.estimateNormals(new ArrayList<Point3d>()));

        // test with random data
        List<Point3d> randomData = DataUtil.generateRandomData(150, 0, 1, 0, 1, 0, 1);
        List<Vector3d> randomNormals = estimator.estimateNormals(randomData);
        for (Vector3d normal : randomNormals) {
            assertTrue(VectorUtil.validPoint(normal));
        }

        // test with a plane
        List<Point3d> data = generatePlaneData();
        List<Vector3d> normals = estimator.estimateNormals(data);
        Vector3d theoreticalNormal = new Vector3d(0, 0, 1);
        assertEquals(normals.size(), data.size());
        for (Vector3d normal : normals) {
            double angle = normal.angle(theoreticalNormal);
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
