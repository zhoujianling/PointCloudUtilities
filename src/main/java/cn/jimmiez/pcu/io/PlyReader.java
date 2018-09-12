package cn.jimmiez.pcu.io;

import cn.jimmiez.pcu.model.PcuPointCloud;
import cn.jimmiez.pcu.Constants;
import javafx.util.Pair;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;

public class PlyReader implements PointCloudReader, MeshReader{

    public static final Integer FORMAT_ASCII = 0x3001;
    public static final Integer FORMAT_BINARY_BIG_ENDIAN = 0x3002;
    public static final Integer FORMAT_BINARY_LITTLE_ENDIAN = 0x3003;

    public static final Integer FORMAT_NON_FORMAT = - 0x3001;

    public static final Integer TYPE_LIST = 0x1001;

    /** scalar type **/
    public static final Integer TYPE_CHAR = 0x0001;
    public static final Integer TYPE_UCHAR = 0x0002;
    public static final Integer TYPE_SHORT = 0x0003;
    public static final Integer TYPE_USHORT = 0x0004;
    public static final Integer TYPE_INT = 0x0005;
    public static final Integer TYPE_UINT = 0x0006;
    public static final Integer TYPE_FLOAT = 0x0007;
    public static final Integer TYPE_DOUBLE = 0x0008;

    public static final Integer TYPE_NONTYPE = - 0x0001;

    public PlyHeader readHeaderThenCloseFile(File file) throws IOException {
        PlyHeader result;
        FileReader reader = new FileReader(file);
        result = readHeader(reader);
        reader.close();
        return result;
    }
    public PlyHeader readHeader(FileReader reader) throws IOException {
        Scanner scanner = new Scanner(reader);
        return readHeader(scanner);
    }

