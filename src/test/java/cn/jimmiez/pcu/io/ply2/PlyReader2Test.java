package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.model.PointCloud3f;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import static org.junit.Assert.*;

public class PlyReader2Test {

    @Test
    public void readPlyDataTestAscii_3() throws IOException {
        PlyReader2 reader = new PlyReader2();
        File file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PlyData data = reader.readPlyData(file);
        for (PlyElement2 element2 : data) {
//            System.out.println("num: " + element2.toString());
            for (PlyProperties properties : element2) {
                assertNotNull(properties);
//                System.out.println(String.format("%f %f %f", properties.parseNextPropertyAsFloat(), properties.parseNextPropertyAsFloat(), properties.parseNextPropertyAsFloat()));
            }
        }
    }

    @Test
    public void readAsciiPlyDataTest_2() throws IOException {
        PlyReader2 reader = new PlyReader2();
        File file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PointCloud3f pointCloud = reader.read(file, PointCloud3f.class);
//        assertNotNull(pointCloud);
//        assertTrue(pointCloud.getPoints().size() == 4770);
//        assertTrue(pointCloud.getPoints().get(1000).length == 3);
    }

    @Test
    public void readBinaryPlyDataTest_2() throws IOException {
        PlyReader2 reader = new PlyReader2();
        File file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/tree_bin.ply").getFile());
        PointCloud3f pointCloud = reader.read(file, PointCloud3f.class);
//        assertNotNull(pointCloud);
//        assertTrue(pointCloud.getPoints().size() == 4770);
//        assertTrue(pointCloud.getPoints().get(1000).length == 3);
    }
}
