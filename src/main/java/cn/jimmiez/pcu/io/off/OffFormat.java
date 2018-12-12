package cn.jimmiez.pcu.io.off;

import java.io.FileOutputStream;

public class OffFormat {

    private String name;

    private boolean hasNormal;

    private boolean hasColor;

    private boolean hasTextureCoordinates;

    private boolean has4Components;

    private boolean isDimensionSpecified;

    private int dimension = 3;

//    public static OffFormat parse(String name) {
//        if (name == null) {
//            System.err.println("Warning: try to parse a null string as OffFormat");
//            return null;
//        }
//        String upper = name.trim().toUpperCase();
//        throw new IllegalStateException("Unsupported type");
//    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean hasNormal() {
        return hasNormal;
    }

    public boolean hasColor() {
        return hasColor;
    }

    public boolean has4Components() {
        return has4Components;
    }

    public boolean hasTextureCoordinates() {
        return hasTextureCoordinates;
    }

    public boolean isDimensionSpecified() {
        return isDimensionSpecified;
    }

    public int getDimension() {
        return dimension;
    }


    private OffFormat() {}

    public static class OffFormatBuilder {
        private OffFormat format = null;

        public OffFormatBuilder() {
            format = new OffFormat();
        }


        public void hasNormal(boolean hasNormal) {
            format.hasNormal = hasNormal;
        }

        public void has4Components(boolean has4Components) {
            format.has4Components = has4Components;
        }

        public void dimensionSpecified(boolean dimensionSpecified) {
            format.isDimensionSpecified = dimensionSpecified;
        }

        public void hasTextureCoordinates(boolean hasTextureCoordinates) {
            format.hasTextureCoordinates = hasTextureCoordinates;
        }

        public void hasColor(boolean hasColor) {
            format.hasColor = hasColor;
        }

        public void dimension(int di) {
            format.dimension = di;
        }

        public OffFormat get() {
            return format;
        }
    }
}
