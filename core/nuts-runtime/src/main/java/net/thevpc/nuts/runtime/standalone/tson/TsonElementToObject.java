package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonElementToObject<T> {
    T toObject(TsonElement element,Class<T> to, TsonObjectContext context);
}
