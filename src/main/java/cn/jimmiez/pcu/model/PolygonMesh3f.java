package cn.jimmiez.pcu.model;

import cn.jimmiez.pcu.io.off.ReadFromOff;
import cn.jimmiez.pcu.io.ply.ReadFromPly;

import java.util.ArrayList;
import java.util.List;

public class PolygonMesh3f {
    private List<float[]> points;
    private List<int[]> faces;

    public PolygonMesh3f() {
        points = new ArrayList<>();
        faces = new ArrayList<>();
    }

    @ReadFromOff
    @ReadFromPly(
            properties = {"x", "y", "z"},
            element = {"vertex", "vertices"}
    )
    public List<float[]> getPoints() {
        return points;
    }

    @ReadFromOff(dataType = ReadFromOff.FACES)
    @ReadFromPly(
            properties = {"vertex_indices"},
            element = {"face"}
    )
    public List<int[]> getFaces() {
        return faces;
    }

}
