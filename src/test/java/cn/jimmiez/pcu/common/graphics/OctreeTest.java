package cn.jimmiez.pcu.common.graphics;

import cn.jimmiez.pcu.DataUtil;
import cn.jimmiez.pcu.util.VectorUtil;
import org.junit.Test;

import javax.vecmath.Point3d;
import java.util.*;

import static cn.jimmiez.pcu.CommonAssertions.*;
import static org.junit.Assert.*;

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
            if (point == data.get(index)) continue;
            if (! VectorUtil.validPoint(data.get(index))) continue;
            double distance = data.get(index).distance(point);
            assertLessEqualThan(radius, distance);
        }
    }

    private void assertKNearestNeighbors(List<Point3d> data, Point3d point, int[] nearestIndices) {
        if (nearestIndices.length < 1) return;
        double prevDistance = Double.NEGATIVE_INFINITY;
        for (int index : nearestIndices) {
            double distance = point.distance(data.get(index));
            assertLessEqualThan(prevDistance, distance);
            prevDistance = distance;
        }
        double radius = data.get(nearestIndices[nearestIndices.length - 1]).distance(point);
        Set<Integer> set = new HashSet<>();
        for (int i : nearestIndices) {
            assertTrue("There should't be duplicate numbers", ! set.contains(i));
            if (data.get(i) == point) {
                fail("A point's nearest neighbor shouldn't be it self.");
            } else {
                set.add(i);
            }
        }
        assertKNearestNeighborsHelp(data, point, set, radius);
    }

    private void assertNeighborsWithinRadius(List<Point3d> data, Point3d point, List<Integer> nearestIndices) {
        if (nearestIndices.size() < 1) return;
        double radius = data.get(nearestIndices.get(nearestIndices.size() - 1)).distance(point);
        Set<Integer> set = new HashSet<>();
        for (int i : nearestIndices) {
            assertTrue("There should't be duplicate numbers", ! set.contains(i));
            set.add(i);
        }
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

        List<Point3d> data = randomData(22349, 1, 11.5);
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

        // test negative K
        try {
            octree.searchNearestNeighbors(-1, 0);
            fail("should throw exception");
        } catch (IllegalArgumentException e) {}

        // test set K to zero
        int[] ks = octree.searchNearestNeighbors(0, 0);
        assertEquals(0, ks.length);

        // test invalid point index
        try {
            octree.searchNearestNeighbors(3, -1);
            fail("should throw exception");
        } catch (Exception e) {}

        // #################################################
        // test searchNearestNeighbors(int, Point3d)
        // #################################################

        // test null
        try {
            octree.searchNearestNeighbors(1, null);
            fail("should throw exception");
        } catch (NullPointerException e) {}

        // test NaN
        try {
            octree.searchNearestNeighbors(1, new Point3d(Double.NaN, Double.NaN, Double.NaN));
            fail("should throw exception");
        } catch (IllegalArgumentException e) {}

        // test regular data
        BoundingBox box = BoundingBox.of(data);
        for (int i = 0; i < 3; i ++) {
            double x = box.getCenter().x + box.getxExtent() * (random.nextDouble() - 0.5);
            double y = box.getCenter().y + box.getyExtent() * (random.nextDouble() - 0.5);
            double z = box.getCenter().z + box.getzExtent()* (random.nextDouble() - 0.5);
            Point3d p = new Point3d(x, y, z);
            int randomK = 1 + random.nextInt(40);
            int[] neighbors = octree.searchNearestNeighbors(randomK, p);
            assertKNearestNeighbors(data, p, neighbors);
        }

        // test the point outside the bounding box of data
        for (int i = 0; i < 30; i ++) {
            double x = box.getCenter().x + box.getxExtent() * (random.nextDouble() + 1.0);
            double y = box.getCenter().y + box.getyExtent() * (random.nextDouble() + 1.0);
            double z = box.getCenter().z + box.getzExtent() * (random.nextDouble() + 1.0);
            Point3d p = new Point3d(x, y, z);
            int randomK = 1 + random.nextInt(170);
            int[] neighbors = octree.searchNearestNeighbors(randomK, p);
            assertEquals(randomK, neighbors.length);
            assertKNearestNeighbors(data, p, neighbors);
        }

        // test NaN in raw points
        data = randomData(7328, 1.5, 9.5);
        box = BoundingBox.of(data);
        for (int i = 0; i < 1000; i ++) {
            int index = random.nextInt(7328);
            data.set(index, new Point3d(Double.NaN, Double.NaN, Double.NaN));
        }
        octree = new Octree();
        octree.buildIndex(data);

        for (int i = 0; i < 3; i ++) {
            double x = box.getCenter().x + box.getxExtent() * (random.nextDouble() - 0.5);
            double y = box.getCenter().y + box.getyExtent() * (random.nextDouble() - 0.5);
            double z = box.getCenter().z + box.getzExtent()* (random.nextDouble() - 0.5);
            Point3d p = new Point3d(x, y, z);
            int randomK = 3 + random.nextInt(30);
            int[] neighbors = octree.searchNearestNeighbors(randomK, p);
            assertKNearestNeighbors(data, p, neighbors);
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
            assertGreaterThan(neighbors.size(), 0);
            for (Integer pointIndex : neighbors) {
                Point3d point = data.get(pointIndex);
                double distance = point.distance(data.get(index));
                assertLessEqualThan(distance, radius);
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

        // test NaN in raw points
        data = randomData(7328, 1.5, 9.5);
        box = BoundingBox.of(data);
        for (int i = 0; i < 1000; i ++) {
            int index = random.nextInt(7328);
            data.set(index, new Point3d(Double.NaN, Double.NaN, Double.NaN));
        }
        octree = new Octree();
        octree.buildIndex(data);

        for (int i = 0; i < 3; i ++) {
            double x = box.getCenter().x + box.getxExtent() * (random.nextDouble() - 0.5);
            double y = box.getCenter().y + box.getyExtent() * (random.nextDouble() - 0.5);
            double z = box.getCenter().z + box.getzExtent()* (random.nextDouble() - 0.5);
            Point3d p = new Point3d(x, y, z);
            int randomK = 7 + random.nextInt(35);
            int[] neighbors = octree.searchNearestNeighbors(randomK, p);
            assertKNearestNeighbors(data, p, neighbors);
        }
    }


//    @Test
//    public void testAdjacentNodes() {
//        Random random = new Random(System.currentTimeMillis());
//
//
//        // test on octree without spatial index
//        Octree octree = new Octree();
//        try {
//            octree.adjacentNodes(0L, Adjacency.FACE);
//            fail("Should throw exception");
//        } catch (IllegalStateException e) {}
//
//        // test medium data
//        for (int i = 0; i < 3; i ++) {
//            int testDataSize = 1900;
//            List<Point3d> testData1 = DataUtil.generateRandomData(testDataSize, -1.2, 3.1, 2.4, 5.8, -3, -1);
//            List<Point3d> testData2 = DataUtil.generateRandomData(testDataSize, 3.2, 9.1, -3.4, 8.8, -1.2, 0);
//            testData1.addAll(testData2);
//            octree = new Octree();
//            octree.buildIndex(testData1);
//            octree.setMaxPointsPerNode(90);
//            Map<Long, Octree.OctreeNode> nodesMap = octree.octreeIndices;
//
//            // test with invalid index
//            for (int j = 0; j < 5; j ++) {
//                long randomIndex = random.nextLong();
//                if (nodesMap.containsKey(randomIndex)) continue;
//                try {
//                    octree.adjacentNodes(randomIndex, Adjacency.FACE);
//                    fail("Should throw exception");
//                } catch (IllegalArgumentException e) {}
//                try {
//                    octree.adjacentNodes(randomIndex, Adjacency.EDGE);
//                    fail("Should throw exception");
//                } catch (IllegalArgumentException e) {}
//                try {
//                    octree.adjacentNodes(randomIndex, Adjacency.VERTEX);
//                    fail("Should throw exception");
//                } catch (IllegalArgumentException e) {}
//
//            }
//
//            // test with correct index
//            for (Long index : nodesMap.keySet()) {
//                Octree.OctreeNode centeredNode = nodesMap.get(index);
//                List<Octree.OctreeNode> nodes = octree.adjacentNodes(index, Adjacency.FACE);
//                assertNotNull(nodes);
//                for (Octree.OctreeNode node : nodes) {
//                    assertNotNull(nodes);
//                    double distance = node.getCenter().distance(centeredNode.getCenter());
//                    assertLessEqualThan(distance, centeredNode.getxExtent() + node.getxExtent());
//                }
//
//            }
//        }
//
//    }

}
