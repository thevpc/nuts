package net.thevpc.nuts.runtime.standalone.reflect.mapper;

import net.thevpc.nuts.reflect.NReflectMapper;
import net.thevpc.nuts.reflect.NReflectType;
import net.thevpc.nuts.reflect.NReflectMappingStrategy;
import net.thevpc.nuts.runtime.standalone.reflect.ReflectUtils;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalStateException;
import net.thevpc.nuts.util.NRef;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class DataObjectMappingStrategy implements NReflectMappingStrategy {
    private final Class from;
    private final Type to;
    private final List<FieldToFieldMapper> fieldMappers = new ArrayList<>();

    public DataObjectMappingStrategy(Class from, Type to) {
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
    public boolean copy(Object o, Object o2, NReflectMapper context) {
        boolean changed = false;
        for (FieldToFieldMapper fieldMapper : fieldMappers) {
            boolean map;
            try {
                map = fieldMapper.map(o, o2, context);
            } catch (RuntimeException ex) {
                throw new IllegalArgumentException(
                        "invalid mapping :  " + fieldMapper.from + " to " + fieldMapper.to
                        , ex);
            }
            if (map) {
                changed = true;
            }
        }
        return changed;
    }

    @Override
    public Object mapToType(Object o, NReflectType fromType, NReflectType toType, NReflectMapper context) {
        Object c = context.getRepository().getType(to).newInstance();
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

        public boolean map(Object a, Object b, NReflectMapper context) {
            return context.getAssignmentPolicy().applyMappingValue(
                    () -> {
                        try {
                            return from.get(a);
                        } catch (IllegalAccessException e) {
                            throw new NIllegalStateException(NMsg.ofC("unable to read source : %s", e), e);
                        }
                    },
                    () -> {
                        try {
                            return to.getField().get(b);
                        } catch (IllegalAccessException e) {
                            throw new NIllegalStateException(NMsg.ofC("unable to read target : %s", e), e);
                        }
                    },
                    mv -> {
                        try {
                            Object sv = context.mapToType(mv.getSourceValue(), toType);
                            Object tv = mv.getTargetValue();
                            if (!context.getEqualizer().equals(tv, sv)) {
                                to.getField().set(b, sv);
                                return true;
                            }
                            return false;
                        } catch (IllegalAccessException e) {
                            throw new NIllegalStateException(NMsg.ofC("unable to set target : %s", e), e);
                        }
                    }

            );
        }
    }
}
