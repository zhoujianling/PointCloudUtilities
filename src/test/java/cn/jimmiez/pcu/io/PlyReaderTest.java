package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.io.ply.PlyHeader;
import cn.jimmiez.pcu.io.ply.PlyReader;
import cn.jimmiez.pcu.model.PcuPointCloud3f;
import javafx.util.Pair;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.*;

public class PlyReaderTest {

    @Test
    public void readAsciiPlyHeaderTest() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PlyHeader header = reader.readHeaderThenCloseFile(file);
        assertTrue(header.getPlyFormat() == PlyReader.FORMAT_ASCII);
        assertEquals(header.getPlyVersion(), 1.0f, 1e-5);
        assertTrue(header.getElementTypes().get("vertex").getPropertiesName().length == 3);
        assertTrue(header.getElementTypes().get("vertex").getPropertiesType().length == 3);
        assertTrue(header.getElementTypes().get("face").getListTypes().get("vertex_indices")[0] == PlyReader.TYPE_UCHAR);
        assertTrue(header.getElementTypes().get("face").getListTypes().get("vertex_indices")[1] == PlyReader.TYPE_INT);
        for (Pair<String, Integer> pair : header.getElementsNumber()) {
            if (pair.getKey().equals("vertex")) {
                assertTrue(pair.getValue() == 4770);
            } else if (pair.getKey().equals("face")) {
                assertTrue(pair.getValue() == 0);
            }
        }
    }

    @Test
    public void readAsciiPlyDataTest() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/simple.ply").getFile());
        PcuPointCloud3f pointCloud = reader.readPointCloud(file, PcuPointCloud3f.class);
        assertNotNull(pointCloud);
        assertTrue(pointCloud.getPoint3ds().size() == 4770);
        assertTrue(pointCloud.getPoint3ds().get(1000).length == 3);
    }


    @Test
    public void readAsciiPlyDataTest2() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/drill_shaft_zip.ply").getFile());
        PcuPointCloud3f pointCloud = reader.readPointCloud(file, PcuPointCloud3f.class);
        assertNotNull(pointCloud);
        assertTrue(pointCloud.getPoint3ds().size() == 881);
        assertTrue(pointCloud.getPoint3ds().get(200).length == 3);
        assertTrue(pointCloud.getFaces().size() == 1288);
    }

    @Test
    public void readBinaryBigEndianPlyDataTest() {

    }

    @Test
    public void readBinaryLittleEndianPlyDataTest() {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/tree_bin.ply").getFile());
        PcuPointCloud3f pointCloud = reader.readPointCloud(file, PcuPointCloud3f.class);
        assertTrue(pointCloud.getPoint3ds().size() == 27788);
        assertTrue(pointCloud.getFaces().size() == 52113);
    }

}
