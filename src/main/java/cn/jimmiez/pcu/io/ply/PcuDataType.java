package cn.jimmiez.pcu.io.ply;

public enum PcuDataType {
    NIL(0, "nil"),
    CHAR(1, "char"),
    UCHAR(1, "uchar"),
    SHORT(2, "short"),
    USHORT(2, "ushort"),
    INT(4, "int"),
    UINT(4, "uint"),
    FLOAT(4, "float"),
    DOUBLE(8, "double");

    private int size = 0;
    private String typeName = "nil";

    PcuDataType(int size, String tn) {
        this.size = size;
        this.typeName = tn;
    }

    public int size() {return size;}

    public String typeName() {return typeName;}

}
