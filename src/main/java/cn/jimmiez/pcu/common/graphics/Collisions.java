package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.common.graphics.shape.Box;
import cn.jimmiez.pcu.common.graphics.shape.Sphere;

import javax.vecmath.Point3d;
import javax.vecmath.Vector3d;


import static java.lang.Math.*;

/**
 * This class contains some collision-detection methods.
 */
public class Collisions {

    /**
     * test if an AABBox intersects with a sphere
     * @param box the AABBox
     * @param sphere the sphere
     * @return test result
     */
    public static boolean intersect(Box box, Sphere sphere) {
        Point3d c1 = box.getCenter();
        Point3d c2 = sphere.getCenter();
        Vector3d hemiDiagonal = new Vector3d(box.getxExtent(), box.getyExtent(), box.getzExtent());
        Vector3d c1c2 = new Vector3d(c2.x - c1.x, c2.y - c1.y, c2.z - c1.z);
        c1c2.absolute();
        c1c2.sub(hemiDiagonal);
        Vector3d distance = new Vector3d(max(0, c1c2.x), max(0, c1c2.y), max(0, c1c2.z));
        return distance.dot(distance) <= sphere.getRadius() * sphere.getRadius();
    }

    /**
     * test if an AABBox contains a sphere
     * @param box the AABBox
     * @param sphere the sphere
     * @return test result
     */
    public static boolean contains(Box box, Sphere sphere) {
        Point3d c1 = box.getCenter();
        Point3d c2 = sphere.getCenter();
        Vector3d c1c2 = new Vector3d(c2.x - c1.x, c2.y - c1.y, c2.z - c1.z);
        c1c2.absolute();
        return c1c2.x + sphere.getRadius() <= box.getxExtent() &&
                c1c2.y + sphere.getRadius() <= box.getyExtent() &&
                c1c2.z + sphere.getRadius() <= box.getzExtent();
    }

}
