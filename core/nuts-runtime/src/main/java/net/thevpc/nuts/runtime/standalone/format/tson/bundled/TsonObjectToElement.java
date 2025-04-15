package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonObjectToElement<T> {
    TsonElementBase toElement(T object, TsonObjectContext context);
}
