package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonNumber extends TsonElement{
    TsonNumberLayout numberLayout();
    String numberSuffix();
}
