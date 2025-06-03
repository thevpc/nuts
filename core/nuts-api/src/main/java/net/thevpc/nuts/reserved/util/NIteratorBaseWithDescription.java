//package net.thevpc.nuts.reserved.util;
//
//import net.thevpc.nuts.elem.*;
//import net.thevpc.nuts.spi.base.NIteratorBase;
//import net.thevpc.nuts.util.NIterator;
//
//public class NIteratorBaseWithDescription<T> extends NIteratorBase<T> {
//    private final NIterator<T> base;
//    private NEDesc description;
//
//    public NIteratorBaseWithDescription(NIterator<T> base, NEDesc description) {
//        this.base = base;
//        this.description = description;
//    }
//
//    @Override
//    public NIterator<T> withDesc(NEDesc description) {
//        this.description = description;
//        return this;
//    }
//
//    @Override
//    public boolean hasNext() {
//        return base.hasNext();
//    }
//
//    @Override
//    public T next() {
//        return base.next();
//    }
//
//    @Override
//    public String toString() {
//        if (description != null) {
//            try {
//                NElement e = description.get();
//                if (e != null) {
//                    return e.toString();
//                }
//            } catch (Exception e) {
//                //
//            }
//        }
//        return "NamedIterator";
//    }
//
//    @Override
//    public NElement describe() {
//        return NEDesc.safeDescribeOfBase(description, base);
////        NObjectElement b = NDescribables.resolveOr(base, session, () -> NElements.ofObject().build())
////                .asObject().get(session);
////        NElement a = description.apply(session);
////        if (b.isEmpty()) {
////            return a;
////        }
////        if (a.isObject()) {
////            return b.builder()
////                    .addAll(a.asObject().get(session))
////                    .build()
////                    ;
////        } else {
////            return b.builder()
////                    .set("name", a)
////                    .build()
////                    ;
////        }
//    }
//}
