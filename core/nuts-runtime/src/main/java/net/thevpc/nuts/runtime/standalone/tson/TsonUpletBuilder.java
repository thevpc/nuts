package net.thevpc.nuts.runtime.standalone.tson;

import java.util.Collection;

public interface TsonUpletBuilder extends Iterable<TsonElement>, TsonElementBuilder {
    String name();

    boolean isNamed();

    boolean isBlank();

    TsonUpletBuilder name(String name);

    TsonUpletBuilder reset();

    TsonUpletBuilder merge(TsonElementBase element);

    TsonUpletBuilder addAll(Iterable<? extends TsonElementBase> element);

    TsonUpletBuilder addAll(TsonElement[] element);

    TsonUpletBuilder addAll(TsonElementBase[] element);

    TsonUpletBuilder add(TsonElementBase element);

    TsonUpletBuilder remove(TsonElementBase element);

    TsonUpletBuilder addAt(int index, TsonElementBase element);

    TsonUpletBuilder setAt(int index, TsonElementBase element);

    TsonUpletBuilder removeAt(int index);

    TsonElement[] params();

    TsonElement param(int index);

    int size();

    TsonUpletBuilder removeAll();


    /// /////////////////////////////////////////////

    TsonUpletBuilder comments(TsonComments comments);

    TsonUpletBuilder setComments(TsonComments comments);

    TsonUpletBuilder setAnnotations(TsonAnnotation[] annotations);

    TsonUpletBuilder addAnnotations(TsonAnnotation... annotations);

    TsonUpletBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonUpletBuilder annotation(String name, TsonElementBase... elements);

    TsonUpletBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonUpletBuilder addAnnotation(TsonAnnotation annotation);

    TsonUpletBuilder removeAnnotationAt(int index);

    TsonUpletBuilder clearAnnotations();

    TsonUpletBuilder ensureCapacity(int length);

    TsonUplet build();
}
