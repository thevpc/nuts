package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.DefaultNElementEntry;
import net.thevpc.nuts.elem.*;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class NElementMapperNElement implements NElementMapper<NElement> {

    public NElementMapperNElement() {
    }

    @Override
    public Object destruct(NElement src, Type typeOfSrc, NElementFactoryContext context) {
        NSession session = context.getSession();
        switch (src.type()) {
            case ARRAY: {
                return src.asArray().get().items().stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
            }
            case OBJECT: {
                Set<Object> visited = new HashSet<>();
                boolean map = true;
                List<Map.Entry<Object, Object>> all = new ArrayList<>();
                for (NElementEntry nElementEntry : src.asObject().get().entries()) {
                    Object k = context.destruct(nElementEntry.getKey(), null);
                    Object v = context.destruct(nElementEntry.getValue(), null);
                    if (map && visited.contains(k)) {
                        map = false;
                    } else {
                        visited.add(k);
                    }
                    all.add(new AbstractMap.SimpleEntry<>(k, v));
                }
                if (map) {
                    LinkedHashMap<Object, Object> m = new LinkedHashMap<>();
                    for (Map.Entry<Object, Object> entry : all) {
                        m.put(entry.getKey(), entry.getValue());
                    }
                    return m;
                }
                return all;
            }
            case CUSTOM: {

            }
            default: {
                return context.objectToElement(src, NPrimitiveElement.class);
            }
        }
    }

    @Override
    public NElement createElement(NElement src, Type typeOfSrc, NElementFactoryContext context) {
        NSession session = context.getSession();
        if(src.type().isPrimitive()){
            return src;
        }
        switch (src.type()){
            case ARRAY:{
                NArrayElement arr = src.asArray().get();
                List<NElement> children=new ArrayList<>(arr.size());
                boolean someChange=false;
                for (NElement c : arr) {
                    NElement v2 = context.objectToElement(c, null);
                    if(!someChange){
                        someChange=v2!=c;
                    }
                    children.add(v2);
                }
                if(someChange){
                    return context.elem().ofArray().addAll(children.toArray(new NElement[0])).build();
                }
                return src;
            }
            case OBJECT:{
                NObjectElement obj = src.asObject().get();
                List<NElementEntry> children=new ArrayList<>(obj.size());
                boolean someChange=false;
                for (NElementEntry e : obj) {
                    boolean someChange0;
                    NElement k2 = context.objectToElement(e.getKey(), null);
                    someChange0=k2!=e.getKey();
                    NElement v2 = context.objectToElement(e.getValue(), null);
                    if(!someChange0){
                        someChange0=v2!=e.getValue();
                    }
                    if(someChange0) {
                        if (!someChange) {
                            someChange = true;
                        }
                        children.add(new DefaultNElementEntry(k2, v2));
                    }else{
                        children.add(e);
                    }
                }
                if(someChange){
                    NObjectElementBuilder obj2 = context.elem().ofObject();
                    obj2.addAll(children.toArray(new NElementEntry[0]));
                    return obj2.build();
                }
                return src;
            }
            case CUSTOM:{
                Object v1 = src.asCustom().get().getValue();
                if(context.getIndestructibleObjects()!=null && context.getIndestructibleObjects().test(v1.getClass())){
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
