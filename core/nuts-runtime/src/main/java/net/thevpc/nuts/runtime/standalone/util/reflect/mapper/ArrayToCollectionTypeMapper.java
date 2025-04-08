package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapperContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectTypeMapper;

import java.lang.reflect.Array;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class ArrayToCollectionTypeMapper implements NReflectTypeMapper {
    private final Type to;
    private final Type componentType;

    public ArrayToCollectionTypeMapper(Type to) {
        this.to = to;
        componentType = TypeHelper.asTypeArgs(to, Collection.class).get()[0];
    }

    @Override
    public boolean copy(Object fromObj, Object toObj, NReflectMapperContext context) {
        Collection<Object> toColl = (Collection) toObj;
        List<Object> ll=new ArrayList<>();
        //take into consideration primitive arrays!!
        int len = Array.getLength(fromObj);
        for (int i = 0; i < len; i++) {
            ll.add(Array.get(fromObj, i));
        }
        return copyCollection(ll, toColl,context, componentType);
    }

    public static boolean copyCollection(List<Object> fromObj, Collection<Object> toObj, NReflectMapperContext context, Type componentType){
        Collection<Object> toColl = (Collection) toObj;
        List<Object> toList = (toColl instanceof List)?((List)toColl):new ArrayList<>();
        int len = fromObj.size();
        boolean changed=false;
        for (int i = 0; i < len; i++) {
            Object nv = context.mapToType(fromObj.get(i), context.repository().getType(componentType));
            boolean checkEq=true;
            while(i>=toList.size()){
                toList.add(null);
                changed=true;
                checkEq=false;
            }
            if(checkEq){
                Object tv = toList.get(i);
                if(!context.equalizer().equals(tv,nv)) {
                    toList.set(i,nv);
                    changed=true;
                }
            }else{
                toList.set(i,nv);
            }
        }
        if(toList!=toColl){
            toColl.clear();
            toColl.addAll(toList);
        }
        return changed;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
        Collection c = (Collection) context.repository().getType(to).newInstance();
        copy(o, c, context);
        return c;
    }
}
