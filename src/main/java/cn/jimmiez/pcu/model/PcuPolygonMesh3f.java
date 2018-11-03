package cn.jimmiez.pcu.model;

import cn.jimmiez.pcu.io.ply.ReadFromPly;

import java.util.ArrayList;
import java.util.List;

public class PcuPolygonMesh3f {
    private List<float[]> point3ds;
    private List<int[]> faces;

    public PcuPolygonMesh3f() {
        point3ds = new ArrayList<>();
        faces = new ArrayList<>();
    }

    @ReadFromPly(
            properties = {"x", "y", "z"},
            element = {"vertex", "vertices"}
    )
    public List<float[]> getPoint3ds() {
        return point3ds;
    }

    @ReadFromPly(
            properties = {"vertex_indices"},
            element = {"face"}
    )
    public List<int[]> getFaces() {
        return faces;
    }
}
