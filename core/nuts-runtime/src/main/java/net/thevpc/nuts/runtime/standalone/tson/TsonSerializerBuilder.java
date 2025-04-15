package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonSerializerBuilder {
    <T> TsonSerializerBuilder setSerializer(Class<T> type, TsonObjectToElement<T> objToElem);

    <T> TsonSerializerBuilder setDeserializer(TsonElementType type, String name, Class<T> to, TsonElementToObject<T> elemToObj);

    <T> TsonSerializerBuilder setDeserializer(TsonElementType type, Class<T> to, TsonElementToObject<T> elemToObj);

    <T> TsonCustomDeserializer<T> customDeserializer(Class<T> to);

    TsonSerializer build();
}
