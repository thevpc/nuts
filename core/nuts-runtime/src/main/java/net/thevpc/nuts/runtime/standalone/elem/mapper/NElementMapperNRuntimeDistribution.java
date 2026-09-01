package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.util.jclass.NRuntimeDistributionImpl;
import net.thevpc.nuts.platform.NRuntimeDistribution;

public class NElementMapperNRuntimeDistribution implements NElementMapper<NRuntimeDistribution> {

    @Override
    public Object toSimple(NElementSerializerContext<NRuntimeDistribution> context) {
        return context.defaultToSimple(context.instance(), null);
    }

    @Override
    public NElement toElement(NElementSerializerContext<NRuntimeDistribution> context) {
        return context.defaultCreateElement(context.instance(), null);
    }

    @Override
    public NRuntimeDistribution toObject(NElementDeserializerContext context) {
        NObjectElement obj = context.element().asObject().get();
        return new NRuntimeDistributionImpl(obj);
    }

}
