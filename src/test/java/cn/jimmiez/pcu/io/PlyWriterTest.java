package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.io.ply.PlyWriter;
import cn.jimmiez.pcu.model.PcuPointCloud3f;
import org.junit.Test;
import static org.junit.Assert.*;

public class PlyWriterTest {


    @Test
    public void writeAsciiPlyTest() {
        PcuPointCloud3f pointCloud = new PcuPointCloud3f();
        pointCloud.getPoint3ds().add(new float[] {0, 1, 2});
        pointCloud.getPoint3ds().add(new float[] {1.4f, 10.2f, -2.1f});
        PlyWriter writer = new PlyWriter();
        int code = writer.write(pointCloud, PcuPointCloud3f.class);
        assertTrue(code == Constants.ERR_CODE_NO_ERROR);
    }
}
