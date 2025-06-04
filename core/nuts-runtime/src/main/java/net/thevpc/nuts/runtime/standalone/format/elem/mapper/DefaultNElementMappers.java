package net.thevpc.nuts.runtime.standalone.format.elem.mapper;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElementMapperBuilder;
import net.thevpc.nuts.elem.NElementMappers;
import net.thevpc.nuts.reflect.NReflectRepository;
import net.thevpc.nuts.spi.NSupportLevelContext;

import java.lang.reflect.Type;

public class DefaultNElementMappers implements NElementMappers {
    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    @Override
    public <T> NElementMapperBuilder<T> mapperBuilder(Type type) {
        return new DefaultNElementMapperBuilder<>(NReflectRepository.of(), type);
    }
}
