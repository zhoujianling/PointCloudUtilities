package cn.jimmiez.pcu.util;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
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

}
