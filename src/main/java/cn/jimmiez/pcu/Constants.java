package cn.jimmiez.pcu;

public class Constants {
    public static final Integer ERR_CODE_NO_ERROR = 200;

    /** err code for reading a ply **/
    public static final Integer ERR_CODE_FILE_NOT_FOUND = 404;
    public static final Integer ERR_CODE_FILE_HEADER_FORMAT_ERROR = 500;
    public static final Integer ERR_CODE_FILE_DATA_FORMAT_ERROR = 501;
    public static final Integer ERR_CODE_NOT_3D_PLY = 301;
    public static final Integer ERR_CODE_NOT_ENOUGH_POINTS = 302;
    public static final Integer ERR_CODE_PRIVATE_METHOD = 601;
    public static final Integer ERR_CODE_METHOD_NO_LIST = 602;

    /** err code for reading a ply **/
    public static final Integer ERR_CODE_BAD_WRITE_REQUEST = 701;

    public static final String MAGIC_STRING = "ply";
}
