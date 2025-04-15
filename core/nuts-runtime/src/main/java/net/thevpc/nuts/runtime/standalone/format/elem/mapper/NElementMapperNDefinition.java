package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinitionBuilder;

import java.lang.reflect.Type;

public class NElementMapperNDefinition implements NElementMapper<NDefinition> {

    @Override
    public Object destruct(NDefinition src, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNDefinition dd = (src instanceof DefaultNDefinition) ? (DefaultNDefinition) src : new DefaultNDefinition(src);
        return context.defaultDestruct(dd, null);
    }

    @Override
    public NElement createElement(NDefinition o, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNDefinition dd = (o instanceof DefaultNDefinition) ? (DefaultNDefinition) o : new DefaultNDefinition(o);
        return context.defaultObjectToElement(dd, null);
    }

    @Override
    public NDefinition createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        DefaultNDefinitionBuilder d = context.defaultElementToObject(o, DefaultNDefinitionBuilder.class);
        //pass the session the instance
        return d.build();
    }
}
