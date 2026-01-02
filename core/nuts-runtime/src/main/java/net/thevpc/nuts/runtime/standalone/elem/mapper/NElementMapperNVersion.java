package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.text.NFormats;

import java.lang.reflect.Type;

public class NElementMapperNVersion implements NElementMapper<NVersion> {

    @Override
    public Object destruct(NVersion src, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            return NFormats.of(src).get().setNtf(true).format(src);
        } else {
            return src.toString();
        }
    }

    @Override
    public NElement createElement(NVersion o, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            return NElement.ofString(NFormats.of(o).get().setNtf(true).format(o).toString());
        } else {
            return context.defaultCreateElement(o.toString(), null);
        }
    }

    @Override
    public NVersion createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        return NVersion.get(o.asStringValue().get()).get();
    }

}
