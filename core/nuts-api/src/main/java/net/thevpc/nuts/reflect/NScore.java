package net.thevpc.nuts.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * NScore @interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@Target({ElementType.TYPE,ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NScore {
    int fixed() default Integer.MIN_VALUE;
    Class<NScorable> custom() default NScorable.class;
}
