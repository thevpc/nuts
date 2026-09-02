package net.thevpc.nuts.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the default fallback implementation or value name for a type or interface.
 * <p>
 * When Nuts reflection or component resolution searches for an implementation
 * without specific criteria, this default value is chosen.
 *
 * @author thevpc
 * @since 0.8.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NDefaultsTo {
    /**
     * The name or identifier of the default implementation.
     *
     * @return default implementation name
     */
    String value();
}
