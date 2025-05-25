package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import net.thevpc.nuts.util.NOptional;

import java.util.List;

public interface TsonAnnotation extends Comparable<TsonAnnotation> {

    NOptional<String> name();

    int size();

    TsonElement param(int index);

    List<TsonElement> params();

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
