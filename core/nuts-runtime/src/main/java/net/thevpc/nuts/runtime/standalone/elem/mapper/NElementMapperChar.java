package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;

public class NElementMapperChar implements NElementMapper<Character> {

    @Override
    public Object toSimple(NElementSerializerContext<Character> context) {
        return context.instance();
    }

    @Override
    public NElement toElement(NElementSerializerContext<Character> context) {
        return NElement.ofString(String.valueOf(context.instance()));
    }

    @Override
    public Character toObject(NElementDeserializerContext context) {
        NElement element = context.element();
        Type to = context.instanceType();
        final String s = element.asStringValue().get();
        if(to==null){
            to=Character.class;
        }

        return (s == null || s.isEmpty())
                ? (((to instanceof Class) && ((Class) to).isPrimitive()) ? '\0' : null)
                : s.charAt(0);
    }
}
