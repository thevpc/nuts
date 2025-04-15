package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonStreamParser {
    Object source();

    void setConfig(TsonStreamParserConfig config);

    void parseElement() ;

    void parseDocument() ;
}
