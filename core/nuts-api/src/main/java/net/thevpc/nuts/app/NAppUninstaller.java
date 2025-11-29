package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an application uninstaller entry point.
 * <p>
 * Methods annotated with {@code @NAppUninstaller} are intended to perform
 * clean-up or removal operations for the application, such as deleting
 * installed files, unregistering resources, or reversing changes applied
 * during installation.
 * </p>
 * <p>
 * Typically, the framework or runtime will detect and invoke these methods
 * when an uninstallation process is triggered. The annotated method should
 * be public, and static if required by the framework, and should handle
 * the uninstallation logic safely.
 * </p>
 *
 * @author thevpc
 * @app.category Annotation
 * @since 0.8.7
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppUninstaller {

}
