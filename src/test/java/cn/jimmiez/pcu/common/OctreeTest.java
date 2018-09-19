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
        assertTrue(octree.getDepth() == 4);
        List<double[]> emptyData = new ArrayList<>();
        octree.buildIndex(emptyData);
        assertTrue(octree.getDepth() == 1);
        List<double[]> data2 = randomData(129, 0, 1);
        octree.buildIndex(data2);
        assertTrue(octree.getDepth() == 2);
    }
}
