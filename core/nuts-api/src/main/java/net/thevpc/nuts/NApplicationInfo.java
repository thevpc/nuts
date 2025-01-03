package net.thevpc.nuts;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to provide metadata information for an application.
 *
 * This annotation can be applied at the class level to specify
 * attributes related to the application, such as its unique identifier.
 *
 * Attributes:
 * - id: The unique identifier for the annotated application. It defaults to an empty string if not provided.
 *
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface NApplicationInfo {
    String id() default "";
}
