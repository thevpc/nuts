package net.thevpc.nuts.runtime.standalone.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapper;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectMappingStrategy;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class CollectionToCollectionMappingStrategy implements NReflectMappingStrategy {
    private final Type to;
    private final Type componentType;

    public CollectionToCollectionMappingStrategy(Type to) {
        this.to = to;
        componentType = TypeHelper.asTypeArgs(to, Collection.class).get()[0];
    }

    @Override
    public boolean copy(Object fromObj, Object toObj, NReflectMapper context) {
        Collection<Object> fromColl = ((Collection) fromObj);
        if (fromColl instanceof List) {
            return ArrayToCollectionMappingStrategy.copyCollection((List) fromObj, (Collection) toObj, context, componentType);
        }
        return ArrayToCollectionMappingStrategy.copyCollection(new ArrayList<>((Collection) fromObj), (Collection) toObj, context, componentType);
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapper context) {
        Collection c = (Collection) context.getRepository().getType(to).newInstance();
        copy(o, c, context);
        return c;
    }
}
