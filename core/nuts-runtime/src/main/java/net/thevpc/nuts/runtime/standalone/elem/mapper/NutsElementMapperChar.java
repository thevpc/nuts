package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.NutsSession;

import java.lang.reflect.Type;

public class NutsElementMapperChar implements NutsElementMapper<Character> {

    @Override
    public Object destruct(Character src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Character o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().ofString(String.valueOf(o));
    }

    @Override
    public Character createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        final String s = o.asString().get(session);
        return (s == null || s.isEmpty())
                ? (((to instanceof Class) && ((Class) to).isPrimitive()) ? '\0' : null)
                : s.charAt(0);
    }
}
