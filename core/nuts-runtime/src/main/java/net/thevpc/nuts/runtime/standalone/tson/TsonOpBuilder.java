package net.thevpc.nuts.runtime.standalone.tson;

import java.util.Collection;

public interface TsonOpBuilder extends TsonElementBuilder {

    String opName();

    TsonOpType opType();

    TsonOpBuilder opName(String opName);

    TsonOpBuilder opType(TsonOpType opType);

    TsonOpBuilder merge(TsonOp other);

    TsonOpBuilder reset();

    TsonElement first();

    TsonOpBuilder first(TsonElementBase key);

    TsonElement second();

    TsonOpBuilder second(TsonElementBase value);

    /// /////////////////////////////////////////////

    TsonOpBuilder comments(TsonComments comments);

    TsonOpBuilder setComments(TsonComments comments);

    TsonOpBuilder setAnnotations(TsonAnnotation... annotations);

    TsonOpBuilder addAnnotations(TsonAnnotation... annotations);

    TsonOpBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonOpBuilder annotation(String name, TsonElementBase... elements);

    TsonOpBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonOpBuilder addAnnotation(TsonAnnotation annotation);

    TsonOpBuilder removeAnnotationAt(int index);

    TsonOpBuilder clearAnnotations();

    TsonOp build();
}
