package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public, no-argument application method to run during an update.
 *
 * <p>The default runtime invokes the method when the application is handled in
 * {@link NApplicationMode#UPDATE update} mode. The method may be an instance
 * method and is responsible for any application-specific update work.</p>
 *
 * @since 0.8.7
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppUpdate {

}
