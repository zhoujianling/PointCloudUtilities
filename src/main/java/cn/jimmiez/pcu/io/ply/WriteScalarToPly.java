package cn.jimmiez.pcu.io.ply;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface WriteScalarToPly {

    /**
     * @return the names of properties
     */
    String[] properties() default {};

    /**
     * @return the type of property, see {@link PcuDataType}.
     */
    PcuDataType type() default PcuDataType.UINT;

    /**
     * @return name of current ply-element
     */
    String element() default "null";



}
