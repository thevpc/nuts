package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsArrayElement;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
import net.thevpc.nuts.runtime.standalone.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

public class NutsElementMapperArray implements NutsElementMapper<Object> {

    public NutsElementMapperArray() {
    }

    @Override
    public Object createObject(NutsElement json, Type typeOfResult, NutsElementFactoryContext context) {
        NutsSession session = context.getSession();
        NutsArrayElement e = (NutsArrayElement) json;
        Class arrType = (Class) typeOfResult;
        Class componentType = arrType.getComponentType();
        switch (componentType.getName()) {
            case "boolean": {
                boolean[] x = new boolean[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get(session).asBoolean().get(session);
                }
                return x;
            }
            case "byte": {
                byte[] x = new byte[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get(session).asByte().get(session);
                }
                return x;
            }
            case "short": {
                short[] x = new short[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get(session).asShort().get(session);
                }
                return x;
            }
            case "int": {
                int[] x = new int[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get(session).asInt().get(session);
                }
                return x;
            }
            case "long": {
                long[] x = new long[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get(session).asLong().get(session);
                }
                return x;
            }
            case "float": {
                float[] x = new float[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get(session).asFloat().get(session);
                }
                return x;
            }
            case "double": {
                double[] x = new double[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).get(session).asDouble().get(session);
                }
                return x;
            }
            default: {
                Object[] x = (Object[]) Array.newInstance(componentType, e.size());
                for (int i = 0; i < e.size(); i++) {
                    x[i] = context.elementToObject(e.get(i).get(session), componentType);
                }
                return x;
            }

        }
    }

    public NutsElement createElement(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(Object src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

}
