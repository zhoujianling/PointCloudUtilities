package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuPolygonMesh;

public interface ReadMeshListener extends IOListener{
    void onReadPointCloudSuccessfully(PcuPolygonMesh mesh, PlyReader.PlyHeader header);
}
