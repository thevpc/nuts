package net.thevpc.nuts.elem;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a constructor or a static factory method as the way to build an instance
 * from an {@link NElement}.
 * <p>
 * This annotation only selects the member to use as a creator. The names of the
 * bound properties are declared with {@link NProperty}. A creator that takes the
 * element itself does not need any parameter annotation.
 *
 * <pre>
 * public class Point {
 *     &#64;NCreator
 *     public Point(&#64;NProperty("x") int x, &#64;NProperty("y") int y) { ... }
 * }
 * </pre>
 *
 * <h2>Supported shapes</h2>
 * For a target type {@code T}, the following shapes are supported, each one either
 * explicitly annotated with {@code @NCreator} or discovered automatically as described
 * under <b>Resolution</b>:
 * <ol>
 * <li>{@code T(NElement e)} constructor : the developer reads the element and its fields manually.</li>
 * <li>{@code static T of(NElement e)} : the same, as a factory.</li>
 * <li>{@code static NOptional&lt;T&gt; of(NElement e)} : the same, and an empty result means that the
 * creator declines the element.</li>
 * <li>{@code T(@NProperty("a") A a, @NProperty("b") B b, ...)} constructor : property bound parameters.</li>
 * <li>{@code static T of(@NProperty("a") A a, ...)} : property bound factory.</li>
 * <li>{@code static NOptional&lt;T&gt; of(@NProperty("a") A a, ...)} : property bound, with optional result.</li>
 * </ol>
 * The element parameter of the shapes 1 to 3 may be {@link NElement} or any of its subtypes
 * ({@link NObjectElement}, {@link NArrayElement}, {@link NStringElement}, {@link NPrimitiveElement} ...).
 * Static factories are only recognized when their name is {@code of} : arbitrary static methods
 * that merely return {@code T} are never treated as creators.
 *
 * <h2>Resolution</h2>
 * Resolution is driven by the parameter shape of the candidate creators :
 * <ol>
 * <li>Candidates are all constructors and all static {@code of} methods annotated with {@code @NCreator},
 * plus all unannotated constructors and unannotated static {@code of} methods that take exactly one
 * parameter assignable to {@link NElement}. An unannotated property bound creator is never considered,
 * except for the canonical constructor of a record.</li>
 * <li>When an element-parameter candidate exists for the type, it wins over the property bound ones.</li>
 * <li>Property bound candidates are used only when no element-parameter candidate applies.</li>
 * <li>Among element-parameter candidates declared with different parameter types (for example
 * {@code of(NObjectElement)} and {@code of(NArrayElement)}), the most specific type that accepts the
 * runtime element is used. Such overloads are supported.</li>
 * <li>When more than one candidate shares the same parameter shape (two {@code @NCreator} constructors
 * bound to the same properties, or two {@code of(NObjectElement)} methods), a mapping exception is thrown
 * naming the type and every conflicting candidate. Nothing is ever picked silently.</li>
 * <li>When no candidate is found, the default behavior is used : instantiate with the no-arg constructor
 * and write every mapped property.</li>
 * </ol>
 *
 * <h2>Records</h2>
 * A record does not need this annotation : its canonical constructor is already the creator, and the
 * component names, possibly renamed with {@link NProperty}, are the bound properties. This annotation
 * targets constructors and methods, so it cannot be written on a record declaration ; writing it on an
 * explicitly declared canonical constructor is accepted and binds the same properties.
 *
 * <h2>Element parameter matching</h2>
 * A parameter declared as {@link NElement} always matches. A parameter declared as a subtype, such as
 * {@link NObjectElement}, matches only when the runtime element is of that kind. When no element-parameter
 * candidate accepts the runtime element, a mapping exception is thrown stating the expected and the actual
 * kinds and the mapping does not fall through to another strategy.
 *
 * <h2>NOptional semantics</h2>
 * When a creator returns {@link net.thevpc.nuts.util.NOptional}, a present value is used as is, and an empty
 * value means that the creator declines the element : the mapper then continues with the next applicable
 * candidate and finally with the default behavior. An exception thrown by a creator is a real failure and is
 * always propagated. The return type is checked against the target type at resolution time : a raw
 * {@code NOptional} or an {@code NOptional<Other>} is not a creator for {@code T}.
 *
 *
 * @author thevpc
 * @since 1.1.0
 * @see NProperty
 * @see NMapBy
 */
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NCreator {
}
