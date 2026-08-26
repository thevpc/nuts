package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.text.NObjectWriter;

public class NElementMapperNVersion implements NElementMapper<NVersion> {

    @Override
    public Object toSimple(NElementSerializerContext<NVersion> context) {
        NVersion src = context.instance();
        if (context.isNtf()) {
            return NObjectWriter.of(src).ntf(true).format(src);
        } else {
            return src.toString();
        }
    }

    @Override
    public NElement toElement(NElementSerializerContext<NVersion> context) {
        NVersion o = context.instance();
        if (context.isNtf()) {
            return NElement.ofString(NObjectWriter.of(o).ntf(true).format(o).toString());
        } else {
            return context.defaultCreateElement(o.toString(), null);
        }
    }

    @Override
    public NVersion toObject(NElementDeserializerContext context) {
        return NVersion.get(context.element().asStringValue().get()).get();
    }

}
