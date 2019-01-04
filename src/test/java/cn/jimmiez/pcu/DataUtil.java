package cn.jimmiez.pcu;

import cn.jimmiez.pcu.common.graph.Graph;

import javax.vecmath.Point3d;
import java.util.List;
import java.util.Random;
import java.util.Vector;

public class DataUtil {

    public static List<Point3d> generateRandomData(int n, double minX, double maxX, double minY, double maxY, double minZ, double maxZ) {
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

//    public static Graph generateRandomGraph(int vn, int en) {
//
//    }

}
