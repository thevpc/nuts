package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public, no-argument application method as a command-completion entry
 * point.
 *
 * <p>The default runtime invokes the method when the application is handled in
 * {@link NApplicationMode#COMPLETE completion} mode. The method should inspect the
 * current command line and publish completion candidates through the command-line
 * API. It may be an instance method.</p>
 *
 * @since 1.0.0
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppComplete {

}
