package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.io.ply.PlyReaderTest;
import cn.jimmiez.pcu.model.PointCloud3f;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Random;

import static org.junit.Assert.*;

public class PlyReader2Test {

    @Test
    public void readPlyDataTest_3() throws IOException {
        PlyReader2 reader = new PlyReader2();
        File file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PlyData data = reader.readPly(file);
        for (PlyElement2 element2 : data) {
            assertNotNull(element2);
//            System.out.println(element2.elementData.length);
        }
        PointCloud3f pc = reader.read(file, PointCloud3f.class);
        assertNotNull(pc);


        file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/tree_bin.ply").getFile());
        data = reader.readPly(file);
        for (PlyElement2 element2 : data) {
            assertNotNull(element2);
//            System.out.println(element2.elementData.length);
        }

        pc = reader.read(file, PointCloud3f.class);
        assertNotNull(pc);



        file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PointCloud3f pointCloud = reader.read(file, PointCloud3f.class);
        assertNotNull(pointCloud);
        assertTrue(pointCloud.getPoints().size() == 4770);
        assertTrue(pointCloud.getPoints().get(1000).length == 3);
    }

    @Test
    public void test2() {
        PlyReader2 reader = new PlyReader2();
        File file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/tree_bin.ply").getFile());
        PlyReaderTest.MeshWithColor4b mesh = reader.read(file, PlyReaderTest.MeshWithColor4b.class);
        int expectedVerticesSize = 27788;
        int expectedFaceSize = 52113;
        assertEquals(expectedVerticesSize, mesh.getPoints().size());
        assertEquals(expectedFaceSize, mesh.getFaces().size());
        assertEquals(expectedVerticesSize, mesh.getVertexColors().size());
        Random random = new Random(System.currentTimeMillis());
        for (int i = 0; i < 300; i ++) {
            int randomPtr = random.nextInt(expectedVerticesSize);
            byte[] rgba = mesh.getVertexColors().get(randomPtr);
            assertEquals(4, rgba.length);
        }

    }
}
