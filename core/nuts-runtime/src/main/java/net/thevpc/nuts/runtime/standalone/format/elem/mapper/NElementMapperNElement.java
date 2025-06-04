package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.util.NBooleanRef;
import net.thevpc.nuts.util.NRef;

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
            case UPLET:
            case NAMED_UPLET:
            {
                return src.asUplet().get().params().stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
            }
            case ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case NAMED_ARRAY:
            case PARAMETRIZED_ARRAY:
            {
                return src.asArray().get().children().stream().map(x -> context.destruct(x, null)).collect(Collectors.toList());
            }
            case OBJECT:
            case PARAMETRIZED_OBJECT:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            {
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
                return context.createElement(src, NPrimitiveElement.class);
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
                NBooleanRef someChange = NRef.ofBoolean(false);
                NElement k = convertOne_objectToElement(p.key(), src, typeOfSrc, context, someChange);
                NElement v = convertOne_objectToElement(p.value(), src, typeOfSrc, context, someChange);
                List<NElementAnnotation> anns = convertAnn_objectToElement(p.annotations(), src, typeOfSrc, context, someChange);
                if (someChange.get()) {
                    NPairElementBuilder obj2 = NElement.ofPairBuilder();
                    obj2.key(k);
                    obj2.value(v);
                    obj2.addAnnotations(anns);
                    obj2.addComments(p.comments());
                    return obj2.build();
                }
                return p;
            }
            case UPLET:
            case NAMED_UPLET:
            {
                NUpletElement arr = src.asUplet().get();
                NBooleanRef someChange = NRef.ofBoolean(false);
                List<NElement> params = convertList_objectToElement(arr.params(), src, typeOfSrc, context, someChange);
                List<NElementAnnotation> anns = convertAnn_objectToElement(arr.annotations(), src, typeOfSrc, context, someChange);
                if (someChange.get()) {
                    NUpletElementBuilder obj2 = NElement.ofUpletBuilder();
                    obj2.addAll(params.toArray(new NElement[0]));
                    obj2.name(arr.name().orNull());
                    obj2.addAnnotations(anns);
                    obj2.addComments(arr.comments());
                    return obj2.build();
                }
                return src;
            }
            case ARRAY:
            case NAMED_ARRAY:
            case NAMED_PARAMETRIZED_ARRAY:
            case PARAMETRIZED_ARRAY:
            {
                NArrayElement arr = src.asArray().get();
                NBooleanRef someChange = NRef.ofBoolean(false);
                List<NElement> children = convertList_objectToElement(arr.children(), src, typeOfSrc, context, someChange);
                List<NElement> params = convertList_objectToElement(arr.params().orNull(), src, typeOfSrc, context, someChange);
                List<NElementAnnotation> anns = convertAnn_objectToElement(arr.annotations(), src, typeOfSrc, context, someChange);
                if (someChange.get()) {
                    NArrayElementBuilder obj2 = NElement.ofArrayBuilder();
                    obj2.addAll(children.toArray(new NElement[0]));
                    if(params!=null){
                        obj2.addParams(params);
                    }
                    obj2.name(arr.name().orNull());
                    obj2.addAnnotations(anns);
                    obj2.addComments(arr.comments());
                    return obj2.build();
                }
                return src;
            }
            case OBJECT:
            case NAMED_OBJECT:
            case NAMED_PARAMETRIZED_OBJECT:
            case PARAMETRIZED_OBJECT:
            {
                NObjectElement obj = src.asObject().get();
                NBooleanRef someChange = NRef.ofBoolean(false);
                List<NElement> children = convertList_objectToElement(obj.children(), src, typeOfSrc, context, someChange);
                List<NElement> params = convertList_objectToElement(obj.params().orNull(), src, typeOfSrc, context, someChange);
                List<NElementAnnotation> anns = convertAnn_objectToElement(obj.annotations(), src, typeOfSrc, context, someChange);
                if (someChange.get()) {
                    NObjectElementBuilder obj2 = NElement.ofObjectBuilder();
                    obj2.addAll(children.toArray(new NElement[0]));
                    if(params!=null){
                        obj2.addParams(params);
                    }
                    obj2.name(obj.name().orNull());
                    obj2.addAnnotations(anns);
                    obj2.addComments(obj.comments());
                    return obj2.build();
                }
                return src;
            }
            case CUSTOM: {
                Object v1 = src.asCustom().get().value();
                if (context.getIndestructibleObjects() != null && context.getIndestructibleObjects().test(v1.getClass())) {
                    return src;
                }
                return context.createElement(v1);
            }
        }
        return src;
    }

    private List<NElementAnnotation> convertAnn_objectToElement(List<NElementAnnotation> oldList, NElement src, Type typeOfSrc, NElementFactoryContext context, NBooleanRef someChange){
        List<NElementAnnotation> newList = null;
        if(oldList!=null){
            newList=new ArrayList<>(oldList.size());
            boolean anyChange00=false;
            for (NElementAnnotation e : oldList) {
                NBooleanRef someChange0= NRef.ofBoolean(false);
                List<NElement> sub = convertList_objectToElement(e.params(), src, typeOfSrc, context, someChange0);
                if(someChange0.get()){
                    newList.add(NElement.ofAnnotation(e.name(), sub.toArray(new NElement[0])));
                    someChange.set(true);
                    anyChange00=true;
                }else{
                    newList.add(e);
                }
            }
            if(anyChange00){
                return newList;
            }
        }
        return oldList;
    }

    private NElement convertOne_objectToElement(NElement k, NElement src, Type typeOfSrc, NElementFactoryContext context, NBooleanRef someChange){
        NElement k2 = context.createElement(k);
        if(k2!=k){
            someChange.set();
            return k2;
        }
        return k;
    }
    private List<NElement> convertList_objectToElement(List<NElement> oldParams, NElement src, Type typeOfSrc, NElementFactoryContext context, NBooleanRef someChange){
        List<NElement> newParams = null;
        if(oldParams!=null){
            newParams=new ArrayList<>(oldParams.size());
            for (NElement e : oldParams) {
                boolean someChange0;
                NElement k2 = context.createElement(e);
                someChange0 = k2 != e;
                if (someChange0) {
                    someChange.set(true);
                    newParams.add(k2);
                } else {
                    newParams.add(e);
                }
            }
        }
        return newParams;
    }

    @Override
    public NElement createObject(NElement o, Type typeOfResult, NElementFactoryContext context) {
        return createElement(o, typeOfResult, context);
    }
}
