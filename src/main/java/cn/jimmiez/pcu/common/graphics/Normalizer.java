package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.util.PcuCommonUtil;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;
import java.util.List;

public class Normalizer {

    private double length;

    public Normalizer() {this(1.0);}

    public Normalizer(double len) {this.length = len;}

    /**
     * move the point cloud and ensure the center of bbox of point cloud is (0, 0, 0)
     * the x,y,z of each point is scaled to the range [-1, +1]
     * @param data point cloud data
     */
    public void normalize(List<Point3d> data) {
        BoundingBox box = BoundingBox.of(data);
        Vector3d bboxCenter = new Vector3d(- box.getCenter().x, - box.getCenter().y, - box.getCenter().z);
        for (Point3d p : data) p.add(bboxCenter);
        double maxLength = 2 * PcuCommonUtil.max(box.getxExtent(), box.getyExtent(), box.getzExtent());
        maxLength = Math.max(1E-7, maxLength);
        double ratio = length / maxLength;
        for (Point3d p : data) p.scale(ratio);
    }

    public double getLength() {
        return length;
    }
}
