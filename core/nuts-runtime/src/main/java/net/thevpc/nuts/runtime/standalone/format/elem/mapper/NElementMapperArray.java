package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.runtime.standalone.format.elem.DefaultNElementFactoryService;
import net.thevpc.nuts.util.NMsg;

import java.lang.reflect.Array;
import java.lang.reflect.Type;

public class NElementMapperArray implements NElementMapper<Object> {

    public NElementMapperArray() {
    }

    @Override
    public Object createObject(NElement json, Type typeOfResult, NElementFactoryContext context) {
        if(json instanceof NListContainerElement) {
            NListContainerElement e = (NListContainerElement) json;
            if (typeOfResult == null) {
                typeOfResult = Object[].class;
            }
            Class arrType = (Class) typeOfResult;
            Class componentType = arrType.getComponentType();
            switch (componentType.getName()) {
                case "boolean": {
                    boolean[] x = new boolean[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).get().asLiteral().asBoolean().get();
                    }
                    return x;
                }
                case "byte": {
                    byte[] x = new byte[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).get().asLiteral().asByte().get();
                    }
                    return x;
                }
                case "short": {
                    short[] x = new short[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).get().asLiteral().asShort().get();
                    }
                    return x;
                }
                case "int": {
                    int[] x = new int[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).get().asLiteral().asInt().get();
                    }
                    return x;
                }
                case "long": {
                    long[] x = new long[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).get().asLiteral().asLong().get();
                    }
                    return x;
                }
                case "float": {
                    float[] x = new float[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).get().asLiteral().asFloat().get();
                    }
                    return x;
                }
                case "double": {
                    double[] x = new double[e.size()];
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = e.get(i).get().asLiteral().asDouble().get();
                    }
                    return x;
                }
                default: {
                    Object[] x = (Object[]) Array.newInstance(componentType, e.size());
                    for (int i = 0; i < e.size(); i++) {
                        x[i] = context.createObject(e.get(i).get(), componentType);
                    }
                    return x;
                }

            }
        }else {
            throw new NIllegalArgumentException(NMsg.ofC("expected an array of objects", typeOfResult));
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
