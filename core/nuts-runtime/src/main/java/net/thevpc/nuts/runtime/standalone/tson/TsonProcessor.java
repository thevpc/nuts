package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonProcessor {
    TsonElement removeComments(TsonElement element);

    TsonElement resolveAliases(TsonElement element);
}
