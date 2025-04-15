package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonElementBase {
    static TsonElementBase of(TsonElementBase any){
        return any==null?Tson.ofNull():any;
    }

    TsonElementType type();

    TsonElement build();

    String toString();

    String toString(boolean compact);

    String toString(TsonFormat format);
}
