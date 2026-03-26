package net.thevpc.nuts.elem;

import java.lang.reflect.Type;

/**
 * classes that implement this interface are (byd default) non destructable using
 *  {@link NElementFactoryContext#destruct(Object, Type)}
 *  and
 *  {@link NElementFactoryContext#defaultDestruct(Object, Type)}
 */
public interface NElementAutoUndestructable {

}
