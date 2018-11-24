package cn.jimmiez.pcu.io.ply;

public enum PlyPropertyType {

    NON_TYPE(0, "NotType"),
    CHAR(1, "char"),
    UCHAR(1, "uchar"),
    SHORT(2, "short"),
    USHORT(2, "ushort"),
    INT(4, "int"),
    UINT(4, "uint"),
    FLOAT(4, "float"),
    DOUBLE(8, "double"),

    // list type
    LIST(0, "list");

    private int size = 0;
    private String typeName = "NotType";

    PlyPropertyType(int size, String tn) {
        this.size = size;
        this.typeName = tn;
    }

    public int size() {return size;}

    public String typeName() {return typeName;}
}
