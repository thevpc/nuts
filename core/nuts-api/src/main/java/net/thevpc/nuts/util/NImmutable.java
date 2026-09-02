package net.thevpc.nuts.util;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that instances of the annotated class or interface are immutable
 * after instantiation, ensuring thread-safety and side-effect-free sharing.
 *
 * @app.category Utility
 * @since 0.5.4
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NImmutable {
}
