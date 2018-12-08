# PointCloudUtil

[![GitHub license](https://img.shields.io/github/license/Jimmie00x0000/PointCloudUtil.svg)](https://github.com/Jimmie00x0000/PointCloudUtil/blob/master/LICENSE)
[![Circle CI](https://img.shields.io/circleci/project/github/Jimmie00x0000/PointCloudUtil/dev.svg)](https://circleci.com/gh/Jimmie00x0000/PointCloudUtil/tree/dev)
[![Maven Central](https://img.shields.io/maven-central/v/cn.jimmiez/pcutil.svg)](https://mvnrepository.com/artifact/cn.jimmiez/pcutil)


## Description
This is a util for point cloud processing. 
![](https://jimmie00x0000.github.io/img/Normals.png)

## Features
* Read and write a *PLY* file.
* Read an *OFF* file.
* Read an *OBJ* file.
* Searching nearest neighbors using *Octree*.
* Estimate normals of point cloud surface.
* Other algorithms for point cloud processing...

## Build 
Make sure maven has been installed and directory of maven has been added into PATH. Run following command:
```shell
mvn package -X
```

## Download
* Download the release via [Maven Central](https://mvnrepository.com/artifact/cn.jimmiez/pcutil).
* Obtain **SNAPSHOT** release from [this page](https://oss.sonatype.org/content/repositories/snapshots/cn/jimmiez/pcutil/)

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

Secondly, you need to declare an entity class for your point cloud. You must provide getters for vertex data or face data and then annotate them with *\@ReadFromPly*. Make sure the *element* and *properties* in the annotation are corresponding to your ply header.

The following code snippet shows how to define a point cloud or mesh:
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

Thirdly, instantiate a *PlyReader*, call *read()* to obtain your point cloud object.
```java
File file = new File("path of your ply file"); //ply file
PlyReader plyReader = new PlyReader();
YourPointCloud pc = plyReader.read(file, YourPointCloud.class);
```

### PlyWriter
There are two choices for writing a *PLY* file.
#### Use annotation
Firstly, declare an entity class for your point cloud or mesh and provide getters of vertex data or face data. You can read multiple scalar properties in one element using annotation *\@WriteScalarToPly* or one list property in one element using annotation *\@WriteListToPly* at once. 

```java
    private class PlyEntity {

        List<Point3d> vertices = new ArrayList<>();
        List<int[]> vertexIndices = new ArrayList<>();

        @WriteScalarToPly(element = "vertex", properties = {"x", "y", "z"}, type = PlyPropertyType.DOUBLE)
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
Secondly, instantiate a *PlyWriter* and call *write()* to write the data into a file:
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

### OffReader 
You can use *OffReader* to get point cloud data from *OFF* file.

Define the entity class as you did in [PlyReader](#PlyReader). Annotate the getters of vertex data of face data with *\@ReadFromOff*, see following code snippet:

```java
public class PolygonMesh3f {
    private List<float[]> points;
    private List<int[]> faces;

    public PolygonMesh3f() {
        points = new ArrayList<>();
        faces = new ArrayList<>();
    }

    @ReadFromOff(dataType = ReadFromOff.VERTICES)
    public List<float[]> getPoints() {
        return points;
    }

    @ReadFromOff(dataType = ReadFromOff.FACES)
    public List<int[]> getFaces() {
        return faces;
    }
}
```

Instantiate the *OffReader* and call *read()* to obtain the point cloud or mesh object.

```java
File file = new File("path...");
OffReader reader = new OffReader();
PolygonMesh3f mesh = reader.read(file, PolygonMesh3f.class);
```

### Octree
Instantiate the *Octree* object, after calling *buildIndex()*, the approximate nearest neighbors of certain point can be quickly found.
```java
List<Point3d> points = new ArrayList();
// ****
int n = 5; // number of neighbors
int i = 6; // if you want to find the nearest neighbors of the ith point.
Octree octree = new Octree();
octree.buildIndex(points);
int[] neighborIndices = octree.searchNearestNeighbors(n, i);

```

Another function *searchNeighborsInSphere()* is useful in finding all points from which the distance to a certain point is less than *radius*.
```java
double radius = 0.5d;
List<Integer> neighborIndices2 = octree.searchNeighborsInSphere(i, radius);
```

## How to Visualize Point Cloud
This library only provide api to operate 3d point cloud and cannot be used for 3d presentation. 

If you need to visualize point cloud using Java, you can refer to [PointCloudUtilSample](https://github.com/Jimmie00x0000/PointCloudUtilSample) which uses Java3d;

## TODO List
* Delaunay triangulation
* vtk reader 
* more unit tests


