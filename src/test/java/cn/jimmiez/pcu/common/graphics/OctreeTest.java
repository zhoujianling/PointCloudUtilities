package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.DataUtil;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static cn.jimmiez.pcu.CommonAssertions.*;
import static org.junit.Assert.fail;

public class OctreeTest {

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

    private void assertKNearestNeighborsHelp(List<Point3d> data, Point3d point, Set<Integer> set, double radius) {
        for (int index = 0; index < data.size(); index ++) {
            if (set.contains(index)) continue;
            double distance = data.get(index).distance(point);
            assertTrue("the distance " + distance + " should be larger than " + radius, distance >= radius);
        }
    }

    private void assertKNearestNeighbors(List<Point3d> data, Point3d point, int[] nearestIndices) {
        if (nearestIndices.length < 1) return;
        double radius = data.get(nearestIndices[nearestIndices.length - 1]).distance(point);
        Set<Integer> set = new HashSet<>();
        for (int i : nearestIndices) set.add(i);
        assertKNearestNeighborsHelp(data, point, set, radius);
    }

    private void assertNeighborsWithinRadius(List<Point3d> data, Point3d point, List<Integer> nearestIndices) {
        if (nearestIndices.size() < 1) return;
        double radius = data.get(nearestIndices.get(nearestIndices.size() - 1)).distance(point);
        Set<Integer> set = new HashSet<>();
        set.addAll(nearestIndices);
        assertKNearestNeighborsHelp(data, point, set, radius);
    }

    @Test
    public void testSearchNearestNeighbors() {
        Random random = new Random(System.currentTimeMillis());

        // test small data
        for (int i = 0; i < 3; i ++) {
            int testSize = 30;
            List<Point3d> testData1 = DataUtil.generateRandomData(testSize, 0, 3, 3, 5, -3, -1);
            Octree o2 = new Octree();
            o2.buildIndex(testData1);
            for (int k = 1; k < testSize; k ++) {
                int pointIndex = random.nextInt(testSize);
                int[] indices = o2.searchNearestNeighbors(k, pointIndex);
                assertEquals(k, indices.length);
                assertKNearestNeighbors(testData1, testData1.get(pointIndex), indices);
            }
            try {
                o2.searchNearestNeighbors(testSize + 1, 0);
                fail("Should throw exception");
            } catch (IllegalArgumentException e) {
            }
        }

        List<Point3d> data = randomData(42349, 1, 11.5);
        Octree octree = new Octree();
        octree.buildIndex(data);

        // test large data
        for (int iter = 0; iter < 3; iter ++) {
            int k = 350 + random.nextInt(100);
            int pointIndex = random.nextInt(data.size());
            int[] indices = octree.searchNearestNeighbors(k, pointIndex);
            assertEquals(k, indices.length);
            // test order of index
            for (int i = 1; i < k; i ++) {
                double distance1 = data.get(pointIndex).distance(data.get(indices[i - 1]));
                double distance2 = data.get(pointIndex).distance(data.get(indices[i]));
                assertLessEqualThan(distance1, distance2);
            }
            // test order of index
            assertKNearestNeighbors(data, data.get(pointIndex), indices);
        }

        // test searchNearestNeighbors(int, Point3d)
        BoundingBox box = BoundingBox.of(data);
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
            assertEquals(randomK, neighbors.length);
            for (int j = 0; j < 30; j ++) {
                int randomIndex = random.nextInt(data.size());
                while (set.contains(randomIndex))  randomIndex = random.nextInt(data.size());
                double distance1 = p.distance(data.get(randomIndex));
                for (int kNeighborIndex : neighbors) {
                    double neighborDistance = data.get(kNeighborIndex).distance(p);
                    assertLessEqualThan(neighborDistance, distance1);
                }
            }
        }
    }

    @Test
    public void testSearchAllNeighborsWithinDistance() {
        int dataSize = 3127;
        List<Point3d> data = randomData(dataSize, 0.5, 11.5);
        Octree octree = new Octree();
        octree.buildIndex(data);

        double radius = 3.9;
        for (int index = 0; index < dataSize; index ++) {
            List<Integer> neighbors = octree.searchAllNeighborsWithinDistance(index, radius);
            assertTrue(neighbors.size() > 0);
            for (Integer pointIndex : neighbors) {
                Point3d point = data.get(pointIndex);
                double distance = point.distance(data.get(index));
                assertTrue(distance <= radius);
            }
            if (index % 10 == 0) assertNeighborsWithinRadius(data, data.get(index), neighbors);
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
            List<Integer> neighbors = octree.searchAllNeighborsWithinDistance(p, randomRadius);
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
