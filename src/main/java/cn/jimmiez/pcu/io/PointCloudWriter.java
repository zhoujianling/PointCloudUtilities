package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuPointCloud;

public interface PointCloudWriter extends PcuFileWriter{
    void writePointCloud(PcuPointCloud pointCloud, String filePath);
}
