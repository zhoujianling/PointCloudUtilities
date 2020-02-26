package cn.jimmiez.pcu.alg.sampler;

import org.junit.Test;

import java.util.Random;
import static org.junit.Assert.*;

public class GridVoxelizerTest {

    @Test
    public void testParseIndex() {
        GridVoxelizer voxelizer = new GridVoxelizer();
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 30; i ++) {
            int xRow = random.nextInt((int)Math.pow(2, 19));
            int yRow = random.nextInt((int)Math.pow(2, 19));
            int zRow = random.nextInt((int)Math.pow(2, 19));
            long cellIndex = voxelizer.indexOfCell(xRow, yRow, zRow);
            int[] cellCoordinates = voxelizer.parseIndex(cellIndex);
            assertEquals(xRow, cellCoordinates[0]);
            assertEquals(yRow, cellCoordinates[1]);
            assertEquals(zRow, cellCoordinates[2]);
        }
    }
}
