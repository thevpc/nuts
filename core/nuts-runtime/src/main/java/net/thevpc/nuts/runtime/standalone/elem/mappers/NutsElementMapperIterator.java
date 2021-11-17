package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsArrayElement;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class NutsElementMapperIterator implements NutsElementMapper<Iterator> {

    @Override
    public Object destruct(Iterator o, Type typeOfSrc, NutsElementFactoryContext context) {
        Iterator nl = (Iterator) o;
        List<Object> values = new ArrayList<>();
        while (nl.hasNext()) {
            values.add(context.destruct(nl.next(), null));
        }
        return values;
    }

    @Override
    public NutsElement createElement(Iterator o, Type typeOfSrc, NutsElementFactoryContext context) {
        Iterator nl = (Iterator) o;
        List<NutsElement> values = new ArrayList<>();
        while (nl.hasNext()) {
            values.add(context.objectToElement(nl.next(), null));
        }
        return new DefaultNutsArrayElement(values, context.getSession());
    }

    @Override
    public Iterator createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        return o.asArray().children().stream().map(x -> context.elementToObject(x, Object.class)).collect(
                Collectors.toList()).iterator();
    }

}
