package net.thevpc.nuts.runtime.standalone.format.tson.bundled;

import java.util.Collection;
import java.util.List;

public interface TsonElementBuilder extends TsonElementBase{

    TsonElementBuilder comments(TsonComments comments);

    TsonElementBuilder setComments(TsonComments comments);

    TsonElementBuilder setAnnotations(TsonAnnotation... annotations);

    TsonElementBuilder addAnnotations(TsonAnnotation... annotations);
    
    TsonElementBuilder addAnnotations(Collection<TsonAnnotation> annotations);

    TsonElementBuilder anchor(String name);

    TsonElementBuilder annotation(String name, TsonElementBase... elements);

    TsonElementBuilder addAnnotation(String name, TsonElementBase... elements);

    TsonElementBuilder addAnnotation(TsonAnnotation annotation);

    TsonElementBuilder removeAnnotationAt(int index);

    TsonElementBuilder clearAnnotations();

    TsonElement build();

    TsonArrayBuilder toArray();

    TsonObjectBuilder toObject();

    TsonUpletBuilder toUplet();

    TsonElementType type();

    TsonComments comments();

    List<TsonAnnotation> annotations();

}
