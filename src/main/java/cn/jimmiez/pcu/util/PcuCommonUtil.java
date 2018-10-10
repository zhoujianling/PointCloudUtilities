package cn.jimmiez.pcu.util;

import javax.vecmath.Vector3d;
import java.util.ArrayList;
import java.util.List;

public class PcuCommonUtil {

    public static List<Vector3d> arrayList2VecList(List<float[]> data) {
        List<Vector3d> result = new ArrayList<>();
        for (float[] point : data) {
            result.add(new Vector3d(point[0], point[1], point[2]));
        }
        return result;
    }
}