    private PlyHeader readHeader(Scanner scanner) throws IOException {
        PlyHeader header = new PlyHeader();
        List<String> headerLines = new ArrayList<>();
        try {
            String line = scanner.nextLine();
            while (! line.equals("end_header")) {
                if (! line.startsWith("comment ")) {
                    headerLines.add(line);
                }
                line = scanner.nextLine();
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Invalid ply file: Cannot find end of header.");
        }
        if (headerLines.size() < 1) {
            throw new IOException("Invalid ply file: No data");
        }
        String firstLine = headerLines.get(0);
        if (! firstLine.equals(Constants.MAGIC_STRING)) {
            throw new IOException("Invalid ply file: Ply file does not start with ply.");
        }
        String secondLine = headerLines.get(1);
        readPlyFormat(secondLine, header);
        for (int lineNo = 2; lineNo < headerLines.size();) {
            String elementLine = headerLines.get(lineNo);
            Pair<String, Integer> pair = readPlyElement(elementLine);
            lineNo += 1;
            int propertyStartNo = lineNo;
            while (lineNo < headerLines.size() && headerLines.get(lineNo).startsWith("property ")) lineNo++;
            PlyElement element = new PlyElement(lineNo - propertyStartNo);
            for (int i = propertyStartNo; i < lineNo; i ++) {
                String[] propertySlices = headerLines.get(i).split(" ");
                if (propertySlices.length < 3) throw new IOException("Invalid ply file.");
                element.propertiesName[i - propertyStartNo] = propertySlices[propertySlices.length - 1];
                element.propertiesType[i - propertyStartNo] = recognizeType(propertySlices[1]);
                if (element.propertiesType[i - propertyStartNo] == TYPE_LIST) {
                    if (propertySlices.length < 5) throw new IOException("Invalid ply file. Wrong list property.");
                    element.listType1 = recognizeType(propertySlices[2]);
                    element.listType2 = recognizeType(propertySlices[3]);
                }
            }
            header.elementTypes.put(pair.getKey(), element);
            header.elementsNumber.add(pair);
        }
        return header;
    }

    private int recognizeType(String type) {
        switch (type) {
            case "char":
                return TYPE_CHAR;
            case "uchar":
                return TYPE_UCHAR;
            case "int":
                return TYPE_INT;
            case "uint":
                return TYPE_UINT;
            case "short":
                return TYPE_SHORT;
            case "ushort":
                return TYPE_USHORT;
            case "float":
                return TYPE_FLOAT;
            case "double":
                return TYPE_DOUBLE;
            case "list":
                return TYPE_LIST;
        }
        return TYPE_NONTYPE;
    }

    private Pair<String, Integer> readPlyElement(String line) throws IOException {
        String[] elementSlices = line.split(" ");
        if (! line.startsWith("element ") || elementSlices.length < 3) {
            throw new IOException("Invalid ply file: Invalid format.");
        }
        String elementName = elementSlices[1];
        Integer elementNumber = Integer.valueOf(elementSlices[2]);
        return new Pair<>(elementName, elementNumber);
    }

    private void readPlyFormat(String line, PlyHeader header) throws IOException {
        if (!line.startsWith("format ")) {
            throw new IOException("Invalid ply file: No format information");
        }
        String[] formatSlices = line.split(" ");
        if (formatSlices.length == 3) {
            if (formatSlices[1].equals("ascii")) {
                header.plyFormat = FORMAT_ASCII;
            } else if (formatSlices[1].equals("binary_little_endian")) {
                header.plyFormat = FORMAT_BINARY_LITTLE_ENDIAN;
            } else if (formatSlices[1].equals("binary_big_endian")) {
                header.plyFormat = FORMAT_BINARY_BIG_ENDIAN;
            }
            header.plyVersion = Float.valueOf(formatSlices[2]);
        } else {
            throw new IOException("Invalid ply file: Wrong format ply in line");
        }
    }

    @Override
    public void readPointCloud(String fileName, ReadPointCloudListener listener) {
        File file = new File(fileName);
        readPointCloud(file, listener);
    }

    /**
     * read a 3d point cloud from a ply file
     * @param file The point cloud file(ply)
     * @param listener The result of reading point cloud
     */
    @Override
    public void readPointCloud(File file, ReadPointCloudListener listener) {
        if (! file.exists()) {
            listener.onError(Constants.ERR_CODE_FILE_NOT_FOUND, "File does NOT exist.");
            return;
        }
        try {
            FileReader reader = new FileReader(file);
            Scanner scanner = new Scanner(reader);
            PlyHeader header = readHeader(scanner);
            PcuPointCloud cloud = new PcuPointCloud();
            PlyElement element4Point = header.elementTypes.get("vertex") != null ? header.elementTypes.get("vertex") : header.elementTypes.get("vertices");
            if (header.elementsNumber.size() < 1 || element4Point == null) {
                throw new IllegalStateException("Not a valid header for 3d point cloud.");
            }
            int vertexElementIndex = 0;
            for (vertexElementIndex = 0; vertexElementIndex < header.elementsNumber.size(); vertexElementIndex ++) {
                if (header.elementsNumber.get(vertexElementIndex).getKey().equals("vertex")
                        || header.elementsNumber.get(vertexElementIndex).getKey().equals("vertices")) {
                   break;
                }
            }
            /** read points iteratively **/
            for (int j = 0; j < header.elementsNumber.get(vertexElementIndex).getValue(); j ++) {
                double[] point = new double[3];
                point[0] = scanner.nextDouble();
                point[1] = scanner.nextDouble();
                point[2] = scanner.nextDouble();
                cloud.getPoint3ds().add(point);
            }
            listener.onReadPointCloudSuccessfully(cloud, header);
        } catch (IOException e) {
            e.printStackTrace();
            listener.onError(Constants.ERR_CODE_FILE_FORMAT_ERROR, e.getMessage());
        } catch (IllegalStateException e) {
            e.printStackTrace();
            listener.onError(Constants.ERR_CODE_NOT_3D_PLY, e.getMessage());
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            listener.onError(Constants.ERR_CODE_NOT_ENOUGH_POINTS, e.getMessage());
        }
    }

    @Override
    public void readMesh(String fileName, ReadMeshListener listener) {
    }

    public static class PlyElement {
        /** ["x", "y", "z", "red", "green", "blue"] **/
        private String[] propertiesName;
        /** [float, float, float, uchar, uchar, uchar] **/
        private int[] propertiesType;

        PlyElement(int propertiesNum) {
            this.propertiesName = new String [propertiesNum];
            this.propertiesType = new int [propertiesNum];
        }

        /**
         * because list will be the only property in a element
         * we can use two type-field to describe list
         **/
        private int listType1 = TYPE_NONTYPE;
        private int listType2 = TYPE_NONTYPE;

        public String[] getPropertiesName() {
            return propertiesName;
        }

        public int[] getPropertiesType() {
            return propertiesType;
        }

        public int getListType1() {
            return listType1;
        }

        public int getListType2() {
            return listType2;
        }

    }

    public static class PlyHeader {
        /** FORMAT_ASCII or FORMAT_BINARY **/
        private int plyFormat = FORMAT_NON_FORMAT;

        private float plyVersion = 0;

        /** [("vertex", 12), ("face", 8)] **/
        private List<Pair<String, Integer>> elementsNumber = new ArrayList<>();

        private Map<String, PlyElement> elementTypes = new HashMap<>();

        private int headerBytes = 0;

        public int getPlyFormat() {
            return plyFormat;
        }

        public float getPlyVersion() {
            return plyVersion;
        }

        public List<Pair<String, Integer>> getElementsNumber() {
            return elementsNumber;
        }

        public Map<String, PlyElement> getElementTypes() {
            return elementTypes;
        }

        public int getHeaderBytes() {
            return headerBytes;
        }
    }

}
