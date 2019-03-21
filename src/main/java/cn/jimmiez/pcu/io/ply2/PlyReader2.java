package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.io.ply.ReadFromPly;
import cn.jimmiez.pcu.util.Pair;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.*;

@SuppressWarnings("Duplicates")
public class PlyReader2 {

    private void readPly(File file, Object object) {
        PlyData plyData = null;
        try {
            plyData = readPlyImpl(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (plyData != null) injectData(plyData, object);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public PlyData readPly(File file) {
        PlyData data = null;
        try {
            data = readPlyImpl(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    private PlyData readPlyImpl(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        Scanner scanner = new Scanner(stream);
        PlyHeader2 header = readHeader(scanner);
        final byte[] bytes = Files.readAllBytes(file.toPath());
        PlyData data = new PlyData(header);
        data.parse(bytes);
        return data;
    }

    private Pair<Integer, List<Integer>> checkAnnotation(ReadFromPly readFromPly, PlyHeader2 header) {
        for (int i = 0; i < header.getElementHeaders().size(); i ++) {
            PlyHeader2.PlyElementHeader elementHeader = header.getElementHeaders().get(i);
            if (! elementHeader.elementName.equals(readFromPly.element())) continue;
            List<Integer> indices = new ArrayList<>();
            for (String propertyName : readFromPly.properties()) {
                int index = elementHeader.findProperty(propertyName);
                if (index == -1) {
                    System.err.println("Cannot recognize the property name in the annotation: " + propertyName);
                    return null;
                }
                indices.add(index);
            }
            return new Pair<>(i, indices);
        }
        System.err.println("Cannot recognize the element name in the annotation: " + readFromPly.element());
        return null;
    }

    private void injectDataImpl(PlyData plyData, List list, ReadFromPly annotation) {
        if (plyData == null || list == null || annotation == null) return;
        if (annotation.properties().length < 1) return;
        list.clear();

        Pair<Integer, List<Integer>> indexPair = checkAnnotation(annotation, plyData.getHeader());
        if (indexPair == null) return;
        List<Integer> propertiesIndices = indexPair.getValue();

        PlyElement2 element = plyData.getElement(annotation.element());
        PlyPropertyType2 propertyType = element.getHeader().getProperties().get(propertiesIndices.get(0)).getValue();
        if (propertyType instanceof PlyPropertyType2.PlyListType) {
            PcuDataType dataType = ((PlyPropertyType2.PlyListType) propertyType).dataType();
//            injectList(element, list, dataType, propertiesIndices);

        } else {
            PcuDataType dataType = ((PlyPropertyType2.PlyScalarType) propertyType).dataType();
            injectScalar(element, list, dataType, propertiesIndices);
        }
    }

//    @SuppressWarnings("unchecked")
//    private void injectList(PlyElement2 element, List list, PcuDataType dataType, List<Integer> propertiesIndices) {
//        int position = 0;
//        int len = propertiesIndices.size();
//        switch (dataType) {
//            case CHAR:
//            case UCHAR:
//                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
//                    byte[] bytes = new byte[len];
//                    for (int j = 0; j < len; j ++) {
//                        double val = element.elementData[position + propertiesIndices.get(j)];
//                        bytes[j] = (byte) val;
//                    }
//                    list.add(bytes);
//                    position += element.getHeader().properties.size();
//                }
//                break;
//            case SHORT:
//            case USHORT:
//                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
//                    short[] shorts = new short[len];
//                    for (int j = 0; j < len; j ++) {
//                        double val = element.elementData[position + propertiesIndices.get(j)];
//                        shorts[j] = (short) val;
//                    }
//                    list.add(shorts);
//                    position += element.getHeader().properties.size();
//                }
//                break;
//            case INT:
//            case UINT:
//                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
//                    int[] vals = new int[len];
//                    for (int j = 0; j < len; j ++) {
//                        double val = element.elementData[position + propertiesIndices.get(j)];
//                        vals[j] = (int) val;
//                    }
//                    list.add(vals);
//                    position += element.getHeader().properties.size();
//                }
//                break;
//            case FLOAT:
//                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
//                    float[] vals = new float[len];
//                    for (int j = 0; j < len; j ++) {
//                        double val = element.elementData[position + propertiesIndices.get(j)];
//                        vals[j] = (float) val;
//                    }
//                    list.add(vals);
//                    position += element.getHeader().properties.size();
//                }
//                break;
//            case DOUBLE:
//                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
//                    double[] vals = new double[len];
//                    for (int j = 0; j < len; j ++) {
//                        double val = element.elementData[position + propertiesIndices.get(j)];
//                        vals[j] = val;
//                    }
//                    list.add(vals);
//                    position += element.getHeader().properties.size();
//                }
//                break;
//        }
//
//    }

    @SuppressWarnings("unchecked")
    private void injectScalar(PlyElement2 element, List list, PcuDataType dataType, List<Integer> propertiesIndices) {
        int position = 0;
        int len = propertiesIndices.size();
        switch (dataType) {
            case CHAR:
            case UCHAR:
                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
                    byte[] bytes = new byte[len];
                    for (int j = 0; j < len; j ++) {
                        double val = element.elementData[position + propertiesIndices.get(j)];
                        bytes[j] = (byte) val;
                    }
                    list.add(bytes);
                    position += element.getHeader().properties.size();
                }
                break;
            case SHORT:
            case USHORT:
                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
                    short[] shorts = new short[len];
                    for (int j = 0; j < len; j ++) {
                        double val = element.elementData[position + propertiesIndices.get(j)];
                        shorts[j] = (short) val;
                    }
                    list.add(shorts);
                    position += element.getHeader().properties.size();
                }
                break;
            case INT:
            case UINT:
                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
                    int[] vals = new int[len];
                    for (int j = 0; j < len; j ++) {
                        double val = element.elementData[position + propertiesIndices.get(j)];
                        vals[j] = (int) val;
                    }
                    list.add(vals);
                    position += element.getHeader().properties.size();
                }
                break;
            case FLOAT:
                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
                    float[] vals = new float[len];
                    for (int j = 0; j < len; j ++) {
                        double val = element.elementData[position + propertiesIndices.get(j)];
                        vals[j] = (float) val;
                    }
                    list.add(vals);
                    position += element.getHeader().properties.size();
                }
                break;
            case DOUBLE:
                for (int lineCount = 0; lineCount < element.getHeader().number; lineCount += 1) {
                    double[] vals = new double[len];
                    for (int j = 0; j < len; j ++) {
                        double val = element.elementData[position + propertiesIndices.get(j)];
                        vals[j] = val;
                    }
                    list.add(vals);
                    position += element.getHeader().properties.size();
                }
                break;
        }

    }

    private <T> void injectData(PlyData data, Object userDefinedEntity) throws InvocationTargetException, IllegalAccessException {
        if (userDefinedEntity == null) return;
        Method[] allMethods = userDefinedEntity.getClass().getDeclaredMethods();
        for (Method method : allMethods) {
            ReadFromPly annotation = method.getAnnotation(ReadFromPly.class);
            if (method.getReturnType() != List.class) {
                System.err.println("Not a valid getter."); continue;
            }
            List list = (List) method.invoke(userDefinedEntity);
            injectDataImpl(data, list, annotation);
        }
    }


    public <T> T read(File file, Class<T> clazz) {
        T object = null;
        try {
            object = clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        readPly(file, object);
        return object;
    }

    private PlyHeader2 readHeader(Scanner scanner) throws IOException {
        PlyHeader2 header = new PlyHeader2();
        List<String> headerLines = new ArrayList<>();
        int byteCount = 0;
        try {
            String line;
            while ((line = scanner.nextLine()) != null) {
                byteCount += (line.getBytes().length + 1);
                if (line.equals("end_header")) break;
                if (line.startsWith("comment ")) {
                    header.getComments().add(line);
                    continue;
                }
                headerLines.add(line);
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Invalid ply file: Cannot find end of header.");
        }
        if (headerLines.size() < 1) {
            throw new IOException("Invalid ply file: No data");
        }
        header.setBytesCount(byteCount);
        String firstLine = headerLines.get(0);
        if (! firstLine.equals(Constants.MAGIC_STRING)) {
            throw new IOException("Invalid ply file: Ply file does not start with ply.");
        }
        String secondLine = headerLines.get(1);
        readPlyFormat(secondLine, header);
        for (int lineNo = 2; lineNo < headerLines.size();) {
            String elementLine = headerLines.get(lineNo);
            PlyHeader2.PlyElementHeader element = new PlyHeader2.PlyElementHeader();
            Pair<String, Integer> pair = readPlyElement(elementLine);
            element.setNumber(pair.getValue());
            element.setElementName(pair.getKey());
            lineNo += 1;
            int propertyStartNo = lineNo;
            while (lineNo < headerLines.size() && headerLines.get(lineNo).startsWith("property ")) lineNo++;
            for (int i = propertyStartNo; i < lineNo; i ++) {
                String[] propertySlices = headerLines.get(i).split(" ");
                if (propertySlices.length < 3) throw new IOException("Invalid ply file.");
                element.getProperties().add(parseProperty(headerLines.get(i)));
            }
            header.getElementHeaders().add(element);
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

    private PcuDataType parseType(String type) throws IOException {
        switch (type) {
            case "char":
            case "int8":
                return PcuDataType.CHAR;
            case "uchar":
            case "uint8":
                return PcuDataType.UCHAR;
            case "int":
            case "int32":
                return PcuDataType.INT;
            case "uint":
            case "uint32":
                return PcuDataType.UINT;
            case "short":
            case "int16":
                return PcuDataType.SHORT;
            case "ushort":
            case "uint16":
                return PcuDataType.USHORT;
            case "float":
            case "float32":
                return PcuDataType.FLOAT;
            case "double":
            case "float64":
                return PcuDataType.DOUBLE;
        }
        throw new IOException("Cannot parse type: " + type);
    }
//
    @SuppressWarnings("SpellCheckingInspection")
    public Pair<String, PlyPropertyType2> parseProperty(String line) throws IOException {
        String[] propertySlices = line.split("(\\s)+");
        String propertyName = propertySlices[propertySlices.length - 1];
        PlyPropertyType2 propertyType;
        if (propertySlices[1].equals("list")) {
            if (propertySlices.length < 5) throw new IOException("Too less properties for list type: " + propertyName);
            final PcuDataType sizeType = parseType(propertySlices[2]);
            final PcuDataType dataType = parseType(propertySlices[3]);
            propertyType = new PlyPropertyType2.PlyListType() {
                @Override
                public PcuDataType sizeType() {
                    return sizeType;
                }

                @Override
                public PcuDataType dataType() {
                    return dataType;
                }
            };
        } else {
            if (propertySlices.length < 3) throw new IOException("Too less properties for scalar type: " + propertyName);
            final PcuDataType type = parseType(propertySlices[1]);
            propertyType = new PlyPropertyType2.PlyScalarType() {
                @Override
                public PcuDataType dataType() {
                    return type;
                }
            };
        }

        return new Pair<>(propertyName, propertyType);
    }

    private void readPlyFormat(String line, PlyHeader2 header) throws IOException {
        if (!line.startsWith("format ")) {
            throw new IOException("Invalid ply file: No format information");
        }
        String[] formatSlices = line.split(" ");
        if (formatSlices.length == 3) {
            switch (formatSlices[1]) {
                case "ascii":
                    header.setFormat(PlyFormat.ASCII);
                    break;
                case "binary_little_endian":
                    header.setFormat(PlyFormat.BINARY_LITTLE_ENDIAN);
                    break;
                case "binary_big_endian":
                    header.setFormat(PlyFormat.BINARY_BIG_ENDIAN);
                    break;
            }
            header.setVersion(Float.valueOf(formatSlices[2]));
        } else {
            throw new IOException("Invalid ply file: Wrong format ply in line");
        }
    }
}
