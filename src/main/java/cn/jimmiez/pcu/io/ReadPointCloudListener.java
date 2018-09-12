package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuPointCloud;

public interface ReadPointCloudListener extends IOListener {
    void onReadPointCloudSuccessfully(PcuPointCloud pointCloud, PlyReader.PlyHeader header);
}
