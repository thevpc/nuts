package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonDocument extends Comparable<TsonDocument> {
    TsonDocumentHeader getHeader();

    TsonElement getContent();

    boolean visit(TsonDocumentVisitor visitor);

    String toString(boolean compact);

    String toString(TsonFormat format);
}
