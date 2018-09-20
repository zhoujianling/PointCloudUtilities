package cn.jimmiez.pcu.util;

import static java.lang.Math.*;

public class PcuVectorUtil {

    public static double distance(double[] p1, double[] p2) {
        return sqrt((p1[0] - p2[0]) * (p1[0] - p2[0])
                + (p1[1] - p2[1]) * (p1[1] - p2[1])
                + (p1[2] - p2[2]) * (p1[2] - p2[2])
        );
    }
}
