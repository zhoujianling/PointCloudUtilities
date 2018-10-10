# PointCloudUtil

[![GitHub license](https://img.shields.io/github/license/Jimmie00x0000/PointCloudUtil.svg)](https://github.com/Jimmie00x0000/PointCloudUtil/blob/master/LICENSE)


## Description

This is a util for point cloud processing. Until recently few features have been added, so DO NOT watch this project :)

## Features
* Read a ply header from a file which follows the [standard](http://paulbourke.net/dataformats/ply/).
* Read a ply file and acquire a point cloud structure.
* Provide an octree for searching nearest neighbors

## Build

Make sure maven has been installed and directory of maven has been added into PATH. Run following command:
```shell
mvn package
```

## How to Use It
### PlyReader
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

Secondly, you need to declare a class to describe your point cloud. Decorate the getter with an annotation \@PcuElement. Make sure the name and type of field is corresponding to your ply header.
```java
class YourPointCloud {
    private List<float[]> points = new ArrayList<>();

    @PcuPlyData(
        properties = {"x", "y", "z"},
        element = {"vertex"}
    )
    public List<float[]> getPoints() {
        return points;
    }
}
```

Thirdly, instantiate a PlyReader, use readPointCloud() to get your point cloud.
```java
File file = new File("path of your ply file"); //ply file
PlyReader plyReader = new PlyReader();
YourPointCloud pc = plyReader.readPointCloud(file, YourPointCloud.class);
```

### Octree
Instantiate the Octree object, after calling *buildIndex()*, the nearest neighbors of certain point can be found in *O(n)* time.
```java
List<float[]> points = new ArrayList();
// ****
int k = 5; // number of neighbors
int i = 6; // if you want to find the neighbors of the 6th point.
Octree octree = new Octree();
octree.buildIndex(points);
int[] neighborIndices = octree.searchNearestNeighbors(k, i);
```
## TODO List
* Add support for writing ply files
* Add support for reading obj files
* Add normal estimator for point cloud


