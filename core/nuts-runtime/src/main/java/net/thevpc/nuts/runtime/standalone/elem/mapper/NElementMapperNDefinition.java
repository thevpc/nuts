package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder;

import java.lang.reflect.Type;

public class NElementMapperNDefinition implements NElementMapper<NDefinition> {

    @Override
    public Object toSimple(NDefinition src, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNDefinition dd = (src instanceof DefaultNDefinition) ? (DefaultNDefinition) src : new DefaultNDefinition(src);
        return context.defaultToSimple(dd, null);
    }

    @Override
    public NElement createElement(NDefinition o, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNDefinition dd = (o instanceof DefaultNDefinition) ? (DefaultNDefinition) o : new DefaultNDefinition(o);
        return context.defaultCreateElement(dd, null);
    }

    @Override
    public NDefinition createObject(NElementDeserializerContext context) {
        DefaultNDefinitionBuilder d = context.defaultToObject(context.element(), DefaultNDefinitionBuilder.class);
        //pass the session the instance
        return d.build();
    }
}
