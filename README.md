# PointCloudUtil

[![GitHub license](https://img.shields.io/github/license/Jimmie00x0000/PointCloudUtil.svg)](https://github.com/Jimmie00x0000/PointCloudUtil/blob/master/LICENSE)
[![Circle CI](https://img.shields.io/circleci/project/github/Jimmie00x0000/PointCloudUtil/dev.svg)](https://circleci.com/gh/Jimmie00x0000/PointCloudUtil/tree/dev)
[![Maven Central](https://img.shields.io/maven-central/v/cn.jimmiez/pcutil.svg)](https://mvnrepository.com/artifact/cn.jimmiez/pcutil)


## Description

This is a util for point cloud processing. 

## Features
* Read a ply file from a file which follows the [standard](http://paulbourke.net/dataformats/ply/).
* Write a ply file.
* Read an obj file.
* Provide an octree for searching nearest neighbors.
* Implement some algorithms (LOP, dijkstra ...)

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

    @ReadFromPly(
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

### PlyWriter
There are two choices for writing a ply file.
#### Use annotation
Firstly, declare a class for your PointCloud/Mesh using annotation WriteScalarToPly and WriteListToPly.
```java
    private class PlyEntity {

        List<Point3d> vertices = new ArrayList<>();
        List<int[]> vertexIndices = new ArrayList<>();

        @WriteScalarToPly(element = "vertex", properties = {"x", "y", "z"}, typeName = "double")
        public List<double[]> vertices() {
            List<double[]> result = new ArrayList<>();
            for (Point3d p : vertices) {
                result.add(new double[]{p.x, p.y, p.z});
            }
            return result;
        }

        @WriteListToPly(element = "face", property = "vertex_index")
        public List<int[]> faces() {
            return vertexIndices;
        }

    }
```
Secondly, use following code to write a ply:
```java
PlyEntity entity = new PlyEntity();
// put your data into entity 
PlyWriter writer = new PlyWriter();
writer.write(entity, new File("ply path"));
```
#### Use prepare()
Firstly, prepare your vertices data and face data.
```java
List<float[]> vertexData = new Vector<>();
vertexData.add(new float[] {0.1f, -3.0f, 0.7f});
vertexData.add(new float[] {0.3f, +3.0f, 0.9f});
vertexData.add(new float[] {2.1f, -3.0f, 1.1f});
vertexData.add(new float[] {0.5f, -3.0f, 1.0f});
vertexData.add(new float[] {2.1f, -3.0f, 1.1f});

List<int[]> faceData = new Vector<>();
faceData.add(new int[] {0, 1, 2});
faceData.add(new int[] {1, 0, 4});

```

Secondly, use one line code to write the ply.
```java
int result = new PlyWriter()
        .prepare()
        .format(PlyReader.FORMAT_ASCII)
        .comment("this is test")
        .defineElement("vertex")
        .defineScalarProperties(new String[] {"x", "y", "z"}, PlyPropertyType.FLOAT, vertexData)
        .defineElement("face")
        .defineListProperty("vertex_indices", PlyPropertyType.UCHAR, PlyPropertyType.INT, faceData)
        .writeTo(new File("yourModel.ply"))
        .okay();

```

### Octree
Instantiate the Octree object, after calling *buildIndex()*, the nearest neighbors of certain point can be found in *O(n)* time.
```java
List<Point3d> points = new ArrayList();
// ****
int n = 5; // number of neighbors
int i = 6; // if you want to find the neighbors of the 6th point.
Octree octree = new Octree();
octree.buildIndex(points);
int[] neighborIndices = octree.searchNearestNeighbors(n, i);
```
## TODO List
* Add normal estimator for point cloud
* Add some common algorithms


