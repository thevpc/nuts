package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperNVersion implements NElementMapper<NVersion> {

    @Override
    public Object destruct(NVersion src, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            NSession ws = context.getSession();
            return src.formatter(context.getSession()).setSession(ws).setNtf(true).format();
        } else {
            return src.toString();
        }
    }

    @Override
    public NElement createElement(NVersion o, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            return context.elem().ofString(o.formatter(context.getSession()).setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NVersion createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        return NVersion.of(o.asString().get(session)).get(session);
    }

}
