package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.text.NObjectWriter;

public class NElementMapperNId implements NElementMapper<NId> {

    @Override
    public Object toSimple(NElementSerializerContext<NId> context) {
        NId o = context.instance();
        if (context.isNtf()) {
            return NObjectWriter.of(o).ntf(true).format(o);
        } else {
            return o.toString();
        }
    }

    @Override
    public NElement toElement(NElementSerializerContext<NId> context) {
        NId o = context.instance();
        if (context.isNtf()) {
//                NutsWorkspace ws = context.getSession().getWorkspace();
//                NutsText n = ws.text().toText(ws.id().formatter(o).setNtf(true).format());
//                return ws.elem().forPrimitive().buildNutsString(n);
            return NElement.ofString(NObjectWriter.of(o).ntf(true).format(o).toString());
        } else {
            return context.defaultCreateElement(o.toString(), null);
        }
    }

    @Override
    public NId toObject(NElementDeserializerContext context) {
        return NId.get(context.element().asPrimitive().flatMap(NElement::asStringValue).get()).get();
    }

}
