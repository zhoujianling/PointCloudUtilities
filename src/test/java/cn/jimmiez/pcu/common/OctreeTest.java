package cn.jimmiez.pcu.common;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import static org.junit.Assert.*;

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
        List<double[]> data = randomData(10384, 0, 10);
        Octree octree = new Octree();
        octree.buildIndex(data);
        int pointNum = 0;
        for (Long nodeKey : octree.getOctreeIndices().keySet()) {
            pointNum += octree.getOctreeIndices().get(nodeKey).getIndices().size();
        }

        assertTrue(octree.getDepth() == 4);
        assertTrue(octree.getOctreeIndices().size() == 8 * 8 * 8);
        assertTrue(pointNum == 10384);

        List<double[]> emptyData = new ArrayList<>();
        octree.buildIndex(emptyData);
        assertTrue(octree.getDepth() == 1);
        List<double[]> data2 = randomData(129, 0, 1);
        octree.buildIndex(data2);
        assertTrue(octree.getDepth() == 2);
        assertTrue(octree.getOctreeIndices().size() == 8);
    }

    @Test
    public void searchNearestNeighborsTest() {
        List<double[]> data = randomData(10384, 0, 10);
        Octree octree = new Octree();
        octree.buildIndex(data);
        int[] indices = octree.searchNearestNeighbors(5, 3);
        double[] target = data.get(3);
        System.out.println("target x " + target[0] + " y " + target[1] + " z " + target[2]);
        for (int index : indices) {
            double[] point = data.get(index);
            System.out.println("x " + point[0] + " y " + point[1] + " z " + point[2]);
        }
        indices = octree.searchNearestNeighbors(105, 90);
    }

}
