package cn.edu.cqu.io;

import java.io.File;

public interface PointCloudReader extends PCUFileReader {
    void readPointCloud(String fileName, ReadPointCloudListener listener);

    void readPointCloud(File file, ReadPointCloudListener listener);
}
