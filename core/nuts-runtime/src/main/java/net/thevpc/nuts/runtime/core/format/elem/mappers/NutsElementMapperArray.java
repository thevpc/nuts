package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

public class NutsElementMapperArray implements NutsElementMapper<Object> {

    public NutsElementMapperArray() {
    }

    @Override
    public Object createObject(NutsElement json, Type typeOfResult, NutsElementFactoryContext context) {
        NutsArrayElement e = (NutsArrayElement) json;
        Class arrType = (Class) typeOfResult;
        Class componentType = arrType.getComponentType();
        switch (componentType.getName()) {
            case "boolean": {
                boolean[] x = new boolean[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).asPrimitive().getBoolean();
                }
                return x;
            }
            case "byte": {
                byte[] x = new byte[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).asPrimitive().getByte();
                }
                return x;
            }
            case "short": {
                short[] x = new short[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).asPrimitive().getShort();
                }
                return x;
            }
            case "int": {
                int[] x = new int[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).asPrimitive().getInt();
                }
                return x;
            }
            case "long": {
                long[] x = new long[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).asPrimitive().getLong();
                }
                return x;
            }
            case "float": {
                float[] x = new float[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).asPrimitive().getFloat();
                }
                return x;
            }
            case "double": {
                double[] x = new double[e.size()];
                for (int i = 0; i < e.size(); i++) {
                    x[i] = e.get(i).asPrimitive().getDouble();
                }
                return x;
            }
            default: {
                Object[] x = (Object[]) Array.newInstance(componentType, e.size());
                for (int i = 0; i < e.size(); i++) {
                    x[i] = context.elementToObject(e.get(i), componentType);
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
