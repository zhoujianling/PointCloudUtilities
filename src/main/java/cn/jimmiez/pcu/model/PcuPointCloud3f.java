package cn.jimmiez.pcu.model;

import cn.jimmiez.pcu.io.ply.ReadFromPly;

import java.util.ArrayList;
import java.util.List;

/**
 * a default entity class representing a point cloud
 */
public class PcuPointCloud3f {
    private List<float[]> point3ds;

    public PcuPointCloud3f() {
        point3ds = new ArrayList<>();
    }

    @ReadFromPly(
            properties = {"x", "y", "z"},
            element = {"vertex", "vertices"}
    )
    public List<float[]> getPoint3ds() {
        return point3ds;
    }

}
