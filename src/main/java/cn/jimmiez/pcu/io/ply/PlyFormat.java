package cn.jimmiez.pcu.io.ply;

public enum PlyFormat {
    ASCII("ascii"),
    BINARY_BIG_ENDIAN("binary_big_endian"),
    BINARY_LITTLE_ENDIAN("binary_little_endian");

    private String name;

    PlyFormat(String name) {
        this.name = name;
    }

}
