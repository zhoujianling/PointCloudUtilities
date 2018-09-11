package cn.edu.cqu.io;

import cn.edu.cqu.model.PolygonMesh;

public interface ReadMeshListener extends IOListener{
    void onReadPointCloudSuccessfully(PolygonMesh mesh);
}
