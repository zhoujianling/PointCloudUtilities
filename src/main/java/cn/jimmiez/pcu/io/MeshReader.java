package cn.jimmiez.pcu.io;

public interface MeshReader {
    void readMesh(String fileName, ReadMeshListener listener);
}
