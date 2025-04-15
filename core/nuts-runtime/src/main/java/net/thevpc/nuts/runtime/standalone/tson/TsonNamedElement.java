package net.thevpc.nuts.runtime.standalone.tson;

public interface TsonNamedElement extends TsonElement {
    boolean isNamed();

    String name();
}
