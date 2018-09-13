package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuPointCloud;

public interface ReadListener<T> extends IOListener {
    void onSucceed(T pointCloud, PlyReader.PlyHeader header);
}
