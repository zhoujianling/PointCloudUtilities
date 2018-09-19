package cn.jimmiez.pcu.io;

public interface ReadListener<T> extends IOListener {
    void onSucceed(T pointCloud, PlyReader.PlyHeader header);
}
