package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a public, no-argument application method to run during uninstallation.
 *
 * <p>The default runtime invokes the method when the application is handled in
 * {@link NApplicationMode#UNINSTALL uninstall} mode. The method may be an instance
 * method and should reverse application-managed installation work as appropriate.</p>
 *
 * @since 0.8.7
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppUninstall {

}
