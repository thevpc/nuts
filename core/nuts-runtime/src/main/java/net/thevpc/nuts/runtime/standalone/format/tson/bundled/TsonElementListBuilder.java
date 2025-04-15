package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonElementListBuilder{
    TsonElement getAt(int index);

    TsonElement get(String name);

    TsonElement get(TsonElementBase name);

    int size();

    TsonElementBaseList build();
}
