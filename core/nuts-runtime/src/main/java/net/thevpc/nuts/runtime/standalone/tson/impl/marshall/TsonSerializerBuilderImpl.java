package net.thevpc.nuts.runtime.standalone.tson.impl.marshall;

import net.thevpc.nuts.runtime.standalone.tson.*;
import net.thevpc.nuts.runtime.standalone.tson.impl.marshall.reflect.JavaWord;

public class TsonSerializerBuilderImpl implements TsonSerializerBuilder {

    private TsonSerializerConfig config;

    public TsonSerializerBuilderImpl(TsonSerializerConfig other) {
        config = new TsonSerializerConfig(other);
    }

    public TsonSerializerBuilderImpl() {
        config = new TsonSerializerConfig();
        config.registerDefaults();
    }

    @Override
    public <T> TsonSerializerBuilder setSerializer(Class<T> type, TsonObjectToElement<T> objToElem) {
        config.registerObjToElemConverter(type, objToElem);
        return this;
    }

    @Override
    public <T> TsonSerializerBuilder setDeserializer(TsonElementType type, Class<T> to, TsonElementToObject<T> elemToObj) {
        return setDeserializer(type, null, to, elemToObj);
    }

    @Override
    public <T> TsonSerializerBuilder setDeserializer(TsonElementType type, String name, Class<T> to, TsonElementToObject<T> elemToObj) {
        config.registerElemToObjConverter(type, name, to, elemToObj);
        return this;
    }

    @Override
    public <T> TsonCustomDeserializer<T> customDeserializer(Class<T> to) {
        return config.customDeserializer(to);
    }

    @Override
    public TsonSerializer build() {
        return new TsonSerializerImpl(config);
    }
}
