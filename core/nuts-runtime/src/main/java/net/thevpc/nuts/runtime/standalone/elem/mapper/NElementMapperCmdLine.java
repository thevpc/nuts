package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;

import java.lang.reflect.Type;

public class NElementMapperCmdLine implements NElementMapper<NCmdLine> {

    @Override
    public Object toSimple(NCmdLine src, Type typeOfSrc, NElementFactoryContext context) {
        return src.toStringArray();
    }

    @Override
    public NElement createElement(NCmdLine o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultCreateElement(this.toSimple(o, null, context), null);
    }

    @Override
    public NCmdLine createObject(NElementDeserializerContext context) {
        String[] i = context.defaultToObject(context.element(), String[].class);
        return NCmdLine.of(i);
    }
}
