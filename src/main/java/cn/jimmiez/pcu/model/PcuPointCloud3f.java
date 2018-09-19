package cn.jimmiez.pcu.model;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class PcuPointCloud3f {
    private List<float[]> point3ds;
    private List<int[]> faces;

    public PcuPointCloud3f() {
        point3ds = new ArrayList<>();
        faces = new ArrayList<>();
    }

    @PcuElement(
            properties = {"x", "y", "z"},
            alternativeNames = {"vertex", "vertices"}
    )
    public List<float[]> getPoint3ds() {
        return point3ds;
    }

    @PcuElement(
            properties = {"vertex_indices"},
            alternativeNames = {"face"}
    )
    public List<int[]> getFaces() {
        return faces;
    }
}
