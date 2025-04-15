package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonDocumentBuilder {
    TsonDocumentHeader getHeader();

    TsonDocumentHeader header();

    TsonDocumentBuilder setHeader(TsonDocumentHeader header);

    TsonDocumentBuilder header(TsonDocumentHeader header);

    TsonElement value();

    TsonElement content();

    TsonDocumentBuilder setValue(TsonElementBase value);

    TsonDocumentBuilder content(TsonElementBase value);

    TsonDocument build();

    String toString();

    String toString(boolean compact);

    String toString(TsonFormat format);

}
