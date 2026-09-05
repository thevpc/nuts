package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public, no-argument application method to run during installation.
 *
 * <p>The default runtime invokes the method when the application is handled in
 * {@link NApplicationMode#INSTALL install} mode. The method may be an instance
 * method; it need not be static.</p>
 *
 * @since 0.5.4
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppInstall {

}
