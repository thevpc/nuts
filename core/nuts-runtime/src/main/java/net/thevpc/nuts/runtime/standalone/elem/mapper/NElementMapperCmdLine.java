package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;

import java.lang.reflect.Type;

public class NElementMapperCmdLine implements NElementMapper<NCmdLine> {

    @Override
    public Object destruct(NCmdLine src, Type typeOfSrc, NElementFactoryContext context) {
        return src.toStringArray();
    }

    @Override
    public NElement createElement(NCmdLine o, Type typeOfSrc, NElementFactoryContext context) {
        return context.defaultObjectToElement(destruct(o, null, context), null);
    }

    @Override
    public NCmdLine createObject(NElement o, Type to, NElementFactoryContext context) {
        String[] i = context.defaultElementToObject(o, String[].class);
        return NCmdLine.of(i);
    }
}
