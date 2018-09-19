package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuPointCloud3f;

public interface PointCloudWriter extends PcuFileWriter{
    void writePointCloud(PcuPointCloud3f pointCloud, String filePath);
}
