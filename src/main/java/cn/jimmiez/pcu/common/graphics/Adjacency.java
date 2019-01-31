package cn.jimmiez.pcu.common.graphics;

/**
 * This class enumerates different ways that two boxes(cell) are adjacent to each other
 */
public enum Adjacency {

    /** the two boxes has one common face **/
    FACE,

    /** the two boxes has at least one common edge **/
    EDGE,

    /** the two boxes has at least one common vertex **/
    VERTEX

}
