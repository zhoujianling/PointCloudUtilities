package cn.jimmiez.pcu.io.off;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ReadFromOff is used to annotate a getter in your point cloud class.
 * This getter should return a list composed of some arrays, where each
 * array should have three floats by default. e.g.
 *
 * public List&lt;float[]&lt; points() { ... }
 *
 * More details about OFF file format can be found in:
 * https://people.sc.fsu.edu/~jburkardt/data/off/off.html
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadFromOff {

    /**
     * a vertex in mesh is an x-y-z point by default
     * if enableRGBA() returns true, the vertex is x-y-z-r-g-b-a
     * @return if RGBA info is added to vertex
     */
    boolean enableRGBA() default false;

}
