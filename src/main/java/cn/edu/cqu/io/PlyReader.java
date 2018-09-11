package cn.edu.cqu.io;

import javafx.util.Pair;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.util.*;

import static cn.edu.cqu.Constants.ERR_CODE_FILE_NOT_FOUND;
import static cn.edu.cqu.Constants.MAGIC_STRING;

public class PlyReader implements PointCloudReader, MeshReader{

    public PlyHeader readHeaderThenCloseFile(File file) throws IOException {
        PlyHeader result;
        FileReader reader = new FileReader(file);
        result = readHeader(reader);
        reader.close();
        return result;
    }

    public PlyHeader readHeader(FileReader reader) throws IOException {
        PlyHeader header = new PlyHeader();
        Scanner scanner = new Scanner(reader);
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
        if (! firstLine.equals(MAGIC_STRING)) {
            throw new IOException("Invalid ply file: Ply file does not start with ply.");
        }
        String secondLine = headerLines.get(1);
        readPlyFormat(secondLine, header);
        for (int lineNo = 2; lineNo < headerLines.size(); lineNo ++) {
            String elementLine = headerLines.get(lineNo);
            Pair<String, Integer> pair = readPlyElement(elementLine);
            lineNo += 1;
            int propertyStartNo = lineNo;
            while (headerLines.get(lineNo ++).startsWith("property "));
            PlyElement element = new PlyElement(lineNo - propertyStartNo);
            for (int i = propertyStartNo; i < lineNo; i ++) {
                String[] propertySlices = headerLines.get(i).split(" ");
                if (propertySlices.length < 3) throw new IOException("Invalid ply file.");
                element.propertiesName[i - propertyStartNo] = propertySlices[propertySlices.length - 1];
                if (propertySlices[1].equals("char")) {
                    element.propertiesType[i - propertyStartNo] = TYPE_CHAR;
                } else if (propertySlices[1].equals("uchar")) {
                    element.propertiesType[i - propertyStartNo] = TYPE_UCHAR;
                } else if (propertySlices[1].equals("int")) {
                } else if (propertySlices[1].equals("uint")) {
                } else if (propertySlices[1].equals("float")) {
                } else if (propertySlices[1].equals("double")) {
                } else if (propertySlices[1].equals("list")) {
                }

            }
            header.elementTypes.put(pair.getKey(), element);

        }
        return header;
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

    private void readPlyProperty(String line, PlyHeader header) {

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

    @Override
    public void readPointCloud(File file, ReadPointCloudListener listener) {
        if (! file.exists()) {
            listener.onError(ERR_CODE_FILE_NOT_FOUND, "File does NOT exist.");
            return;
        }

    }

    @Override
    public void readMesh(String fileName, ReadMeshListener listener) {
        File f;
        FileReader r;
    }

    public static final Integer FORMAT_ASCII = 0x3001;
    public static final Integer FORMAT_BINARY_BIG_ENDIAN = 0x3002;
    public static final Integer FORMAT_BINARY_LITTLE_ENDIAN = 0x3003;

    public static final Integer FORMAT_NON_FORMAT = - 0x3001;

    public static final Integer TYPE_LIST = 0x1001;

    public static final Integer TYPE_CHAR = 0x0001;
    public static final Integer TYPE_UCHAR = 0x0002;
    public static final Integer TYPE_SHORT = 0x0003;
    public static final Integer TYPE_USHORT = 0x0004;
    public static final Integer TYPE_INT = 0x0005;
    public static final Integer TYPE_UINT = 0x0006;
    public static final Integer TYPE_FLOAT = 0x0007;
    public static final Integer TYPE_DOUBLE = 0x0008;

    public static final Integer TYPE_NONTYPE = - 0x0001;

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

        public void setPropertiesName(String[] propertiesName) {
            this.propertiesName = propertiesName;
        }

        public int[] getPropertiesType() {
            return propertiesType;
        }

        public void setPropertiesType(int[] propertiesType) {
            this.propertiesType = propertiesType;
        }

        public int getListType1() {
            return listType1;
        }

        public void setListType1(int listType1) {
            this.listType1 = listType1;
        }

        public int getListType2() {
            return listType2;
        }

        public void setListType2(int listType2) {
            this.listType2 = listType2;
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

        public void setPlyFormat(int plyFormat) {
            this.plyFormat = plyFormat;
        }

        public float getPlyVersion() {
            return plyVersion;
        }

        public void setPlyVersion(float plyVersion) {
            this.plyVersion = plyVersion;
        }

        public List<Pair<String, Integer>> getElementsNumber() {
            return elementsNumber;
        }

        public void setElementsNumber(List<Pair<String, Integer>> elementsNumber) {
            this.elementsNumber = elementsNumber;
        }

        public Map<String, PlyElement> getElementTypes() {
            return elementTypes;
        }

        public int getHeaderBytes() {
            return headerBytes;
        }
    }

}
