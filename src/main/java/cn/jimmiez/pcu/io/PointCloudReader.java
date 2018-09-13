package cn.jimmiez.pcu.io;

import java.io.File;

public interface PointCloudReader extends PcuFileReader {
    void readPointCloud(String fileName, ReadListener listener);

    void readPointCloud(File file, ReadListener listener);
}
