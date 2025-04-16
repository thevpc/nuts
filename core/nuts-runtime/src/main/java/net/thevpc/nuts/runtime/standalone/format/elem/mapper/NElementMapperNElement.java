package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class NElementMapperNElement implements NElementMapper<NElement> {

    public NElementMapperNElement() {
    }

    @Override
    public Object destruct(NElement src, Type typeOfSrc, NElementFactoryContext context) {
        switch (src.type()) {
            case PAIR:{
                NPairElement p = src.asPair().get();
                return new AbstractMap.SimpleEntry<Object, Object>(
                        context.defaultDestruct(p.key(), NElement.class),
                        context.defaultDestruct(p.value(), NElement.class)
                );
            }
            case ARRAY: {
                return src.asArray().get().children().stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
            }
            case OBJECT: {
                Set<Object> visited = new HashSet<>();
                boolean map = true;
                List<Object> all = new ArrayList<>();
                for (NElement nElement : src.asObject().get().children()) {
                    if (map && nElement instanceof NPairElement) {
                        NPairElement nPairElement = (NPairElement) nElement;
                        Object k = context.destruct(nPairElement.key(), null);
                        Object v = context.destruct(nPairElement.value(), null);
                        if (visited.contains(k)) {
                            map = false;
                        } else {
                            visited.add(k);
                        }
                        all.add(new AbstractMap.SimpleEntry<>(k, v));
                    } else {
                        Object k = context.destruct(nElement, null);
                        all.add(k);
                    }
                }
                if (map) {
                    LinkedHashMap<Object, Object> m = new LinkedHashMap<>();
                    for (Object item : all) {
                        Map.Entry<Object, Object> entry = (Map.Entry<Object, Object>) item;
                        m.put(entry.getKey(), entry.getValue());
                    }
                    return m;
                }
                return all;
            }
            case CUSTOM: {
                return src.asCustom().get();
            }
            default: {
                return context.objectToElement(src, NPrimitiveElement.class);
            }
        }
    }

    @Override
    public NElement createElement(NElement src, Type typeOfSrc, NElementFactoryContext context) {
        if (src.type().isPrimitive()) {
            return src;
        }
        switch (src.type()) {
            case PAIR: {
                NPairElement p = src.asPair().get();
                NElement k = p.key();
                NElement v = p.value();
                boolean someChange0;
                NElement k2 = context.objectToElement(k, null);
                NElement v2 = context.objectToElement(v, null);
                if (k2 == k || v2 == v) {
                    return p.builder().key(k2).value(v2).build();
                }
                return p;
            }
            case ARRAY: {
                NArrayElement arr = src.asArray().get();
                List<NElement> children = new ArrayList<>(arr.size());
                boolean someChange = false;
                for (NElement c : arr) {
                    NElement v2 = context.objectToElement(c, null);
                    if (!someChange) {
                        someChange = v2 != c;
                    }
                    children.add(v2);
                }
                if (someChange) {
                    return context.elem().ofArrayBuilder().addAll(children.toArray(new NElement[0])).build();
                }
                return src;
            }
            case OBJECT: {
                NObjectElement obj = src.asObject().get();
                List<NElement> children = new ArrayList<>(obj.size());
                boolean someChange = false;
                for (NElement e : obj) {
                    boolean someChange0;
                    NElement k2 = context.objectToElement(e, null);
                    someChange0 = k2 != e;
                    if (someChange0) {
                        if (!someChange) {
                            someChange = true;
                        }
                        children.add(k2);
                    } else {
                        children.add(e);
                    }
                }
                if (someChange) {
                    NObjectElementBuilder obj2 = context.elem().ofObjectBuilder();
                    obj2.addAll(children.toArray(new NElement[0]));
                    return obj2.build();
                }
                return src;
            }
            case CUSTOM: {
                Object v1 = src.asCustom().get().value();
                if (context.getIndestructibleObjects() != null && context.getIndestructibleObjects().test(v1.getClass())) {
                    return src;
                }
                return context.objectToElement(v1, null);
            }
        }
        return src;
    }

    @Override
    public NElement createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        return createElement(o, typeOfResult, context);
    }
}
