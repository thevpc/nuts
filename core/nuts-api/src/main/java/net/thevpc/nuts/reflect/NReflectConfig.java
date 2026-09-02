package net.thevpc.nuts.reflect;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Configures reflection and property access strategies for types and methods.
 * <p>
 * Controls how getters, setters, fields, and constructors are discovered and
 * mapped during serialization, deserialization, and bean manipulation.
 *
 * @author thevpc
 * @since 0.8.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NReflectConfig {
    /**
     * Property access strategies to apply.
     *
     * @return array of property access strategies
     */
    NReflectPropertyAccessStrategy[] strategy() default {};
}
