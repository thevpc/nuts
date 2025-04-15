package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonDocumentHeaderBuilder {
    TsonDocumentHeaderBuilder reset();

    TsonDocumentHeaderBuilder parse(TsonAnnotation a);

    TsonDocumentHeaderBuilder setVersion(String version);

    TsonDocumentHeaderBuilder setEncoding(String encoding);

    String getVersion();

    String getEncoding();

    TsonElement[] getParams();

    TsonDocumentHeaderBuilder with(TsonElementBase... elements);

    TsonDocumentHeaderBuilder addParam(TsonElementBase element);

    TsonDocumentHeaderBuilder removeParam(TsonElementBase element);

    TsonDocumentHeaderBuilder addParam(TsonElementBase element, int index);

    TsonDocumentHeaderBuilder removeParamAt(int index);

    TsonDocumentHeaderBuilder addParams(TsonElementBase... element);

    TsonDocumentHeaderBuilder addParams(Iterable<? extends TsonElementBase> element);

    TsonAnnotation toAnnotation();

    TsonDocumentHeader build();
}
