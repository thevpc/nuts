package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapperContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectTypeMapper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class CollectionToCollectionTypeMapper implements NReflectTypeMapper {
    private final Type to;
    private final Type componentType;

    public CollectionToCollectionTypeMapper(Type to) {
        this.to = to;
        componentType = TypeHelper.asTypeArgs(to, Collection.class).get()[0];
    }

    @Override
    public boolean copy(Object fromObj, Object toObj, NReflectMapperContext context) {
        Collection<Object> fromColl = ((Collection) fromObj);
        if (fromColl instanceof List) {
            return ArrayToCollectionTypeMapper.copyCollection((List) fromObj, (Collection) toObj, context, componentType);
        }
        return ArrayToCollectionTypeMapper.copyCollection(new ArrayList<>((Collection) fromObj), (Collection) toObj, context, componentType);
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
        Collection c = (Collection) context.repository().getType(to).newInstance();
        copy(o, c, context);
        return c;
    }
}
