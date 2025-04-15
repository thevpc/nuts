package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.List;

public interface TsonAnnotationBuilder {
    TsonAnnotationBuilder reset();

    TsonAnnotationBuilder merge(TsonElementBase element);

    String getName();

    String name();

    TsonAnnotationBuilder name(String name);


    TsonAnnotationBuilder addAll(TsonElement... element);

    TsonAnnotationBuilder addAll(TsonElementBase... element);

    TsonAnnotationBuilder addAll(Iterable<? extends TsonElementBase> element);

    TsonAnnotationBuilder setParametrized(boolean parametrized);

    boolean isParametrized();

    TsonAnnotationBuilder with(TsonElementBase... element);

    TsonAnnotationBuilder add(TsonElementBase element);


    TsonAnnotationBuilder remove(TsonElementBase element);

    TsonAnnotationBuilder add(TsonElementBase element, int index);

    TsonAnnotationBuilder removeAt(int index);

    List<TsonElement> all();

    List<TsonElement> params();

    TsonElement get(int index);

    TsonAnnotation build();

    TsonAnnotationBuilder merge(TsonAnnotation element);

    int size();

    TsonAnnotationBuilder ensureCapacity(int length);
}
