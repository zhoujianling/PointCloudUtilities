package cn.jimmiez.pcu.io.ply2;

import cn.jimmiez.pcu.model.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlyHeader2 {

    private int bytesCount = 0;

    private float version = 0f;

    private List<PlyElementHeader> elementHeaders;

    private PlyFormat format = null;

    private List<String> comments;

    public PlyHeader2() {
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

    public PlyElementHeader findElement(String elementName) {
        for (PlyElementHeader header : elementHeaders) {
            if (header.elementName.equals(elementName)) {
                return header;
            }
        }
        throw new IllegalStateException("Cannot find the element " + elementName);
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

        List<Pair<String, PlyPropertyType2>> properties = new ArrayList<>();

        public List<Pair<String, PlyPropertyType2>> getProperties() {
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
