package net.thevpc.nuts.elem;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NComponent;

import java.lang.reflect.Type;

public interface NElementMappers extends NComponent {
    static NElementMappers of() {
        return NExtensions.of(NElementMappers.class);
    }
    <T> NElementMapperBuilder<T> mapperBuilder(Type type);
}
