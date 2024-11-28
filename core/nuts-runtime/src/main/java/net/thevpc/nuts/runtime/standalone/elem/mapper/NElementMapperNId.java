package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.format.NFormats;
import net.thevpc.nuts.util.NLiteral;

import java.lang.reflect.Type;

public class NElementMapperNId implements NElementMapper<NId> {

    @Override
    public Object destruct(NId o, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
            return NFormats.of().ofFormat(o).get().setNtf(true).format();
        } else {
            return o.toString();
        }
    }

    @Override
    public NElement createElement(NId o, Type typeOfSrc, NElementFactoryContext context) {
        if (context.isNtf()) {
//                NutsWorkspace ws = context.getSession().getWorkspace();
//                NutsText n = ws.text().toText(ws.id().formatter(o).setNtf(true).format());
//                return ws.elem().forPrimitive().buildNutsString(n);
            NSession session = context.getSession();
            return context.elem().ofString(NFormats.of().ofFormat(o).get().setNtf(true).format().toString());
        } else {
            return context.defaultObjectToElement(o.toString(), null);
        }
    }

    @Override
    public NId createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        return NId.of(o.asPrimitive().flatMap(NLiteral::asString).get()).get();
    }

}
