package net.thevpc.nuts.runtime.standalone.tson;

import java.util.Collection;
import java.util.List;

public interface TsonObjectBuilder extends Iterable<TsonElement>, TsonElementBuilder {
    TsonObjectBuilder clear();

    List<TsonElement> body();

    TsonObjectBuilder clearBody();

    TsonElementBaseListBuilder content();

    boolean iParametrized();

    TsonObjectBuilder setParametrized(boolean parametrized);

    List<TsonElement> params();

    int paramsCount();

    TsonObjectBuilder clearParams();

    String name();

    TsonObjectBuilder name(String name);

    TsonObjectBuilder addParam(TsonElementBase element);

    TsonObjectBuilder removeParam(TsonElementBase element);

    TsonObjectBuilder addParam(TsonElementBase element, int index);

    TsonObjectBuilder removeParamAt(int index);

    TsonObjectBuilder addParams(TsonElement[] element);

    TsonObjectBuilder addParams(TsonElementBase[] element);

    TsonObjectBuilder addParams(Iterable<? extends TsonElementBase> element);

    TsonObjectBuilder merge(TsonElementBase element);

    TsonObjectBuilder addAll(TsonElementBase... element);

    TsonObjectBuilder addAll(TsonElement... element);

    TsonObjectBuilder addAll(Iterable<? extends TsonElementBase> element);


    TsonObjectBuilder add(TsonElementBase key, TsonElementBase value);

    TsonObjectBuilder add(String key, TsonElementBase value);

    TsonObjectBuilder add(String key, String value);

    TsonObjectBuilder add(String key, int value);

    TsonObjectBuilder add(String key, long value);

    TsonObjectBuilder add(String key, float value);

    TsonObjectBuilder add(String key, double value);

    TsonObjectBuilder add(String key, byte value);

    TsonObjectBuilder add(String key, short value);

    TsonObjectBuilder add(String key, char value);

    TsonObjectBuilder add(String key, boolean value);

    TsonObjectBuilder add(String key, Enum value);

    TsonObjectBuilder add(TsonElementBase element);


    TsonObjectBuilder addAt(int index, TsonElementBase element);

    TsonObjectBuilder set(TsonElementBase key, TsonElementBase value);

    TsonObjectBuilder set(String key, TsonElementBase value);

    TsonObjectBuilder set(String key, String value);

    TsonObjectBuilder set(String key, int value);

    TsonObjectBuilder set(String key, long value);

    TsonObjectBuilder set(String key, float value);

    TsonObjectBuilder set(String key, double value);

    TsonObjectBuilder set(String key, byte value);

    TsonObjectBuilder set(String key, short value);

    TsonObjectBuilder set(String key, char value);

    TsonObjectBuilder set(String key, boolean value);

    TsonObjectBuilder set(String key, Enum value);

    TsonObjectBuilder setAt(int index, TsonElementBase element);

    TsonObjectBuilder remove(TsonElementBase element);

    TsonObjectBuilder removeAt(int index);

    List<TsonElement> all();


    /// /////////////////////////////////////////////

    TsonObjectBuilder comments(TsonComments comments);

    TsonObjectBuilder setComments(TsonComments comments);

    TsonObjectBuilder setAnnotations(TsonAnnotation... annotations);

    TsonObjectBuilder addAnnotations(TsonAnnotation... annotations);

    TsonObjectBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonObjectBuilder annotation(String name, TsonElementBase... elements);

    TsonObjectBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonObjectBuilder addAnnotation(TsonAnnotation annotation);

    TsonObjectBuilder removeAnnotationAt(int index);

    TsonObjectBuilder clearAnnotations();

    TsonObjectBuilder remove(String name);

    TsonObjectBuilder ensureCapacity(int length);

    TsonObject build();
}
