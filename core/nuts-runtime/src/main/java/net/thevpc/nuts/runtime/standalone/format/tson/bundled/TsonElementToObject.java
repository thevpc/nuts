package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonElementToObject<T> {
    T toObject(TsonElement element,Class<T> to, TsonObjectContext context);
}
