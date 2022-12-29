package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNArrayElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class NElementMapperIterator implements NElementMapper<Iterator> {

    @Override
    public Object destruct(Iterator o, Type typeOfSrc, NElementFactoryContext context) {
        Iterator nl = (Iterator) o;
        List<Object> values = new ArrayList<>();
        while (nl.hasNext()) {
            values.add(context.destruct(nl.next(), null));
        }
        return values;
    }

    @Override
    public NElement createElement(Iterator o, Type typeOfSrc, NElementFactoryContext context) {
        Iterator nl = (Iterator) o;
        List<NElement> values = new ArrayList<>();
        while (nl.hasNext()) {
            values.add(context.objectToElement(nl.next(), null));
        }
        return new DefaultNArrayElement(values, context.getSession());
    }

    @Override
    public Iterator createObject(NElement o, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
        return o.asArray().get(session).items().stream().map(x -> context.elementToObject(x, Object.class)).collect(
                Collectors.toList()).iterator();
    }

}
