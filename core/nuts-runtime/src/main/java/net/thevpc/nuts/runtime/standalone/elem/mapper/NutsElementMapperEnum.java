package net.thevpc.nuts.runtime.standalone.elem.mapper;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElement;
import net.thevpc.nuts.elem.NutsElementFactoryContext;
import net.thevpc.nuts.elem.NutsElementMapper;
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
        NutsSession session = context.getSession();
        switch (o.type()) {
            case BYTE:
            case SHORT:
            case INTEGER:
            case LONG: {
                return (Enum) ((Class) to).getEnumConstants()[o.asInt().get(session)];
            }
            case STRING: {
                Class cc = ReflectUtils.getRawClass(to);
                if(NutsEnum.class.isAssignableFrom(cc)){
                    return (Enum) NutsEnum.parse(cc, o.asString().get(session)).get(session);
                }
                return Enum.valueOf(cc, o.asString().get(session));
            }
        }
        throw new NutsUnsupportedEnumException(session, o.type());
    }
}
