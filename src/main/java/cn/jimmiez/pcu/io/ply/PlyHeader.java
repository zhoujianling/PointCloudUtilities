package cn.jimmiez.pcu.io.ply;

import cn.jimmiez.pcu.util.Pair;

import java.util.ArrayList;
import java.util.List;

public class PlyHeader {

    private int bytesCount = 0;

    private float version = 0f;

    private List<PlyElementHeader> elementHeaders;

    private PlyFormat format = null;

    private List<String> comments;

    public PlyHeader() {
        elementHeaders = new ArrayList<>();
        comments = new ArrayList<>();
    }

    public void setBytesCount(int bytesCount) {
        this.bytesCount = bytesCount;
    }

    public int getBytesCount() {return this.bytesCount;}

    public void setVersion(float f) {
        this.version = f;
    }

    public int findElement(String elementName) {
        for (int i = 0; i < elementHeaders.size(); i ++) {
            PlyElementHeader header = elementHeaders.get(i);
            if (header.elementName.equals(elementName)) {
                return i;
            }
        }
        return -1;
    }

    public PlyFormat getFormat() {
        return format;
    }

    public void setFormat(PlyFormat format) {
        this.format = format;
    }

    public List<PlyElementHeader> getElementHeaders() {
        return elementHeaders;
    }

    public List<String> getComments() {
        return comments;
    }

    public static class PlyElementHeader {

        int number = 0;

        String elementName;

        List<Pair<String, PlyPropertyType>> properties = new ArrayList<>();

        public int findProperty(String key) {
            for (int i = 0; i < properties.size(); i ++) {
                Pair<String, PlyPropertyType> pair = properties.get(i);
                if (pair.getKey().equals(key)) {
                    return i;
                }
            }
            return -1;
        }

        public List<Pair<String, PlyPropertyType>> getProperties() {
            return properties;
        }

        public String getElementName() {
            return elementName;
        }

        public void setElementName(String name) {
            this.elementName = name;
        }

        public int getNumber() {return number;}

        public void setNumber(int number) {
            this.number = number;
        }
    }

}
