package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class NElementMapperNElementBuilder implements NElementMapper<NElementBuilder> {

    public NElementMapperNElementBuilder() {
    }

    @Override
    public Object destruct(NElementBuilder src, Type typeOfSrc, NElementFactoryContext context) {
        switch (src.type()) {
            case ARRAY: {
                return src.build().asArray()
                        .get()
                        .children().stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
            }
            case OBJECT: {
                Set<Object> visited = new HashSet<>();
                boolean map = true;
                List<Object> all = new ArrayList<>();
                for (NElement item : src.build().asObject().get().children()) {
                    if(map && item instanceof NPairElement) {
                        NPairElement nPairElement = (NPairElement) item;
                        Object k = context.destruct(nPairElement.key(), null);
                        Object v = context.destruct(nPairElement.value(), null);
                        if (map && visited.contains(k)) {
                            map = false;
                        } else {
                            visited.add(k);
                        }
                        all.add(new AbstractMap.SimpleEntry<>(k, v));
                    }else {
                        all.add(context.destruct(item, null));
                    }
                }
                if (map) {
                    LinkedHashMap<Object, Object> m = new LinkedHashMap<>();
                    for (Object item : all) {
                        Map.Entry<Object, Object> entry=(Map.Entry<Object, Object>) item;
                        m.put(entry.getKey(), entry.getValue());
                    }
                    return m;
                }
                return all;
            }
            case CUSTOM: {
                return src.build().asCustom().get();
            }
            default: {
                return context.createElement(src, NPrimitiveElement.class);
            }
        }
    }

    @Override
    public NElement createElement(NElementBuilder src, Type typeOfSrc, NElementFactoryContext context) {
        if(src.type().isAnyPrimitive()){
            return src.build();
        }
        switch (src.type()){
            case ARRAY:{
                NArrayElement arr = src.build().asArray().get();
                List<NElement> children=new ArrayList<>(arr.size());
                boolean someChange=false;
                for (NElement c : arr) {
                    NElement v2 = context.createElement(c);
                    if(!someChange){
                        someChange=v2!=c;
                    }
                    children.add(v2);
                }
                if(someChange){
                    return NElement.ofArrayBuilder().addAll(children.toArray(new NElement[0])).build();
                }
                return src.build();
            }
            case OBJECT:{
                NObjectElement obj = src.build().asObject().get();
                List<NElement> children = new ArrayList<>(obj.size());
                boolean someChange = false;
                for (NElement e : obj) {
                    boolean someChange0;
                    NElement k2 = context.createElement(e);
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
                    NObjectElementBuilder obj2 = NElement.ofObjectBuilder();
                    obj2.addAll(children.toArray(new NElement[0]));
                    return obj2.build();
                }
                return src.build();
            }
            case CUSTOM:{
                Object v1 = src.build().asCustom().get().value();
                if(context.isIndestructibleObject(v1)){
                    return src.build();
                }
                return context.createElement(v1);
            }
        }
        return src.build();
    }

    @Override
    public NElementBuilder createObject(NElement src, Type typeOfResult, NElementFactoryContext context) {
        return createElement(src.builder(), typeOfResult, context).builder();
    }
}
