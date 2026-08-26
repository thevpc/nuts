package net.thevpc.nuts.reflect;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NFactoryMethod @interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NFactoryMethod {
}
