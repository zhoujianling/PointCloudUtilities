package cn.jimmiez.pcu.model;

import cn.jimmiez.pcu.io.obj.ObjDataType;
import cn.jimmiez.pcu.io.obj.ReadFromObj;
import cn.jimmiez.pcu.io.off.ReadFromOff;
import cn.jimmiez.pcu.io.ply.ReadFromPly;

import java.util.ArrayList;
import java.util.List;

/**
 * a default entity class representing a point cloud
 */
public class PointCloud3f {
    private List<float[]> points;

    public PointCloud3f() {
        points = new ArrayList<>();
    }

    @ReadFromObj(type = ObjDataType.V_GEOMETRIC_VERTICES)
    @ReadFromOff
    @ReadFromPly(
            properties = {"x", "y", "z"},
            element = "vertex"
    )
    public List<float[]> getPoints() {
        return points;
    }

}
