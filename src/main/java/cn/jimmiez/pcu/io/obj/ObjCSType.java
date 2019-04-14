package cn.jimmiez.pcu.io.obj;

public enum ObjCSType {

    BASIC_MATRIX("bmatrix"),
    BEZIER("bezier"),
    B_SPLINE("bspline"),
    CARDINAL("cardinal"),
    TAYLOR("taylor"),
    ;

    private String keyword;

    ObjCSType(String keyword) {
        this.keyword = keyword;
    }

}
