package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.Collection;

public interface TsonCustomBuilder extends TsonElementBuilder {
    TsonCustomBuilder setCustom(Object element);

    Object getCustom();

    TsonElement build();

    ////////////////////////////////////////////////

    TsonCustomBuilder comments(TsonComments comments);

    TsonCustomBuilder setComments(TsonComments comments);

    TsonCustomBuilder setAnnotations(TsonAnnotation... annotations);

    TsonCustomBuilder addAnnotations(TsonAnnotation... annotations);

    TsonCustomBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonCustomBuilder annotation(String name, TsonElementBase... elements);

    TsonCustomBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonCustomBuilder addAnnotation(TsonAnnotation annotation);

    TsonCustomBuilder removeAnnotationAt(int index);

    TsonCustomBuilder clearAnnotations();
}
