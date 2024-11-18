package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementFactoryContext;
import net.thevpc.nuts.elem.NElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNElementFactoryService;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

public class NElementMapperArray implements NElementMapper<Object> {

    public NElementMapperArray() {
    }

    @Override
    public Object createObject(NElement json, Type typeOfResult, NElementFactoryContext context) {
        NSession session = context.getSession();
        NArrayElement e = (NArrayElement) json;
        Class arrType = (Class) typeOfResult;
        Class componentType = arrType.getComponentType();
        switch (componentType.getName()) {
            case "boolean": {
                boolean[] x = new boolean[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get().asBoolean().get();
                }
                return x;
            }
            case "byte": {
                byte[] x = new byte[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get().asByte().get();
                }
                return x;
            }
            case "short": {
                short[] x = new short[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get().asShort().get();
                }
                return x;
            }
            case "int": {
                int[] x = new int[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get().asInt().get();
                }
                return x;
            }
            case "long": {
                long[] x = new long[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get().asLong().get();
                }
                return x;
            }
            case "float": {
                float[] x = new float[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get().asFloat().get();
                }
                return x;
            }
            case "double": {
                double[] x = new double[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get().asDouble().get();
                }
                return x;
            }
            default: {
                Object[] x = (Object[]) Array.newInstance(componentType, e.size());
                for (int i = 0; i < e.size(); i++) {
                    x[i] = context.elementToObject(e.get(i).get(), componentType);
                }
                return x;
            }

        }
    }

    public NElement createElement(Object src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(Object src, Type typeOfSrc, NElementFactoryContext context) {
        return DefaultNElementFactoryService._destructArray1(src, context);
    }

}
