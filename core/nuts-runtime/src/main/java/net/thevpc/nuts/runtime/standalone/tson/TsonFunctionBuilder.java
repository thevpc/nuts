//package net.thevpc.nuts.runtime.standalone.tson;
//
//import java.util.Collection;
//import java.util.List;
//
//public interface TsonFunctionBuilder extends TsonElementBuilder {
//    TsonFunctionBuilder reset();
//
//    TsonFunctionBuilder merge(TsonElementBase element);
//
//    TsonFunctionBuilder name(String name);
//
//    TsonFunctionBuilder setName(String name);
//
//    TsonFunctionBuilder addAll(TsonElement... element);
//
//    TsonFunctionBuilder addAll(TsonElementBase... element);
//
//    TsonFunctionBuilder addAll(Iterable<? extends TsonElementBase> element);
//
//    TsonFunctionBuilder add(TsonElementBase element);
//
//    TsonFunctionBuilder remove(TsonElementBase element);
//
//    TsonFunctionBuilder add(TsonElementBase element, int index);
//
//    TsonFunctionBuilder removeAt(int index);
//
//    List<TsonElement> args();
//
//    TsonFunctionBuilder removeAllParams();
//
//    ////////////////////////////////////////////////
//
//    TsonFunctionBuilder comments(TsonComments comments);
//
//    TsonFunctionBuilder setComments(TsonComments comments);
//
//    TsonFunctionBuilder setAnnotations(TsonAnnotation... annotations);
//
//    TsonFunctionBuilder addAnnotations(TsonAnnotation... annotations);
//
//    TsonFunctionBuilder addAnnotations(Collection<TsonAnnotation> annotations);
//
//    TsonFunctionBuilder annotation(String name, TsonElementBase... elements);
//
//    TsonFunctionBuilder addAnnotation(String name, TsonElementBase... elements);
//
//    TsonFunctionBuilder addAnnotation(TsonAnnotation annotation);
//
//    TsonFunctionBuilder removeAnnotationAt(int index);
//
//    TsonFunctionBuilder removeAllAnnotations();
//
//    TsonFunctionBuilder ensureCapacity(int length);
//}
