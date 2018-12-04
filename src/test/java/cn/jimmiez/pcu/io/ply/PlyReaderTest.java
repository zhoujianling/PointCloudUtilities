package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.util.Pair;
import cn.jimmiez.pcu.model.PointCloud3f;
import cn.jimmiez.pcu.model.PolygonMesh3f;
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
        assertTrue(header.getElementTypes().get("vertex").getPropertiesName().size() == 3);
        assertTrue(header.getElementTypes().get("vertex").getPropertiesType().size() == 3);
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
        PointCloud3f pointCloud = reader.readPointCloud(file, PointCloud3f.class);
        assertNotNull(pointCloud);
        assertTrue(pointCloud.getPoints().size() == 4770);
        assertTrue(pointCloud.getPoints().get(1000).length == 3);
    }


    @Test
    public void readAsciiPlyDataTest2() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/drill_shaft_zip.ply").getFile());
        PolygonMesh3f pointCloud = reader.readPointCloud(file, PolygonMesh3f.class);
        assertNotNull(pointCloud);
        assertTrue(pointCloud.getPoints().size() == 881);
        assertTrue(pointCloud.getPoints().get(200).length == 3);
        assertTrue(pointCloud.getFaces().size() == 1288);
    }

    @Test
    public void readBinaryBigEndianPlyDataTest() {

    }

    @Test
    public void readBinaryLittleEndianPlyDataTest() {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("model/ply/tree_bin.ply").getFile());
        PolygonMesh3f pointCloud = reader.readPointCloud(file, PolygonMesh3f.class);
        assertTrue(pointCloud.getPoints().size() == 27788);
        assertTrue(pointCloud.getFaces().size() == 52113);
    }

}
