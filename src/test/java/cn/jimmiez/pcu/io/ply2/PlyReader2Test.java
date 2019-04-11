package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.model.PointCloud3f;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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
    }
}
