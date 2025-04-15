package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonStreamParser {
    Object source();

    void setConfig(TsonStreamParserConfig config);

    void parseElement() ;

    void parseDocument() ;
}
