package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.util.PcuCommonUtil;

import javax.vecmath.Point3d;
import java.util.List;

public class Normalizer {

    private double length = 1.0;

    public Normalizer() {this(1.0);}

    public Normalizer(double len) {this.length = len;}

    /**
     * transform point cloud to 1x1x1 bounding box
     * @param data point cloud data
     */
    public void normalize(List<Point3d> data) {
        BoundingBox box = BoundingBox.of(data);
        double maxLength = PcuCommonUtil.max(box.maxX() - box.minX(), box.maxY() - box.minY(), box.maxZ() - box.minZ());
        double ratio = length / maxLength;
        for (Point3d p : data) p.scale(ratio);
    }
}
