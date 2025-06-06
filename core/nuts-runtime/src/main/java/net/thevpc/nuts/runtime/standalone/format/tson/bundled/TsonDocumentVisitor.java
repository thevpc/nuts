package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

public interface TsonDocumentVisitor {
    /**
     * return true if need to continue with children
     *
     * @param element
     * @return
     */
    boolean visit(TsonElement element);

    boolean visit(TsonDocumentHeader element);

    boolean visit(TsonAnnotation element);

    boolean visit(TsonDocument element);
}
