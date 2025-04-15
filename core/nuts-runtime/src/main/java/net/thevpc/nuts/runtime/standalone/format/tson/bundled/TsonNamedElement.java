package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonNamedElement extends TsonElement {
    boolean isNamed();

    String name();
}
