# PointCloudUtil

## Description

This is a util for point cloud processing. Until recently few features have been added, so DO NOT watch this project :)

## Features
* Read a ply header from a file which follows the [standard](http://paulbourke.net/dataformats/ply/).
* Read a ASCII-format ply file and acquire a point cloud structure.

## Build

Make sure maven has been installed and directory of maven has been added into PATH. Run following command:
```shell
mvn package
```

## How to Use It

Import the project, then use following code to read a point cloud:
```java
File file = ...; //ply file
PlyReader plyReader = new PlyReader();
plyReader.readPointCloud(file, new ReadPointCloudListener() {
    @Override
    public void onSucceed(PcuPointCloud pcuPointCloud, PlyReader.PlyHeader plyHeader) {
        // process data
    }

    @Override
    public void onFail(int i, String s) {
        // handle exception
    }
});
```

## ToDo List
* Add support for reading and writing ply files
* Add normal estimator for point cloud


