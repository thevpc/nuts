package net.thevpc.nuts.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NDefaultsTo @interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface NDefaultsTo {
    /**
     * Value.
     *
     * @return value result
     */
    String value();
}
