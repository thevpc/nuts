//package net.thevpc.nuts.runtime.standalone.tson;
//
//import java.util.List;
//
//public interface TsonElementHeaderBuilder<T extends TsonElementBuilder> {
//
//    TsonElementHeaderBuilder<T> clear();
//
//    TsonElementHeaderBuilder<T> set(TsonElementHeader header);
//
//    TsonElementHeaderBuilder<T> name(String name);
//
//    TsonElementHeaderBuilder<T> name(String name);
//
//    TsonElementHeaderBuilder<T> addArgs(TsonElement... element);
//
//    TsonElementHeaderBuilder<T> addArgs(TsonElementBase... element);
//
//    TsonElementHeaderBuilder<T> addArgs(Iterable<? extends TsonElementBase> element);
//
//    TsonElementHeaderBuilder<T> addArg(TsonElementBase element);
//
//    TsonElementHeaderBuilder<T> remove(TsonElementBase element);
//
//    TsonElementHeaderBuilder<T> addArg(TsonElementBase element, int index);
//
//    TsonElementHeaderBuilder<T> removeAt(int index);
//
//    List<TsonElement> args();
//
//    int argsCount();
//
//    TsonElementHeaderBuilder<T> clearArgs();
//
//    boolean isParametrized() ;
//
//    TsonElementHeaderBuilder<T> setParametrized(boolean hasArgs);
//    T then();
//}
