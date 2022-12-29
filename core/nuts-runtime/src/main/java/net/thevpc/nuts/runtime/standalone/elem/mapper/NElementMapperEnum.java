package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;
import net.thevpc.nuts.util.NEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class NElementMapperEnum implements NElementMapper<Enum> {

    @Override
    public Object destruct(Enum src, Type typeOfSrc, NElementFactoryContext context) {
        return src;
    }

    @Override
    public NElement createElement(Enum o, Type typeOfSrc, NElementFactoryContext context) {
        if (o instanceof NEnum) {
            return context.elem().ofString(((NEnum) o).id());
        }
        return context.elem().ofString(String.valueOf(o));
    }

    @Override
    public Enum createObject(NElement o, Type to, NElementFactoryContext context) {
        NSession session = context.getSession();
        switch (o.type()) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG: {
                return (Enum) ((Class) to).getEnumConstants()[o.asInt().get(session)];
            }
            case STRING: {
                Class cc = ReflectUtils.getRawClass(to);
                String name = o.asString().get(session);
                if (NEnum.class.isAssignableFrom(cc)) {
                    return (Enum) NEnum.parse(cc, name).get(session);
                }
                try {
                    return Enum.valueOf(cc, name);
                } catch (RuntimeException ex) {
                    LenientParser y = cache.computeIfAbsent(cc, LenientParser::new);
                    Object o2 = y.get(name);
                    if (o2 != null) {
                        return (Enum) o2;
                    }
                    throw ex;
                }
            }
        }
        throw new NUnsupportedEnumException(session, o.type());
    }

    private static Map<Class, LenientParser> cache = new HashMap<>();

    private static class LenientParser {
        Map<String, Object> fields_exact = new HashMap<>();
        Map<String, Object> fields_lower = new HashMap<>();
        Map<String, Object> fields_shrink = new HashMap<>();
        Map<String, Object> exact = new HashMap<>();
        Map<String, Object> lower = new HashMap<>();
        Map<String, Object> lowerShrink = new HashMap<>();

        public LenientParser(Class enumClass) {
            Object[] enumConstants = enumClass.getEnumConstants();
            if (enumConstants != null) {
                for (Object enumConstant : enumConstants) {
                    String s = String.valueOf(enumConstant);
                    exact.put(s, enumConstant);
                    String lowerCased = s.toLowerCase();
                    lower.put(lowerCased, enumConstant);
                    lowerCased = lowerCased.replace("_", "");
                    lowerShrink.put(lowerCased, enumConstant);
                }
                if (exact.size() != enumConstants.length) {
                    exact.clear();
                }
                if (lower.size() != enumConstants.length) {
                    lower.clear();
                }
                if (lowerShrink.size() != enumConstants.length) {
                    lowerShrink.clear();
                }
                for (Field declaredField : enumClass.getDeclaredFields()) {
                    if (
                            Modifier.isStatic(declaredField.getModifiers())
                                    && Modifier.isPublic(declaredField.getModifiers())
                                    && declaredField.getType().equals(enumClass)
                    ) {
                        String s = declaredField.getName();
                        Object enumConstant = null;
                        try {
                            enumConstant = declaredField.get(null);
                        } catch (IllegalAccessException e) {
                            //
                        }
                        if (enumConstant != null) {
                            fields_exact.put(s, enumConstant);
                            String lowerCased = s.toLowerCase();
                            fields_lower.put(lowerCased, enumConstant);
                            lowerCased = lowerCased.replace("_", "");
                            fields_shrink.put(lowerCased, enumConstant);
                        }
                    }
                }
            }
        }

        public Object get(String name) {
            Object r = exact.get(name);
            if (r != null) {
                return (Enum) r;
            }
            String lowerCased = name.toLowerCase();
            r = lower.get(lowerCased);
            if (r != null) {
                return (Enum) r;
            }
            String lowerCased2 = lowerCased.replace("_", "");
            r = lowerShrink.get(lowerCased2);
            if (r != null) {
                return (Enum) r;
            }
            r = fields_exact.get(name);
            if (r != null) {
                return (Enum) r;
            }
            r = fields_lower.get(lowerCased);
            if (r != null) {
                return (Enum) r;
            }
            r = fields_shrink.get(lowerCased2);
            if (r != null) {
                return (Enum) r;
            }
            return null;
        }
    }
}
