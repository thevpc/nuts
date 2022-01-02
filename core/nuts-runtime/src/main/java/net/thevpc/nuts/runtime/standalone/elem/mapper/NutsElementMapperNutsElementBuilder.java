package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementEntry;

import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

public class NutsElementMapperNutsElementBuilder implements NutsElementMapper<NutsElementBuilder> {

    public NutsElementMapperNutsElementBuilder() {
    }

    @Override
    public Object destruct(NutsElementBuilder src, Type typeOfSrc, NutsElementFactoryContext context) {
        switch (src.type()) {
            case ARRAY: {
                return src.build().asArray().children().stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
            }
            case OBJECT: {
                Set<Object> visited = new HashSet<>();
                boolean map = true;
                List<Map.Entry<Object, Object>> all = new ArrayList<>();
                for (NutsElementEntry nutsElementEntry : src.build().asObject().children()) {
                    Object k = context.destruct(nutsElementEntry.getKey(), null);
                    Object v = context.destruct(nutsElementEntry.getValue(), null);
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
                return context.objectToElement(src, NutsPrimitiveElement.class);
            }
        }
    }

    @Override
    public NutsElement createElement(NutsElementBuilder src, Type typeOfSrc, NutsElementFactoryContext context) {
        if(src.type().isPrimitive()){
            return src.build();
        }
        switch (src.type()){
            case ARRAY:{
                NutsArrayElement arr = src.build().asArray();
                List<NutsElement> children=new ArrayList<>(arr.size());
                boolean someChange=false;
                for (NutsElement c : arr) {
                    NutsElement v2 = context.objectToElement(c, null);
                    if(!someChange){
                        someChange=v2!=c;
                    }
                    children.add(v2);
                }
                if(someChange){
                    return context.elem().ofArray().addAll(children.toArray(new NutsElement[0])).build();
                }
                return src.build();
            }
            case OBJECT:{
                NutsObjectElement obj = src.build().asObject();
                List<NutsElementEntry> children=new ArrayList<>(obj.size());
                boolean someChange=false;
                for (NutsElementEntry e : obj) {
                    boolean someChange0;
                    NutsElement k2 = context.objectToElement(e.getKey(), null);
                    someChange0=k2!=e.getKey();
                    NutsElement v2 = context.objectToElement(e.getValue(), null);
                    if(!someChange0){
                        someChange0=v2!=e.getValue();
                    }
                    if(someChange0) {
                        if (!someChange) {
                            someChange = true;
                        }
                        children.add(new DefaultNutsElementEntry(k2, v2));
                    }else{
                        children.add(e);
                    }
                }
                if(someChange){
                    NutsObjectElementBuilder obj2 = context.elem().ofObject();
                    obj2.addAll(children.toArray(new NutsElementEntry[0]));
                    return obj2.build();
                }
                return src.build();
            }
            case CUSTOM:{
                Object v1 = src.build().asCustom().getValue();
                if(context.getIndestructibleObjects()!=null && context.getIndestructibleObjects().test(v1.getClass())){
                    return src.build();
                }
                return context.objectToElement(v1, null);
            }
        }
        return src.build();
    }

    @Override
    public NutsElementBuilder createObject(NutsElement src, Type typeOfResult, NutsElementFactoryContext context) {
        if(src.type().isPrimitive()){
            return src.asSafeObject().builder();
        }
        switch (src.type()){
            case ARRAY:{
                NutsArrayElement arr = src.asArray();
                List<NutsElement> children=new ArrayList<>(arr.size());
                boolean someChange=false;
                for (NutsElement c : arr) {
                    NutsElement v2 = context.objectToElement(c, null);
                    if(!someChange){
                        someChange=v2!=c;
                    }
                    children.add(v2);
                }
                if(someChange){
                    return context.elem().ofArray().addAll(children.toArray(new NutsElement[0]));
                }
                return src.asArray().builder();
            }
            case OBJECT:{
                NutsObjectElement obj = src.asObject();
                List<NutsElementEntry> children=new ArrayList<>(obj.size());
                boolean someChange=false;
                for (NutsElementEntry e : obj) {
                    boolean someChange0;
                    NutsElement k2 = context.objectToElement(e.getKey(), null);
                    someChange0=k2!=e.getKey();
                    NutsElement v2 = context.objectToElement(e.getValue(), null);
                    if(!someChange0){
                        someChange0=v2!=e.getValue();
                    }
                    if(someChange0) {
                        if (!someChange) {
                            someChange = true;
                        }
                        children.add(new DefaultNutsElementEntry(k2, v2));
                    }else{
                        children.add(e);
                    }
                }
                if(someChange){
                    NutsObjectElementBuilder obj2 = context.elem().ofObject();
                    obj2.addAll(children.toArray(new NutsElementEntry[0]));
                    return obj2;
                }
                return src.asSafeObject().builder();
            }
            case CUSTOM:{
                throw new NutsIllegalArgumentException(context.getSession(),NutsMessage.plain("unsupported"));
            }
        }
        throw new NutsIllegalArgumentException(context.getSession(),NutsMessage.plain("unsupported"));
    }
}
