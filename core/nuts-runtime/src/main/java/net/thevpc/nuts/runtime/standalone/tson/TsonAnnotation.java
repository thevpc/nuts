package net.thevpc.nuts.runtime.standalone.tson;

import java.util.List;

public interface TsonAnnotation extends Comparable<TsonAnnotation> {

    String name();

    int size();

    TsonElement param(int index);

    TsonElementList params();

    /**
     * like params but never null
     *
     * @return
     */
    List<TsonElement> children();

    TsonAnnotationBuilder builder();

    boolean isParametrized();

    boolean isNamed();

    String toString(boolean compact);
}
