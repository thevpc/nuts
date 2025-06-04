package net.thevpc.nuts.reflect;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.lang.reflect.Type;

public interface NReflectMapper extends NComponent {
    static NReflectMapper of() {
        return NExtensions.of(NReflectMapper.class);
    }

    NReflectRepository getReflectRepository();

    NReflectMapper setReflectRepository(NReflectRepository r);

    boolean copy(Object from, Object to);

    Object mapToType(Object from, Type to);

    Object mapToType(Object from, NReflectType to);

    interface Context {
        NReflectRepository getReflectRepository();
    }

    interface Converter {
        Object convert(Object value, String path, NReflectType fromType, NReflectType toType, NReflectMapperContext context);
    }

}
