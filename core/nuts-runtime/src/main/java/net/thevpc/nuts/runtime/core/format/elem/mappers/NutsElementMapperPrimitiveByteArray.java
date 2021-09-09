package net.thevpc.nuts.runtime.core.format.elem.mappers;

import net.thevpc.nuts.NutsArrayElement;
import net.thevpc.nuts.NutsElement;
import net.thevpc.nuts.NutsElementFactoryContext;
import net.thevpc.nuts.NutsElementMapper;
import net.thevpc.nuts.runtime.core.format.elem.DefaultNutsElementFactoryService;

import java.lang.reflect.Type;

public class NutsElementMapperPrimitiveByteArray implements NutsElementMapper<byte[]> {

    public NutsElementMapperPrimitiveByteArray() {
    }

    @Override
    public NutsElement createElement(byte[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._createArray1(src, context);
    }

    @Override
    public Object destruct(byte[] src, Type typeOfSrc, NutsElementFactoryContext context) {
        return DefaultNutsElementFactoryService._destructArray1(src, context);
    }

    @Override
    public byte[] createObject(NutsElement o, Type typeOfResult, NutsElementFactoryContext context) {
        NutsArrayElement earr = o.asArray();
        byte[] arr = new byte[earr.size()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) context.elementToObject(earr.get(i), byte.class);
        }
        return arr;
    }
}
