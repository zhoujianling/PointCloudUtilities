package cn.jimmiez.pcu.io.ply;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteListToPly {

    /**
     * @return the name of this list-type property
     */
    String property() default "vertex_index";

    /**
     * @return the type of list size, see {@link PlyPropertyType}.
     */
    PlyPropertyType sizeType() default PlyPropertyType.UCHAR;

    /**
     * @return the type of values in the list, see {@link PlyPropertyType}.
     */
    PlyPropertyType valType() default PlyPropertyType.INT;

    /**
     * @return the name of current ply-element
     */
    String element() default "null";

}
