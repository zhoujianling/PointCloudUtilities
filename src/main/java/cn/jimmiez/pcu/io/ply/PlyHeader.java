package cn.jimmiez.pcu.io.ply;


import cn.jimmiez.pcu.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlyHeader {
    /** FORMAT_ASCII or FORMAT_BINARY **/
    private int plyFormat = PlyReader.FORMAT_NON_FORMAT;

    private float plyVersion = 0;

    /** [("vertex", 12), ("face", 8)] **/
    private List<Pair<String, Integer>> elementsNumber = new ArrayList<>();

    private Map<String, PlyElement> elementTypes = new HashMap<>();

    private int headerBytes = 0;

    public int getPlyFormat() {
        return plyFormat;
    }

    public void setPlyFormat(int format) {
        this.plyFormat = format;
    }

    public float getPlyVersion() {
        return plyVersion;
    }


    public void setPlyVersion(float v) {
        this.plyVersion = v;
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

    public void setHeaderBytes(int b) {
        this.headerBytes = b;
    }
}
