package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonObjectContext {
    <T> TsonElement elem(T any);

    <T> T obj(TsonElement element, Class<T> clazz);

    boolean isPreferName() ;

    TsonObjectContext setPreferName(boolean preferName) ;

    TsonObjectContext copy() ;
}
