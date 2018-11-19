package cn.jimmiez.pcu.util;

import javax.vecmath.Point3d;
import java.util.ArrayList;
import java.util.List;

public class PcuCommonUtil {

    public static List<Point3d> arrayList2VecList(List<float[]> data) {
        List<Point3d> result = new ArrayList<>();
        for (float[] point : data) {
            result.add(new Point3d(point[0], point[1], point[2]));
        }
        return result;
    }

    public static ArrayList<Integer> incrementalIntegerList(int n) {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < n; i ++) {
            list.add(i);
        }
        return list;
    }

    public static double max(double... ds) {
        double result = ds[0];
        for (double d : ds) {
            result = Math.max(d, result);
        }
        return result;
    }
}
