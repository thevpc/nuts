package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;

import java.lang.reflect.Type;

public class NutsElementMapperChar implements NutsElementMapper<Character> {

    @Override
    public Object destruct(Character src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Character o, Type typeOfSrc, NutsElementFactoryContext context) {
        return context.elem().forString(String.valueOf(o));
    }

    @Override
    public Character createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        final String s = o.asPrimitive().getString();
        return (s == null || s.isEmpty())
                ? (((to instanceof Class) && ((Class) to).isPrimitive()) ? '\0' : null)
                : s.charAt(0);
    }
}
