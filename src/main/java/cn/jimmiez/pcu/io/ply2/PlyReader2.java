package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.io.ply.ReadFromPly;
import cn.jimmiez.pcu.util.Pair;
import cn.jimmiez.pcu.util.PcuReflectUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

@SuppressWarnings("Duplicates")
public class PlyReader2 {

    public PlyData readPlyData(File file) throws IOException {
        FileInputStream stream = new FileInputStream(file);
        Scanner scanner = new Scanner(stream);
        PlyHeader2 header = readHeader(scanner);
        PlyData data = new PlyData(file, header);
        return data;
    }

    private List<Method> findAllElementGetter(List<Method> methods) {
        List<Method> getters = new ArrayList<>();
        for (Method m : methods) {
            ReadFromPly pcuEle = m.getAnnotation(ReadFromPly.class);
            if (pcuEle == null) continue;
            getters.add(m);
        }
        return getters;
    }


//    private <T> void injectData(List<List> dataContainer, List<Integer> arraySizes, List<List<int[]>> pointerList, PlyData data) throws InvocationTargetException, IllegalAccessException {
//        byte[] byteBuffer = null;
//        short[] shortBuffer = null;
//        int[] intBuffer = null;
//        float[] floatBuffer = null;
//        double[] doubleBuffer = null;
//        int elementPointer = 0;
//        for (PlyElement2 element2 : data) {
//            int linePointer = 0;
//            PlyHeader2.PlyElementHeader elementHeader = data.getHeader().getElementHeaders().get(elementPointer);
//            int[] sizes = initBufferSize(elementHeader);
//            byteBuffer = new byte[sizes[0]];
//            shortBuffer = new short[sizes[1]];
//            intBuffer = new int[sizes[2]];
//            floatBuffer = new float[sizes[3]];
//            doubleBuffer = new double[sizes[4]];
//            int propertyNumber = elementHeader.getProperties().size();
//            for (PlyProperties properties : element2) {
//                int propertyPointer = 0;
//                for (; propertyPointer < propertyNumber; propertyPointer ++) {
//                    int[] positions = pointerList.get(elementPointer).get(propertyPointer);
//
//                }
//                PlyPropertyType2 type = elementHeader.getProperties().get(linePointer).getValue();
//                linePointer += 1;
//            }
//            elementPointer += 1;
//        }
//    }
//
//    private int[] initBufferSize(PlyHeader2.PlyElementHeader header) {
//        int[] sizes = new int[5];
//        sizes[0] = 0; sizes[1] = 0; sizes[2] = 0; sizes[3] = 0; sizes[4] = 0;
//        for (int i = 0; i < header.getProperties().size(); i ++) {
//            PlyPropertyType2 type = header.getProperties().get(i).getValue();
//            if (type instanceof PlyPropertyType2.PlyScalarType) {
//                PlyPropertyType2.PlyScalarType scalarType = (PlyPropertyType2.PlyScalarType) type;
//                switch (scalarType.dataType()) {
//                    case CHAR:      sizes[0] += 1;  break;
//                    case SHORT:     sizes[1] += 1;  break;
//                    case INT:       sizes[2] += 1;  break;
//                    case FLOAT:     sizes[3] += 1;  break;
//                    case DOUBLE:    sizes[4] += 1;  break;
//                }
//            }
//        }
//        return sizes;
//    }

//    private void putValue(List<List> dataContainer,)

    private <T> T reflect(PlyHeader2 header, PlyData data, Class<T> clazz) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T object = clazz.newInstance();
        List<Method> allMethods = PcuReflectUtil.fetchAllMethods(object);
        List<Method> methods = findAllElementGetter(allMethods);

        for (Method method : methods) {
            ReadFromPly annotation = method.getAnnotation(ReadFromPly.class);
            PlyHeader2.PlyElementHeader elementHeader = header.findElement(annotation.element());
            if (elementHeader == null) continue;
            List list = (List) method.invoke(object);
            if (list == null) continue;
//            String[] elementNames = annotation.element();
        }
        return object;
    }

    public <T> T read(File file, Class<T> clazz) {
        T object = null;
        try {
            FileInputStream stream = new FileInputStream(file);
            Scanner scanner = new Scanner(stream);
            PlyHeader2 header = readHeader(scanner);
            PlyData data = new PlyData(file, header);
            stream.close();
            object = reflect(header, data, clazz);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
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
