package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a class as the definition of a Nuts application.
 * <p>
 * The annotated class typically contains lifecycle methods annotated with
 * {@link NAppInstaller}, {@link NAppUninstaller}, {@link NAppUpdater},
 * and {@link NAppRunner}, which define the installation, uninstallation,
 * update, and execution behaviors of the application.
 * </p>
 * <p>
 * The optional {@code id} attribute allows specifying a unique identifier
 * for the application. If left empty, the framework may generate an
 * identifier based on the class name or other metadata.
 * </p>
 *
 * <p>
 * Example usage:
 * <pre>{@code
 * @NAppDefinition(id = "com.example.myapp")
 * public class MyApp {
 *     @NAppInstaller
 *     public static void install() { ... }
 *
 *     @NAppRunner
 *     public static void run() { ... }
 * }
 * }</pre>
 * </p>
 *
 * @author thevpc
 * @app.category Annotation
 * @since 0.8.7
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppDefinition {
    String id() default "";
}
