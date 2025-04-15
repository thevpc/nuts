package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.Collection;

public interface TsonPairBuilder extends TsonElementBuilder{
    TsonPairBuilder merge(TsonPair other);

    TsonPairBuilder reset();

    TsonElement key();

    TsonPairBuilder key(TsonElementBase key);

    TsonElement value();

    TsonPairBuilder value(TsonElementBase value);

    ////////////////////////////////////////////////

    TsonPairBuilder comments(TsonComments comments);

    TsonPairBuilder setComments(TsonComments comments);

    TsonPairBuilder setAnnotations(TsonAnnotation... annotations);

    TsonPairBuilder addAnnotations(TsonAnnotation... annotations);

    TsonPairBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonPairBuilder annotation(String name, TsonElementBase... elements);

    TsonPairBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonPairBuilder addAnnotation(TsonAnnotation annotation);

    TsonPairBuilder removeAnnotationAt(int index);

    TsonPairBuilder clearAnnotations();
    TsonPair build();
}
