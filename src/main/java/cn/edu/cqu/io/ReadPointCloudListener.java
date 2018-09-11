package cn.edu.cqu.io;

import cn.edu.cqu.model.PointCloud;

public interface ReadPointCloudListener extends IOListener {
    void onReadPointCloudSuccessfully(PointCloud pointCloud);
}
