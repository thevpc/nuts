package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NSession;

import java.lang.reflect.Type;

public class NElementMapperChar implements NElementMapper<Character> {

    @Override
    public Object destruct(Character src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Character o, Type typeOfSrc, NElementFactoryContext context) {
        return context.elem().ofString(String.valueOf(o));
    }

    @Override
    public Character createObject(NElement o, Type to, NElementFactoryContext context) {
        final String s = o.asString().get();
        return (s == null || s.isEmpty())
                ? (((to instanceof Class) && ((Class) to).isPrimitive()) ? '\0' : null)
                : s.charAt(0);
    }
}
