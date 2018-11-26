package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.model.PcuPointCloud3f;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class PlyReader2Test {

    @Test
    public void readAsciiPlyDataTest_2() throws IOException {
        PlyReader2 reader = new PlyReader2();
        File file = new File(PlyReader2Test.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PcuPointCloud3f pointCloud = reader.readPointCloud(file, PcuPointCloud3f.class);
        assertNotNull(pointCloud);
//        assertTrue(pointCloud.getPoints().size() == 4770);
//        assertTrue(pointCloud.getPoints().get(1000).length == 3);
    }
}
