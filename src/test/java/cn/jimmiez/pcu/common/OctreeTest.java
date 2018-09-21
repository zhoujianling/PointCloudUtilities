package cn.jimmiez.pcu.common;

import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;
import static cn.jimmiez.pcu.util.PcuVectorUtil.*;

public class OctreeTest {

    private List<double[]> randomData(int number, double min, double max) {
        List<double[]> list = new ArrayList<>();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < number; i ++) {
            double[] xyz = new double[3];
            xyz[0] = random.nextDouble() * (max - min) + min;
            xyz[1] = random.nextDouble() * (max - min) + min;
            xyz[2] = random.nextDouble() * (max - min) + min;
            list.add(xyz);
        }

        return list;
    }

    @Test
    public void buildIndexTest() {
        int dataSize = 10384;
        double min = 0;
        double max = 10;
        List<double[]> data = randomData(dataSize, min, max);
        Octree octree = new Octree();
        octree.buildIndex(data);
        int pointNum = 0;
        for (Long nodeKey : octree.octreeIndices.keySet()) {
            pointNum += octree.octreeIndices.get(nodeKey).getIndices().size();
        }
        assertTrue(octree.getDepth() == 4);
        assertTrue(octree.octreeIndices.size() == 8 * 8 * 8);
        assertTrue(pointNum == dataSize);

        /** corner node **/
        Octree.OctreeNode node = octree.octreeIndices.get(0L);
        assertTrue(Math.abs(node.minX - 0) < 1e-1);
        assertTrue(Math.abs(node.minY - 0) < 1e-1);
        assertTrue(Math.abs(node.minZ - 0) < 1e-1);

        List<double[]> emptyData = new ArrayList<>();
        octree.buildIndex(emptyData);
        assertTrue(octree.getDepth() == 1);
        List<double[]> data2 = randomData(129, 0, 1);
        octree.buildIndex(data2);
        assertTrue(octree.getDepth() == 2);
        assertTrue(octree.octreeIndices.size() == 8);
    }

    @Test
    public void searchNearestNeighborsTest() {
        List<double[]> data = randomData(42349, 1, 11.5);
        Octree octree = new Octree();
        octree.buildIndex(data);
        int[] indices = octree.searchNearestNeighbors(5, 3);
        assertTrue(indices.length == 5);
        double dis1 = distance(data.get(3), data.get(indices[0]));
        double dis2 = distance(data.get(3), data.get(indices[1]));
        double dis3 = distance(data.get(3), data.get(indices[2]));
        assertTrue(dis1 < dis2);
        assertTrue(dis2 < dis3);
//        double[] target = data.get(3);
//        System.out.println("target x " + target[0] + " y " + target[1] + " z " + target[2]);
//        for (int index : indices) {
//            double[] point = data.get(index);
//            System.out.println("x " + point[0] + " y " + point[1] + " z " + point[2]);
//        }
        indices = octree.searchNearestNeighbors(705, 90);
        assertTrue(indices.length == 705);
        for (int i = 1; i < 705; i ++) {
            double distance1 = distance(data.get(90), data.get(indices[i - 1]));
            double distance2 = distance(data.get(90), data.get(indices[i]));
            assertTrue(distance1 < distance2);
        }
    }

    @Test
    public void bitsTest() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<double[]> data = randomData(90384, 0, 10);
        Octree octree = new Octree();
        octree.buildIndex(data);

        long t1 = 140L;
        int[] coords = octree.index2Coordinates(t1);
        assertTrue(coords.length == 3);
        long t1t = octree.coordinates2Index(coords);
        assertTrue(t1 == t1t);
        int[] c2 = new int[] {5, 8, 14};
        long k2 = octree.coordinates2Index(c2);
        // 011 101 001 100
        assertTrue(k2 == 0x74C);
        List<Long> neighbors = octree.obtainAdjacent26Indices(k2);
        int[] nkc = octree.index2Coordinates(neighbors.get(0));
        assertTrue(nkc[0] == 4);
        assertTrue(nkc[1] == 7);
        assertTrue(nkc[2] == 13);
    }

    @Test
    public void locateOctreeNodeTest() {
        int dataSize = 42349;
        List<double[]> data = randomData(dataSize, 1, 11.5);
        Octree octree = new Octree();
        octree.buildIndex(data);
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 10; i ++) {
            int randomPointIndex = random.nextInt(dataSize);
            long nodeIndex = octree.locateOctreeNode(octree.root, data.get(randomPointIndex));
            List<Integer> indices = octree.octreeIndices.get(nodeIndex).indices;
            boolean exist = false;
            for (int pointIndex : indices) {
                if (pointIndex == randomPointIndex) {
                    exist = true;
                    break;
                }
            }
            assertTrue(exist);

            exist = false;
            nodeIndex = nodeIndex == 0 ? 1 : nodeIndex - 1;
            indices = octree.octreeIndices.get(nodeIndex).indices;
            for (int pointIndex : indices) {
                if (pointIndex == randomPointIndex) {
                    exist = true;
                    break;
                }
            }
            assertFalse(exist);
        }
    }

    @Test
    public void obtainAdjacent26IndicesTest() {
        int dataSize = 82940;
        List<double[]> data = randomData(dataSize, 0.5, 11.5);
        Octree octree = new Octree();
        octree.buildIndex(data);
        long nodeIndex = octree.coordinates2Index(new int[] {1, 2, 3});
        Octree.OctreeNode centralNode = octree.octreeIndices.get(nodeIndex);
        assertNotNull(centralNode);

        List<Long> adjacentIndices = octree.obtainAdjacent26Indices(nodeIndex);
        assertTrue(adjacentIndices.size() == 26);
        List<Octree.OctreeNode> nodes = new ArrayList<>();
        for (Long adjacentNodeIndex : adjacentIndices) {
            nodes.add(octree.octreeIndices.get(adjacentNodeIndex));
        }

        /** test boundary nodes **/
        nodeIndex = octree.coordinates2Index(new int[] {0, 0, 0});
        centralNode = octree.octreeIndices.get(nodeIndex);
        assertNotNull(centralNode);
        adjacentIndices = octree.obtainAdjacent26Indices(nodeIndex);
        assertTrue(adjacentIndices.size() == 7);

        nodeIndex = octree.coordinates2Index(new int[] {0, 0, 1});
        centralNode = octree.octreeIndices.get(nodeIndex);
        assertNotNull(centralNode);
        adjacentIndices = octree.obtainAdjacent26Indices(nodeIndex);
        assertTrue(adjacentIndices.size() == 11);
    }

}
