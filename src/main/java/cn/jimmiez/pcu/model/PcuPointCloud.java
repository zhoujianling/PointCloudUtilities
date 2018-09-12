package cn.jimmiez.pcu.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PcuPointCloud {
    private List<double[]> point3ds;

    public PcuPointCloud() {
        point3ds = new ArrayList<>();
    }

    public List<double[]> getPoint3ds() {
        return point3ds;
    }
}
