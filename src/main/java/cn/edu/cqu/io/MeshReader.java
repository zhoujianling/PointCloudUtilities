package cn.edu.cqu.io;

public interface MeshReader {
    void readMesh(String fileName, ReadMeshListener listener);
}
