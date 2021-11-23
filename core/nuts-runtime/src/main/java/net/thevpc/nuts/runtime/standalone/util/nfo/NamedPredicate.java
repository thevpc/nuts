//package net.thevpc.nuts.runtime.standalone.util.nfo;
//
//import net.thevpc.nuts.*;
//
//import java.util.function.Function;
//import java.util.function.Predicate;
//
//public class NamedPredicate< T> implements NutsPredicate<T> {
//    private final Predicate< T> expr;
//    private final Function<NutsElements,NutsElement> nfo;
//
//    public NamedPredicate(Predicate< T> expr, Function<NutsElements,NutsElement> nfo) {
//        this.expr = expr;
//        this.nfo = nfo;
//    }
//
//    @Override
//    public boolean test(T t) {
//        return expr.test(t);
//    }
//
//    @Override
//    public String toString() {
//        return "NamedPredicate";
//    }
//
//    @Override
//    public NutsElement describe(NutsElements elems) {
//        NutsObjectElement b = NutsDescribables.resolveOr(expr, elems,()->elems.ofObject().build())
//                .toObject();
//        NutsElement a = nfo.apply(elems);
//        if(b.isEmpty()){
//            return a;
//        }
//        if(a.isObject()){
//            return b.builder()
//                    .addAll(a.asObject())
//                    .build()
//                    ;
//        }else {
//            return b.builder()
//                    .set("name", a)
//                    .build()
//                    ;
//        }
//    }
//}
