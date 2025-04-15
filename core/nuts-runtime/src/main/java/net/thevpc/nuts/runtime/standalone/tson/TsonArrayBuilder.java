package net.thevpc.nuts.runtime.standalone.tson;

import java.util.Collection;
import java.util.List;

public interface TsonArrayBuilder extends Iterable<TsonElement>, TsonElementBuilder {
    TsonArrayBuilder reset();

    TsonArrayBuilder merge(TsonElementBase element);

    TsonArrayBuilder addAll(TsonElement... element);

    TsonArrayBuilder addAll(TsonElementBase... element);

    TsonArrayBuilder addAll(Iterable<? extends TsonElementBase> element);

    TsonArrayBuilder add(TsonElementBase element);

    TsonArrayBuilder remove(TsonElementBase element);

    TsonArrayBuilder add(TsonElementBase element, int index);

    TsonArrayBuilder removeAt(int index);

    TsonArray build();

    List<TsonElement> all();

    List<TsonElement> getAll();

    TsonArrayBuilder removeAll();

    ////////////////////////////////////////////////

    TsonArrayBuilder comments(TsonComments comments);

    TsonArrayBuilder setComments(TsonComments comments);

    TsonArrayBuilder setAnnotations(TsonAnnotation... annotations);

    TsonArrayBuilder addAnnotations(TsonAnnotation... annotations);

    TsonArrayBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonArrayBuilder annotation(String name, TsonElementBase... elements);

    TsonArrayBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonArrayBuilder addAnnotation(TsonAnnotation annotation);

    TsonArrayBuilder removeAnnotationAt(int index);

    TsonArrayBuilder clearAnnotations();

    TsonArrayBuilder ensureCapacity(int length);

    boolean isParametrized();

    TsonArrayBuilder setParametrized(boolean parametrized);

    List<TsonElement> params();

    int paramsCount();

    TsonArrayBuilder clearParams();

    String name();

    TsonArrayBuilder name(String name);

    TsonArrayBuilder addParam(TsonElementBase element);

    TsonArrayBuilder removeParam(TsonElementBase element);

    TsonArrayBuilder addParam(TsonElementBase element, int index);

    TsonArrayBuilder removeParamAt(int index);

    TsonArrayBuilder addParams(TsonElement[] element);

    TsonArrayBuilder addParams(TsonElementBase[] element);

    TsonArrayBuilder addParams(Iterable<? extends TsonElementBase> element);

    List<TsonElement> body();

    TsonArrayBuilder clearBody();
}
