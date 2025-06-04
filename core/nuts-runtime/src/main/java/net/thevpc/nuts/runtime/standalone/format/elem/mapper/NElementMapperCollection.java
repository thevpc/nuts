package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementAnnotation;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.format.elem.DefaultNArrayElement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

public class NElementMapperCollection implements NElementMapper {


    public NElementMapperCollection() {
    }

    @Override
    public Object destruct(Object src, Type typeOfSrc, NElementFactoryContext context) {
        Collection<Object> coll = (Collection) src;
        return coll.stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
    }

    @Override
    public NElement createElement(Object o, Type typeOfSrc, NElementFactoryContext context) {
        Collection<Object> coll = (Collection) o;
        List<NElement> collect = coll.stream().map(x -> context.createElement(x)).collect(Collectors.toList());
        return new DefaultNArrayElement(null,null,collect, new NElementAnnotation[0],null);
    }

    public Collection fillObject(NElement o, Collection coll, Type elemType, Type to, NElementFactoryContext context) {
        for (NElement nutsElement : o.asArray().get().children()) {
            coll.add(context.createObject(nutsElement, elemType));
        }
        return coll;
    }

    @Override
    public Collection createObject(NElement o, Type to, NElementFactoryContext context) {
        Class cls = ReflectUtils.getRawClass(to);
        Type elemType = Object.class;
        if (to instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) to;
            elemType = pt.getActualTypeArguments()[0];
        }
//            if (cls == null) {
//                throw new IllegalArgumentException("invalid");
//            }
        switch (cls.getName()) {
            case "java.util.Collection":
            case "java.util.List":
            case "java.util.ArrayList": {
                return fillObject(o, new ArrayList(o.asArray().get().size()), elemType, to, context);
            }
            case "java.util.Set":
            case "java.util.LinkedHashset": {
                return fillObject(o, new LinkedHashSet(), elemType, to, context);
            }
            case "java.util.Hashset": {
                return fillObject(o, new HashSet(), elemType, to, context);
            }
            case "java.util.SortedSet":
            case "java.util.NavigableSet":
            case "java.util.TreeSet": {
                return fillObject(o, new TreeSet(), elemType, to, context);
            }
            case "java.util.Queue": {
                return fillObject(o, new LinkedList(), elemType, to, context);
            }
            case "java.util.BlockingQueue": {
                return fillObject(o, new LinkedBlockingQueue(), elemType, to, context);
            }
            case "java.util.TransferQueue": {
                return fillObject(o, new LinkedTransferQueue(), elemType, to, context);
            }
            case "java.util.Deque": {
                return fillObject(o, new ArrayList(), elemType, to, context);
            }
            default: {
                NReflectType m = context.getTypesRepository().getType(to);
                return fillObject(o, (Collection) m.newInstance(), elemType, to, context);
            }
        }
    }

}
