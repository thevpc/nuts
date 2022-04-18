package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.reflect.ReflectUtils;

import java.lang.reflect.Type;

public class NutsElementMapperEnum implements NutsElementMapper<Enum> {

    @Override
    public Object destruct(Enum src, Type typeOfSrc, NutsElementFactoryContext context) {
        return src;
    }

    @Override
    public NutsElement createElement(Enum o, Type typeOfSrc, NutsElementFactoryContext context) {
        if(o instanceof NutsEnum){
            return context.elem().ofString(((NutsEnum) o).id());
        }
        return context.elem().ofString(String.valueOf(o));
    }

    @Override
    public Enum createObject(NutsElement o, Type to, NutsElementFactoryContext context) {
        switch (o.type()) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG: {
                NutsPrimitiveElement p = o.asPrimitive();
                return (Enum) ((Class) to).getEnumConstants()[p.getInt()];
            }
            case STRING: {
                NutsPrimitiveElement p = o.asPrimitive();
                Class cc = ReflectUtils.getRawClass(to);
                if(NutsEnum.class.isAssignableFrom(cc)){
                    return (Enum) NutsEnum.parse(cc, p.getString()).get(context.getSession());
                }
                return Enum.valueOf(cc, p.getString());
            }
        }
        throw new NutsUnsupportedEnumException(context.getSession(), o.type());
    }
}
