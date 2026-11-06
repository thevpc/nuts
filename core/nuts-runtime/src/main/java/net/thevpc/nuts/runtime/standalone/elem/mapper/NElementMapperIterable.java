package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementDeserializerContext;
import net.thevpc.nuts.elem.NElementSerializerContext;
import net.thevpc.nuts.elem.NToElement;
import net.thevpc.nuts.runtime.standalone.elem.item.DefaultNArrayElement;

import net.thevpc.nuts.runtime.standalone.elem.CoreNElementUtils;
import net.thevpc.nuts.runtime.standalone.reflect.SimpleParametrizedType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIllegalArgumentException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Iterables that are not {@link java.util.Collection}s (and have no dedicated mapper) are mapped as arrays,
 * the same way collections are. Deserialization creates a collection (see {@link NElementMapperCollection}).
 */
public class NElementMapperIterable implements NElementMapper<Iterable> {
    private final NElementMapperCollection collectionMapper = new NElementMapperCollection();
    private final NElementMapperObjReflect objectMapper = new NElementMapperObjReflect();

    @Override
    public Object toSimple(NElementSerializerContext<Iterable> context) {
        Iterable it = context.instance();
        if (it instanceof NToElement) {
            // NToElement and Iterable are unrelated interfaces : keep honoring the explicit element form
            return ((NToElement) it).toElement();
        }
        List<Object> values = new ArrayList<>();
        for (Object o : it) {
            values.add(context.toSimple(o, null));
        }
        return values;
    }

    @Override
    public NElement toElement(NElementSerializerContext<Iterable> context) {
        Iterable it = context.instance();
        if (it instanceof NToElement) {
            return ((NToElement) it).toElement();
        }
        List<NElement> values = new ArrayList<>();
        for (Object o : it) {
            values.add(context.toElement(o));
        }
        return new DefaultNArrayElement(null, null, values);
    }

    @Override
    public Iterable toObject(NElementDeserializerContext context) {
        Type to = context.instanceType();
        Class<?> cls = CoreNElementUtils.rawClass(to, Iterable.class);
        if (cls.equals(Iterable.class) || Collection.class.isAssignableFrom(cls)) {
            return (Iterable) collectionMapper.toObject(context);
        }
        // a custom Iterable : build it from the list of its elements when it accepts one,
        // otherwise it is a plain bean
        if (context.element().isAnyArray()) {
            Constructor<?> c = findListConstructor(cls);
            if (c != null) {
                Type elemType = CoreNElementUtils.resolveTypeArguments(to, Iterable.class, context.typesRepository())[0];
                Object list = context.toObject(context.element(),
                        elemType == null ? List.class : new SimpleParametrizedType(List.class, new Type[]{elemType}, null));
                try {
                    c.setAccessible(true);
                    return (Iterable) c.newInstance(list);
                } catch (InvocationTargetException ex) {
                    throw new NIllegalArgumentException(NMsg.ofC("unable to create %s", to), ex.getCause() == null ? ex : ex.getCause());
                } catch (ReflectiveOperationException ex) {
                    throw new NIllegalArgumentException(NMsg.ofC("unable to create %s", to), ex);
                }
            }
        }
        return (Iterable) objectMapper.toObject(context);
    }

    private static Constructor<?> findListConstructor(Class<?> cls) {
        for (Class<?> p : new Class[]{List.class, Collection.class, Iterable.class}) {
            try {
                return cls.getDeclaredConstructor(p);
            } catch (NoSuchMethodException ex) {
                //try next
            }
        }
        return null;
    }

}
