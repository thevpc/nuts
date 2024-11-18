package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.NDefinition;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.definition.DefaultNDefinition;

import java.lang.reflect.Type;

public class NElementMapperNDefinition implements NElementMapper<NDefinition> {

    @Override
    public Object destruct(NDefinition src, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNDefinition dd = (src instanceof DefaultNDefinition) ? (DefaultNDefinition) src : new DefaultNDefinition(src, context.getWorkspace());
        return context.defaultDestruct(dd, null);
    }

    @Override
    public NElement createElement(NDefinition o, Type typeOfSrc, NElementFactoryContext context) {
        DefaultNDefinition dd = (o instanceof DefaultNDefinition) ? (DefaultNDefinition) o : new DefaultNDefinition(o, context.getWorkspace());
        return context.defaultObjectToElement(dd, null);
    }

    @Override
    public NDefinition createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        NDefinition d = context.defaultElementToObject(o, DefaultNDefinition.class);
        //pass the session the instance
        return new DefaultNDefinition(d, context.getWorkspace());
    }
}
