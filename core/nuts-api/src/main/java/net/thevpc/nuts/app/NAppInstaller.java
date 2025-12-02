package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as the installer for an application.
 * <p>
 * Methods annotated with {@code @NAppInstaller} are responsible for
 * performing installation tasks, such as setting up required resources,
 * directories, or configuration needed before the application can run.
 * </p>
 * <p>
 * Typically, the annotated method should be public and static. It may
 * optionally take parameters that provide context about the installation
 * environment or workspace.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * public class MyApp {
 *     @NAppInstaller
 *     public static void install() {
 *         System.out.println("Installing application resources...");
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author thevpc
 * @app.category Annotation
 * @since 0.5.4
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppInstaller {

}
