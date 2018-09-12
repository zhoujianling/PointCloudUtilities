package cn.jimmiez.pcu.io;

import java.io.File;

public interface PointCloudReader extends PcuFileReader {
    void readPointCloud(String fileName, ReadPointCloudListener listener);

    void readPointCloud(File file, ReadPointCloudListener listener);
}
