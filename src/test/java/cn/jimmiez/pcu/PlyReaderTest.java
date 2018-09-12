package cn.jimmiez.pcu;

import cn.jimmiez.pcu.io.PlyReader;
import cn.jimmiez.pcu.io.ReadPointCloudListener;
import cn.jimmiez.pcu.model.PcuPointCloud;
import javafx.util.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

public class PlyReaderTest {

    public void simplePlyHeaderTest(PlyReader.PlyHeader header) {
        Assert.assertTrue(header.getPlyFormat() == PlyReader.FORMAT_ASCII);
        Assert.assertEquals(header.getPlyVersion(), 1.0f, 1e-5);
        Assert.assertTrue(header.getElementTypes().get("vertex").getPropertiesName().length == 3);
        Assert.assertTrue(header.getElementTypes().get("vertex").getPropertiesType().length == 3);
        Assert.assertTrue(header.getElementTypes().get("face").getListType1() == PlyReader.TYPE_UCHAR);
        Assert.assertTrue(header.getElementTypes().get("face").getListType2() == PlyReader.TYPE_INT);
        for (Pair<String, Integer> pair : header.getElementsNumber()) {
            if (pair.getKey().equals("vertex")) {
                Assert.assertTrue(pair.getValue() == 4770);
            } else if (pair.getKey().equals("face")) {
                Assert.assertTrue(pair.getValue() == 0);
            }
        }
    }

    @Test
    public void readAsciiPlyHeaderTest() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("pc/ply/simple.ply").getFile());
        PlyReader.PlyHeader header = reader.readHeaderThenCloseFile(file);
        simplePlyHeaderTest(header);
    }

    @Test
    public void readPlyDataTest() throws IOException {
        PlyReader reader = new PlyReader();
        File file = new File(PlyReaderTest.class.getClassLoader().getResource("pc/ply/simple.ply").getFile());
        reader.readPointCloud(file, new ReadPointCloudListener() {
            @Override
            public void onReadPointCloudSuccessfully(PcuPointCloud pointCloud, PlyReader.PlyHeader header) {
                simplePlyHeaderTest(header);
                Assert.assertTrue(pointCloud.getPoint3ds().size() == 4770);
                Assert.assertTrue(pointCloud.getPoint3ds().get(1000).length == 3);
            }

            @Override
            public void onError(int code, String message) {
                Assert.assertTrue(false);
            }
        });
    }
}
