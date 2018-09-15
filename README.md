# PointCloudUtil

## Description

This is a util for point cloud processing. Until recently few features have been added, so DO NOT watch this project :)

## Features
* Read a ply header from a file which follows the [standard](http://paulbourke.net/dataformats/ply/).
* Read a ply file and acquire a point cloud structure.

## Build

Make sure maven has been installed and directory of maven has been added into PATH. Run following command:
```shell
mvn package
```

## How to Use It

Firstly, on *NIX system, use following command to see the header of your ply file:
```shell
head -n 20 foo.ply

```
you will see a structure like this:
```
ply
format binary_little_endian 1.0
comment VCGLIB generated
element vertex 27788
property float x
property float y
property float z
property uchar red
property uchar green
property uchar blue
property uchar alpha
property float quality
element face 52113
property list uchar int vertex_indices
end_header
```

Secondly, you need to define a class to describe your point cloud.
```java
class YourPointCloud {
    private List<float[]> points = new ArrayList<>();

    @PcuElement(
        properties = {"x", "y", "z"},
        alternativeNames = {"vertex"}
    )
    public List<float[]> getPoints() {
        return points;
    }
}
```
Thirdly, instantiate a PlyReader, use readPointCloud() to get your point cloud.
```java
File file = ...;// ply file
PlyReader plyReader = new PlyReader();
YourPointCloud pc = plyReader.readPointCloud(file, YourPointCloud.class);
```

## TODO List
* Add support for writing ply files
* Add support for reading obj files
* Add normal estimator for point cloud


