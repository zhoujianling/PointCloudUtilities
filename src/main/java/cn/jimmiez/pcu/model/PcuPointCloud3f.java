package cn.jimmiez.pcu.model;

import cn.jimmiez.pcu.io.off.ReadFromOff;
import cn.jimmiez.pcu.io.ply.ReadFromPly;

import java.util.ArrayList;
import java.util.List;

/**
 * a default entity class representing a point cloud
 */
public class PcuPointCloud3f {
    private List<float[]> points;

    public PcuPointCloud3f() {
        points = new ArrayList<>();
    }

    @ReadFromOff
    @ReadFromPly(
            properties = {"x", "y", "z"},
            element = {"vertex", "vertices"}
    )
    public List<float[]> getPoints() {
        return points;
    }

}
