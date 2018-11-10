package cn.jimmiez.pcu.common.graphics;

import org.junit.Test;
import static org.junit.Assert.*;

import javax.swing.text.html.HTMLEditorKit;
import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class HoppeEstimatorTest {

    private static double THRESHOLD = Math.PI / 10;

    @Test
    public void estimateNormalsTest() {
        HoppeEstimator estimator = new HoppeEstimator();
        List<Point3d> data = generatePlaneData();
        List<Vector3d> normals = estimator.estimateNormals(data);
        Vector3d thereticalNormal = new Vector3d(0, 0, 1);
        for (Vector3d normal : normals) {
            double angle = normal.angle(thereticalNormal);
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
        double maxZ = 0.23;
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
