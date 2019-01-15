package cn.jimmiez.pcu.util;

import javax.vecmath.Tuple3d;

import static java.lang.Math.*;

public class VectorUtil {

    public static boolean validPoint(Tuple3d point) {
        return (!(Double.isNaN(point.x) || Double.isNaN(point.y) || Double.isNaN(point.z)) &&
                !(Double.isInfinite(point.x) || Double.isInfinite(point.y) || Double.isInfinite(point.z)));
    }

}
