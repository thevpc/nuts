package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public, no-argument application method as a normal execution entry point.
 *
 * <p>The default runtime invokes the method when the application is handled in
 * {@link NApplicationMode#RUN run} mode. The method may be an instance method;
 * application command-line state is obtained from the current Nuts context rather
 * than through method parameters.</p>
 *
 * @since 0.8.7
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppRun {

}
