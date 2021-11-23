//package net.thevpc.nuts.runtime.standalone.util.nfo;
//
//import net.thevpc.nuts.*;
//
//import java.util.function.Function;
//
//public class NamedFunction<F, T> implements NutsFunction<F, T> {
//    private final Function<F, T> converter;
//    private final Function<NutsElements,NutsElement> nfo;
//
//    public NamedFunction(Function<F, T> converter, Function<NutsElements,NutsElement> nfo) {
//        this.converter = converter;
//        this.nfo = nfo;
//    }
//
//    @Override
//    public T apply(F f) {
//        return converter.apply(f);
//    }
//
//    @Override
//    public String toString() {
//        return "NamedFunction{" +
//                "converter=" + converter +
//                ", nfo=" + nfo +
//                '}';
//    }
//
//    @Override
//    public NutsElement describe(NutsElements elems) {
//        NutsObjectElement b = NutsDescribables.resolveOrDestruct(converter, elems)
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
