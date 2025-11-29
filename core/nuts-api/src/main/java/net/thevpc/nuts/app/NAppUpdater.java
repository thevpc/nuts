package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an application updater entry point.
 * <p>
 * Methods annotated with {@code @NAppUpdater} are intended to perform
 * update operations for the application, such as downloading new versions,
 * applying patches, or refreshing internal resources.
 * </p>
 * <p>
 * Typically, the framework or runtime will detect and invoke these methods
 * when an update process is triggered. The annotated method should be
 * public, static (if required by the framework), and accept the expected
 * parameters defined by the update mechanism.
 * </p>
 *
 * @author thevpc
 * @app.category Annotation
 * @since 0.8.7
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppUpdater {

}
