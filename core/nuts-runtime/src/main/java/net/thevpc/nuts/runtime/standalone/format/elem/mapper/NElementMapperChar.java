package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperChar implements NElementMapper<Character> {

    @Override
    public Object destruct(Character src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Character o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(String.valueOf(o));
    }

    @Override
    public Character createObject(NElement o, Type to, NElementFactoryContext context) {
        final String s = o.asStringValue().get();
        if(to==null){
            to=Character.class;
        }

        return (s == null || s.isEmpty())
                ? (((to instanceof Class) && ((Class) to).isPrimitive()) ? '\0' : null)
                : s.charAt(0);
    }
}
