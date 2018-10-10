package cn.jimmiez.pcu.model;

import java.util.ArrayList;
import java.util.List;

public class PcuPolygonMesh3f {
    private List<float[]> point3ds;
    private List<int[]> faces;

    public PcuPolygonMesh3f() {
        point3ds = new ArrayList<>();
        faces = new ArrayList<>();
    }

    @PcuPlyData(
            properties = {"x", "y", "z"},
            element = {"vertex", "vertices"}
    )
    public List<float[]> getPoint3ds() {
        return point3ds;
    }

    @PcuPlyData(
            properties = {"vertex_indices"},
            element = {"face"}
    )
    public List<int[]> getFaces() {
        return faces;
    }
}
