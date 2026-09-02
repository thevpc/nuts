package net.thevpc.nuts.reflect;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as a component or object factory method.
 * <p>
 * Nuts reflection mechanisms recognize annotated methods as instance creators
 * when constructing and wiring services, components, or mapped objects.
 *
 * @author thevpc
 * @since 0.8.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NFactoryMethod {
}
