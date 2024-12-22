package net.thevpc.nuts.reflect;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

public interface NReflectCopy extends NComponent {
    static NReflectCopy of() {
        return NExtensions.of(NReflectCopy.class);
    }

    NReflectRepository getReflectRepository();

    NReflectCopy setReflectRepository(NReflectRepository r);

    void include(String... names);

    void excludeProperty(String... names);

    void rename(String from, String to);

    void setPropertyConverter(String property, Converter converter);

    void setTypeConverter(NReflectType fromType, NReflectType toType, Converter converter);

    void copy(Object from, Object to);
    Object convert(Object from, NReflectType to);

    interface Context {
        NReflectRepository getReflectRepository();
    }
    interface Converter {
        Object convert(Object value, NReflectType fromType, NReflectType toType,Context context);
    }
}
