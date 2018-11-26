package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.Constants;
import cn.jimmiez.pcu.io.ply.PlyElement;
import cn.jimmiez.pcu.io.ply.PlyHeader;
import cn.jimmiez.pcu.io.ply.PlyPropertyType;
import cn.jimmiez.pcu.io.ply2.PlyData;
import cn.jimmiez.pcu.model.Pair;
import cn.jimmiez.pcu.model.PcuPointCloud3f;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

import static cn.jimmiez.pcu.io.ply.PlyReader.*;

@SuppressWarnings("Duplicates")
public class PlyReader2 {



    public PcuPointCloud3f readPointCloud(File file, Class<PcuPointCloud3f> clazz) {
        PcuPointCloud3f object = null;
        try {
            object = clazz.newInstance();
            FileInputStream stream = new FileInputStream(file);
            Scanner scanner = new Scanner(stream);
            PlyHeader header = readHeader(scanner);
            PlyData data = new PlyData(file, header);
            stream.close();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return object;
    }

    private PlyHeader readHeader(Scanner scanner) throws IOException {
        PlyHeader header = new PlyHeader();
        List<String> headerLines = new ArrayList<>();
        int byteCount = 0;
        try {
            String line;
            while ((line = scanner.nextLine()) != null) {
                byteCount += (line.getBytes().length + 1);
                if (line.equals("end_header")) break;
                if (line.startsWith("comment ")) continue;
                headerLines.add(line);
            }
        } catch (NoSuchElementException e) {
            throw new IOException("Invalid ply file: Cannot find end of header.");
        }
        if (headerLines.size() < 1) {
            throw new IOException("Invalid ply file: No data");
        }
        header.setHeaderBytes(byteCount);
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
            PlyElement element = new PlyElement();
            for (int i = propertyStartNo; i < lineNo; i ++) {
                String[] propertySlices = headerLines.get(i).split(" ");
                if (propertySlices.length < 3) throw new IOException("Invalid ply file.");
                element.getPropertiesName().add(propertySlices[propertySlices.length - 1]);
                element.getPropertiesType().add(recognizeType(propertySlices[1]));
                if (element.getPropertiesType().get(i - propertyStartNo) == PlyPropertyType.LIST) {
                    if (propertySlices.length < 5) throw new IOException("Invalid ply file. Wrong list property.");
                    PlyPropertyType[] types = new PlyPropertyType[] {recognizeType(propertySlices[2]), recognizeType(propertySlices[3])};
                    element.getListTypes().put(element.getPropertiesName().get(i - propertyStartNo), types);
                }
            }
            header.getElementTypes().put(pair.getKey(), element);
            header.getElementsNumber().add(pair);
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

    @SuppressWarnings("SpellCheckingInspection")
    public static PlyPropertyType recognizeType(String type) {
        switch (type) {
            case "char":
            case "int8":
                return PlyPropertyType.CHAR;
            case "uchar":
            case "uint8":
                return PlyPropertyType.UCHAR;
            case "int":
            case "int32":
                return PlyPropertyType.INT;
            case "uint":
            case "uint32":
                return PlyPropertyType.UINT;
            case "short":
            case "int16":
                return PlyPropertyType.SHORT;
            case "ushort":
            case "uint16":
                return PlyPropertyType.USHORT;
            case "float":
            case "float32":
                return PlyPropertyType.FLOAT;
            case "double":
            case "float64":
                return PlyPropertyType.DOUBLE;
            case "list":
                return PlyPropertyType.LIST;
        }
        return PlyPropertyType.NON_TYPE;
    }

    private void readPlyFormat(String line, PlyHeader header) throws IOException {
        if (!line.startsWith("format ")) {
            throw new IOException("Invalid ply file: No format information");
        }
        String[] formatSlices = line.split(" ");
        if (formatSlices.length == 3) {
            switch (formatSlices[1]) {
                case "ascii":
                    header.setPlyFormat(FORMAT_ASCII);
                    break;
                case "binary_little_endian":
                    header.setPlyFormat(FORMAT_BINARY_LITTLE_ENDIAN);
                    break;
                case "binary_big_endian":
                    header.setPlyFormat(FORMAT_BINARY_BIG_ENDIAN);
                    break;
            }
            header.setPlyVersion(Float.valueOf(formatSlices[2]));
        } else {
            throw new IOException("Invalid ply file: Wrong format ply in line");
        }
    }

}
