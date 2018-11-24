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
 * {@literal public List<float[]> points() { ... }}
 *
 * More details about OFF file format can be found in:
 * https://people.sc.fsu.edu/~jburkardt/data/off/off.html
 *
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ReadFromOff {

    int dataType() default VERTICES;

    int VERTICES = 0;
    int FACES = 1;
    int VERTEX_COLORS = 2;
    int FACE_COLORS = 3;

}
