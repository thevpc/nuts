package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectUtils;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNArrayElement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

public class NElementMapperCollection implements NElementMapper<Collection> {


    public NElementMapperCollection() {
    }

    @Override
    public Collection toSimple(NElementSerializerContext<Collection> context) {
        Collection<Object> coll = (Collection) context.instance();
        return coll.stream().map(x -> context.toSimple(x, null)).collect(Collectors.toList());
    }

    @Override
    public NElement toElement(NElementSerializerContext<Collection> context) {
        Collection<Object> coll = (Collection) context.instance();
        List<NElement> collect = coll.stream().map(x -> context.toElement(x)).collect(Collectors.toList());
        return new DefaultNArrayElement(null, null, collect);
    }

    public Collection fillObject(NElement o, Collection coll, Type elemType, Type to, NElementFactoryContext context) {
        for (NElement nutsElement : o.asArray().get().children()) {
            coll.add(context.toObject(nutsElement, elemType));
        }
        return coll;
    }

    @Override
    public Collection toObject(NElementDeserializerContext context) {
        Type to = context.instanceType();
        NElement element = context.element();
        if(to==null){
            to=ArrayList.class;
        }
        Class cls = NReflectUtils.getRawClass(to).get();
        Type elemType = Object.class;
        if (to instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) to;
            elemType = pt.getActualTypeArguments()[0];
        }
        switch (cls.getName()) {
            case "java.util.Collection":
            case "java.util.List":
            case "java.util.ArrayList": {
                return fillObject(element, new ArrayList(element.asArray().get().size()), elemType, to, context);
            }
            case "java.util.Set":
            case "java.util.LinkedHashset": {
                return fillObject(element, new LinkedHashSet(), elemType, to, context);
            }
            case "java.util.Hashset": {
                return fillObject(element, new HashSet(), elemType, to, context);
            }
            case "java.util.SortedSet":
            case "java.util.NavigableSet":
            case "java.util.TreeSet": {
                return fillObject(element, new TreeSet(), elemType, to, context);
            }
            case "java.util.Queue": {
                return fillObject(element, new LinkedList(), elemType, to, context);
            }
            case "java.util.BlockingQueue": {
                return fillObject(element, new LinkedBlockingQueue(), elemType, to, context);
            }
            case "java.util.TransferQueue": {
                return fillObject(element, new LinkedTransferQueue(), elemType, to, context);
            }
            case "java.util.Deque": {
                return fillObject(element, new ArrayList(), elemType, to, context);
            }
            default: {
                NReflectType m = context.getTypesRepository().getType(to);
                return fillObject(element, (Collection) m.newInstance(), elemType, to, context);
            }
        }
    }

}
