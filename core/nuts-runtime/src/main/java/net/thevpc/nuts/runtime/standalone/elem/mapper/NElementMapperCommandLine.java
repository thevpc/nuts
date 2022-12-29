package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperCommandLine implements NElementMapper<NCommandLine> {

    @Override
    public Object destruct(NCommandLine src, Type typeOfSrc, NElementFactoryContext context) {
        return src.toStringArray();
    }

    @Override
    public NElement createElement(NCommandLine o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NCommandLine createObject(NElement o, Type to, NElementFactoryContext context) {
        String[] i = context.defaultElementToObject(o, String[].class);
        return NCommandLine.of(i);
    }
}
