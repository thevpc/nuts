package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonNumber extends TsonElement{
    TsonNumberLayout numberLayout();
    String numberSuffix();
}
