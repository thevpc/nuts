package net.thevpc.nuts.elem;


import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target({ElementType.PARAMETER, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.METHOD, ElementType.TYPE})
public @interface NutsMapBy {
    Class mapClass() default void.class;

    String name() default "";
}
