package net.thevpc.nuts.runtime.standalone.util.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapperContext;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectTypeMapper;
import net.thevpc.nuts.util.NRef;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class DataObjectTypeMapper implements NReflectTypeMapper {
    private final Class from;
    private final Type to;
    private final List<FieldToFieldMapper> fieldMappers = new ArrayList<>();

    public DataObjectTypeMapper(Class from, Type to) {
        this.from = from;
        this.to = to;
        Map<String, TypeHelper.GenericField> fromFields = new LinkedHashMap<>();
        Map<String, TypeHelper.GenericField> toFields = new LinkedHashMap<>();
        resolveFields(from, fromFields);
        resolveFields(to, toFields);
        for (Map.Entry<String, TypeHelper.GenericField> ff : fromFields.entrySet()) {
            TypeHelper.GenericField n = toFields.get(ff.getKey());
            if (n != null) {
                fieldMappers.add(new FieldToFieldMapper(ff.getValue().getField(), n));
            }
        }
    }

    @Override
    public boolean copy(Object o, Object o2, NReflectMapperContext context) {
        boolean changed = false;
        for (FieldToFieldMapper fieldMapper : fieldMappers) {
            boolean map;
            try {
                map = fieldMapper.map(o, o2, context);
            }catch (RuntimeException ex){
                throw new IllegalArgumentException(
                        "invalid mapping :  "+fieldMapper.from+" to "+fieldMapper.to
                        ,ex);
            }
            if (map) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapperContext context) {
        Object c = context.repository().getType(to).newInstance();
        copy(o, c, context);
        return c;
    }

    private void resolveFields(Type c, Map<String, TypeHelper.GenericField> all) {
        if (c != null) {
            for (TypeHelper.GenericField genericField : TypeHelper.getDeclaredFields(c)) {
                int m = genericField.getModifiers();
                String fieldName = genericField.getName();
                if (!Modifier.isStatic(m) && !Modifier.isFinal(m)) {
                    if (!all.containsKey(fieldName)) {
                        all.put(fieldName, genericField);
                    }
                }
            }
            Type s = TypeHelper.getGenericSuperclass(c);
            if (s != null) {
                resolveFields(s, all);
            }
        }
    }


    private static class FieldToFieldMapper {
        private final Field from;
        private final TypeHelper.GenericField to;
        private final Type toType;

        public FieldToFieldMapper(Field from, TypeHelper.GenericField to) {
            this.from = from;
            this.to = to;
            from.setAccessible(true);
            to.getField().setAccessible(true);
            toType = to.getType();
        }

        public boolean map(Object a, Object b, NReflectMapperContext context) {
            try {
                NRef<Object> srcVal = null;
                NRef<Object> targetVal = null;
                switch (context.getSource()) {
                    case NULL : {
                        srcVal = NRef.of(from.get(a));
                        if (!srcVal.isNull()) {
                            return false;
                        }
                        break;
                    }
                    case NON_NULL : {
                        srcVal = NRef.of(from.get(a));
                        if (srcVal.isNull()) {
                            return false;
                        }
                        break;
                    }
                    case BLANK : {
                        srcVal = NRef.of(from.get(a));
                        if (!srcVal.isBlank()) {
                            return false;
                        }
                        break;
                    }
                    case NON_BLANK : {
                        srcVal = NRef.of(from.get(a));
                        if (srcVal.isBlank()) {
                            return false;
                        }
                        break;
                    }
                }
                switch (context.getTarget()) {
                    case NULL : {
                        targetVal = NRef.of(to.getField().get(b));
                        if (!targetVal.isNull()) {
                            return false;
                        }
                        break;
                    }
                    case NON_NULL : {
                        targetVal = NRef.of(to.getField().get(b));
                        if (targetVal.isNull()) {
                            return false;
                        }
                        break;
                    }
                    case BLANK : {
                        targetVal = NRef.of(to.getField().get(b));
                        if (!targetVal.isBlank()) {
                            return false;
                        }
                        break;
                    }
                    case NON_BLANK : {
                        targetVal = NRef.of(to.getField().get(b));
                        if (targetVal.isBlank()) {
                            return false;
                        }
                        break;
                    }
                }
                Object v = srcVal == null ? from.get(a) : srcVal.get();
                Object v2 = context.mapToType(v, context.repository().getType(toType));
                Object nv2 = targetVal == null ? to.getField().get(b) : targetVal.get();
                if (!context.getEq().equals(nv2, v2)) {
                    if(v2==null){
                        switch (to.getType().getTypeName()){
                            case "boolean":{
                                v2=false;
                                break;
                            }
                        }
                    }
                    to.getField().set(b, v2);
                    return true;
                }
                return false;
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
