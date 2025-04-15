package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonObjectToElement<T> {
    TsonElementBase toElement(T object, TsonObjectContext context);
}
