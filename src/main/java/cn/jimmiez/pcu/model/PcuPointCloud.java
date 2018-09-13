package cn.jimmiez.pcu.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PcuPointCloud {
    private List<float[]> point3ds;

    public PcuPointCloud() {
        point3ds = new ArrayList<>();
    }

    @PcuElement(
            properties = {"x", "y", "z"},
            alternativeNames = {"vertex", "vertices"}
    )
    public List<float[]> getPoint3ds() {
        return point3ds;
    }

}
