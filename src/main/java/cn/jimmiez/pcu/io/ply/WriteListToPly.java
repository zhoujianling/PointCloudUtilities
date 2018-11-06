package cn.jimmiez.pcu.io.ply;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteListToPly {

    /**
     * @return the properties describing the vertex
     */
    String[] properties() default {};

    /**
     * @return the type of list size, e.g., "uchar"
     */
    String sizeType() default "uchar";

    /**
     * @return the type of values in the list, e.g., "uint"
     */
    String valType() default "int";

    /**
     * once you specify the name of element, the ply reader will extract data of this element from ply file.
     * Vertices data will be added into the List as an array by default,
     */
    String element() default "null";

}
