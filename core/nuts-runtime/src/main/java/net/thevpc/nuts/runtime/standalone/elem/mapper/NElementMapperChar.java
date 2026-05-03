package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperChar implements NElementMapper<Character> {

    @Override
    public Object toSimple(Character src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Character o, Type typeOfSrc, NElementFactoryContext context) {
        return NElement.ofString(String.valueOf(o));
    }

    @Override
    public Character createObject(NElementDeserializerContext context) {
        NElement element = context.element();
        Type to = context.to();
        final String s = element.asStringValue().get();
        if(to==null){
            to=Character.class;
        }

        return (s == null || s.isEmpty())
                ? (((to instanceof Class) && ((Class) to).isPrimitive()) ? '\0' : null)
                : s.charAt(0);
    }
}
