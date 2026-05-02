package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNArrayElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class NElementMapperIterator implements NElementMapper<Iterator> {

    @Override
    public Object toSimple(Iterator o, Type typeOfSrc, NElementFactoryContext context) {
        Iterator nl = (Iterator) o;
        List<Object> values = new ArrayList<>();
        while (nl.hasNext()) {
            values.add(context.toSimple(nl.next(), null));
        }
        return values;
    }

    @Override
    public NElement createElement(Iterator o, Type typeOfSrc, NElementFactoryContext context) {
        Iterator nl = (Iterator) o;
        List<NElement> values = new ArrayList<>();
        while (nl.hasNext()) {
            values.add(context.toElement(nl.next()));
        }
        return new DefaultNArrayElement(null,null,values);
    }

    @Override
    public Iterator createObject(NElement o, Type to, NElementFactoryContext context) {
        return o.asArray().get().children().stream().map(x -> context.toObject(x, Object.class)).collect(
                Collectors.toList()).iterator();
    }

}
