package net.thevpc.nuts.runtime.standalone.elem.mappers;

import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectType;
import net.thevpc.nuts.runtime.bundles.reflect.ReflectUtils;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsArrayElement;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.LinkedTransferQueue;
import java.util.stream.Collectors;

public class NutsElementMapperCollection implements NutsElementMapper {

    private final DefaultNutsElementFactoryService defaultNutsElementFactoryService;

    public NutsElementMapperCollection(DefaultNutsElementFactoryService defaultNutsElementFactoryService) {
        this.defaultNutsElementFactoryService = defaultNutsElementFactoryService;
    }

    @Override
    public Object destruct(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
        Collection<Object> coll = (Collection) src;
        return coll.stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
    }

    @Override
    public NutsElement createElement(Object o, Type typeOfSrc, NutsElementFactoryContext context) {
        Collection<Object> coll = (Collection) o;
        List<NutsElement> collect = coll.stream().map(x -> context.objectToElement(x, null)).collect(Collectors.toList());
        return new DefaultNutsArrayElement(collect, context.getSession());
    }

    public Collection fillObject(NutsElement o, Collection coll, Type elemType, Type to, NutsElementFactoryContext context) {
        for (NutsElement nutsElement : o.asArray().children()) {
            coll.add(context.elementToObject(nutsElement, elemType));
        }
        return coll;
    }

    @Override
    public Collection createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
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
                return fillObject(o, new ArrayList(o.asArray().size()), elemType, to, context);
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
                ReflectType m = defaultNutsElementFactoryService.getTypesRepository().getType(to);
                return fillObject(o, (Collection) m.newInstance(), elemType, to, context);
            }
        }
    }

}
