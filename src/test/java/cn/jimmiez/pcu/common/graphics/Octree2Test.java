package cn.jimmiez.pcu.common.graphics;

import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.*;

import static org.junit.Assert.assertTrue;

public class Octree2Test {
    private List<Point3d> randomData(int number, double min, double max) {
        List<Point3d> list = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < number; i ++) {
            Point3d xyz = new Point3d();
            xyz.x = random.nextDouble() * (max - min) + min;
            xyz.y = random.nextDouble() * (max - min) + min;
            xyz.z = random.nextDouble() * (max - min) + min;
            list.add(xyz);
        }

        return list;
    }

    @Test
    public void searchNearestNeighborsTest() {
        List<Point3d> data = randomData(42349, 1, 11.5);
        Octree2 octree = new Octree2();
        octree.buildIndex(data);
        int[] indices = octree.searchNearestNeighbors(5, 3);
        assertTrue(indices.length == 5);
        double dis1 = data.get(3).distance(data.get(indices[0]));
        double dis2 = data.get(3).distance(data.get(indices[1]));
        double dis3 = data.get(3).distance(data.get(indices[2]));
        assertTrue(dis1 < dis2);
        assertTrue(dis2 < dis3);

        int k = 325;
        indices = octree.searchNearestNeighbors(k, 90);
        assertTrue(indices.length == k);
        for (int i = 1; i < k; i ++) {
            double distance1 = data.get(90).distance(data.get(indices[i - 1]));
            double distance2 = data.get(90).distance(data.get(indices[i]));
            assertTrue(distance1 < distance2);
        }

//         TEST searchNearestNeighbors(int, Point3d)
        BoundingBox box = BoundingBox.of(data);
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 3; i ++) {
            double x = box.getCenter().x + box.getxExtent() * (random.nextDouble() - 0.5);
            double y = box.getCenter().y + box.getyExtent() * (random.nextDouble() - 0.5);
            double z = box.getCenter().z + box.getzExtent()* (random.nextDouble() - 0.5);
            Point3d p = new Point3d(x, y, z);
            int randomK = random.nextInt(40);
            if (randomK == 0) randomK += 1;
            int[] neighbors = octree.searchNearestNeighbors(randomK, p);
            Set<Integer> set = new HashSet<>();
            for (int index : neighbors) set.add(index);
            assertTrue(neighbors.length == randomK);
            for (int j = 0; j < 30; j ++) {
                int randomIndex = random.nextInt(data.size());
                while (set.contains(randomIndex))  randomIndex = random.nextInt(data.size());
                double distance1 = p.distance(data.get(randomIndex));
                for (int kNeighborIndex : neighbors) {
                    double neighborDistance = data.get(kNeighborIndex).distance(p);
                    assertTrue(distance1 >= neighborDistance);
                }
            }
        }
    }

    @Test
    public void searchNeighborsInSphereTest() {
        int dataSize = 3127;
        List<Point3d> data = randomData(dataSize, 0.5, 11.5);
        Octree2 octree = new Octree2();
        octree.buildIndex(data);

        double radius = 1.9;
        for (int index = 0; index < dataSize; index ++) {
            List<Integer> neighbors = octree.searchNeighborsInSphere(index, radius);
            assertTrue(neighbors.size() > 0);
            for (Integer pointIndex : neighbors) {
                Point3d point = data.get(pointIndex);
                double distance = point.distance(data.get(index));
                assertTrue(distance <= radius);
            }
        }

        // TEST searchNearestNeighbors(int, Point3d)
        BoundingBox box = BoundingBox.of(data);
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 3; i ++) {
            double x = box.getCenter().x + box.getxExtent() * (random.nextDouble() - 0.5);
            double y = box.getCenter().y + box.getyExtent() * (random.nextDouble() - 0.5);
            double z = box.getCenter().z + box.getzExtent()* (random.nextDouble() - 0.5);
            Point3d p = new Point3d(x, y, z);
            double randomRadius = Math.min(box.getxExtent(), Math.min(box.getyExtent(), box.getzExtent())) * 0.3;
            List<Integer> neighbors = octree.searchNeighborsInSphere(p, randomRadius);
            Set<Integer> set = new HashSet<>();
            set.addAll(neighbors);
            for (int j = 0; j < 25; j ++) {
                int randomIndex = random.nextInt(data.size());
                while (set.contains(randomIndex))  randomIndex = random.nextInt(data.size());
                double distance1 = p.distance(data.get(randomIndex));
                for (int kNeighborIndex : neighbors) {
                    double neighborDistance = data.get(kNeighborIndex).distance(p);
                    assertTrue(distance1 >= neighborDistance);
                }
            }
        }

    }
}
