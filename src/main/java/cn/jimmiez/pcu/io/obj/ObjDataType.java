package cn.jimmiez.pcu.io.obj;

public enum ObjDataType {
    // vertex data
    V_GEOMETRIC_VERTICES("v"),
    VT_TEXTURE_VERTICES("vt"),
    VN_VERTEX_NORMALS("vn"),
    VP_PARAMETER_SPACE_VERTICES("vp"),

    // free form curve/surface attributes
    CSTYPE_RATIONAL_FORMS_OF_CURVE_ETC("cstype"),
    DEG_DEGREE("deg"),
    BMAT_BASIS_MATRIX("bmat"),
    STEP_STEP_SIZE("step"),

    // elements
    P_POINT("p"),
    L_LINE("l"),
    F_FACE("f"),
    CURV_CURVE("curv"),
    CURV2_2D_CURVE("curv2"),
    SURF_SURFACE("surf"),

    // free-form curve/surface body statements
    PARM_PARAMETER_VALUES("parm"),
    TRIM_OUTER_TRIMMING_LOOP("trim"),
    HOLE_INNER_TRIMMING_LOOP("hole"),
    SCRV_SEPECIAL_CURVE("scrv"),
    SP_SPECIAL_POINT("sp"),
    END_END_STATEMENT("end"),

    // connectivity between free-form surfaces
    CON_CONNECT("con"),

    // grouping
    G_GROUP_NAME("g"),
    S_SMOOTHING_GROOP("s"),
    MG_MERGING_GROUP("mg"),
    O_OBJECT_NAME("o"),

    // display/render attributes
    BEVEL_BEVEL_INTERPOLATION("bevel"),
    C_INTERP_COLOR_INTERPOLATION("c_interp"),
    D_INTERP_DISSOLVE_INTERPOLATION("d_interp"),
    LOD_LEVEL_OF_DETAIL("lod"),
    USEMTL_MATERIAL_NAME("usemtl"),
    MTLLIB_MATERIAL_LIBRARY("mtllib"),
    SHADOW_OBJ_SHADOW_CASTING("shadow_obj"),
    TRACE_OBJ_RAY_TRACING("trace_obj"),
    CTECH_CURVE_APPROXIMATION_TECHNIQUE("ctech"),
    STECH_SURFACE_APPROXIMATION_TECHNIQUE("stech"),

    //
    COMMENT_COMMENT("#");

    private String keyword;

    ObjDataType(String keyword) {
        this.keyword = keyword;
    }

    public String getKeyword() {
        return keyword;
    }
}

