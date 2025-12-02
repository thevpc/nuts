package net.thevpc.nuts.app;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as the main application entry point to be executed.
 * <p>
 * Methods annotated with {@code @NAppRunner} serve as the primary runtime
 * entry for the application logic. This is the method that will be invoked
 * when the application is started through the Nuts framework.
 * </p>
 * <p>
 * Typically, the annotated method should be public and static, and can
 * accept arguments as needed for execution. The framework detects this
 * annotation to automatically invoke the method as part of the application
 * lifecycle.
 * </p>
 *
 * <p>
 * Common usage:
 * <pre>{@code
 * public class MyApp {
 *     @NAppRunner
 *     public static void main(String[] args) {
 *         System.out.println("Application started!");
 *     }
 * }
 * }</pre>
 * </p>
 *
 * @author thevpc
 * @app.category Annotation
 * @since 0.8.7
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface NAppRunner {

}
