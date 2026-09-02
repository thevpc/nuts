package net.thevpc.nuts.elem;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Customizes object-to-element mapping and serialization for fields, parameters, and types.
 * <p>
 * Allows specifying a custom mapper class or overriding the target property name
 * used when converting between Java objects and Nuts elements (JSON, TSON, YAML).
 *
 * @author thevpc
 * @since 0.8.0
 */
@Target({ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface NMapBy {
    /**
     * Custom mapping class responsible for converting the element.
     *
     * @return custom mapper class
     */
    Class<?> mapClass() default void.class;

    /**
     * Target property name in the serialized element representation.
     *
     * @return serialized property name
     */
    String name() default "";
}
