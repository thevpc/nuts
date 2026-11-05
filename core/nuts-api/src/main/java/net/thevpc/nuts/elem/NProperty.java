package net.thevpc.nuts.elem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the element property name bound to a creator parameter.
 * <p>
 * This annotation only carries a name. The member it applies to is selected by
 * {@link NCreator} : {@code @NProperty} tells which property of the mapped element feeds
 * which parameter of the creator.
 *
 * <pre>
 * public class Point {
 *     &#64;NCreator
 *     public Point(&#64;NProperty("x") int x, &#64;NProperty("y") int y) { ... }
 * }
 * </pre>
 *
 * On a record, this annotation renames the component and it may be declared either on the component
 * itself or on the matching parameter of the canonical constructor, whether that constructor is the
 * implicit one or written by hand. Records always keep their canonical constructor as creator, so
 * renaming a component does not require {@link NCreator}.
 * <p>
 * Fields and accessors may carry this annotation, but only the element to object path reads them :
 * writing an ordinary object to an element keeps using the java property names.
 *
 * @author thevpc
 * @since 1.1.0
 * @see NCreator
 */
@Target({ElementType.PARAMETER, ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NProperty {

    /**
     * Serialized property name.
     * Empty means : use the java name.
     *
     * @return serialized property name
     */
    String value() default "";
}
