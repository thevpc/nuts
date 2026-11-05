package net.thevpc.nuts.elem;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the custom mapper class responsible for converting a type to and from an
 * {@link NElement} (JSON, TSON, YAML).
 * <p>
 * This annotation only carries the mapper class. Its two other roles are now declared elsewhere :
 * property renaming is {@link NProperty} and creator selection is {@link NCreator}.
 * <p>
 * Note that {@link #mapClass()} is not read by the element mapper yet : declaring it records the intent
 * for a custom mapping without changing how the type is currently converted.
 *
 * @author thevpc
 * @since 0.8.0
 * @see NCreator
 * @see NProperty
 */
@Target({ElementType.TYPE, ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface NMapBy {
    /**
     * Custom mapping class responsible for converting the element.
     *
     * @return custom mapper class
     */
    Class<?> mapClass() default void.class;
}
