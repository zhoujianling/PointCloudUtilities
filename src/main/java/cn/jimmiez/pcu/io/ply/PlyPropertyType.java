package cn.jimmiez.pcu.io.ply;

public enum PlyPropertyType {

    NON_TYPE(0),
    CHAR(1),
    UCHAR(2),
    SHORT(3),
    USHORT(4),
    INT(5),
    UINT(6),
    FLOAT(7),
    DOUBLE(8),
    LIST(10);

    private int val = 0;

    PlyPropertyType(int val) {
        this.val = val;
    }

    public int val() {return val;}

}
