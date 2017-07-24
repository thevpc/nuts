package net.vpc.app.nuts.util;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by vpc on 6/23/17.
 */
@Target({METHOD,FIELD})
@Retention(RUNTIME)
public @interface JsonTransient {
}
