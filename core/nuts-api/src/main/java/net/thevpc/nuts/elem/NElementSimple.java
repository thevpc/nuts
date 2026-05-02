package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

/**
 * classes that implement this interface are (by default) atomic using
 *  {@link NElementFactoryContext#toSimple(Object, Type)}
 *  and
 *  {@link NElementFactoryContext#defaultToSimple(Object, Type)}
 */
public interface NElementSimple {

}
