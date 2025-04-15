package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonProcessor {
    TsonElement removeComments(TsonElement element);

    TsonElement resolveAliases(TsonElement element);
}
