package cn.jimmiez.pcu.model;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * User can decorate a getter in their entity class with this annotation,
 * then PCU will inject the real values into this field
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PcuElement {

    /**
     * the properties list of this element
     */
    String[] properties() default {};

    String[] alternativeNames() default {};
}
