//package net.thevpc.nuts.spi.base;
//
//import net.thevpc.nuts.elem.*;
//import net.thevpc.nuts.reserved.util.NIteratorBaseWithDescription;
//import net.thevpc.nuts.util.NIterator;
//
//public abstract class NIteratorBase<T> implements NIterator<T> {
//    public NIteratorBase() {
//    }
//
//    @Override
//    public NIterator<T> withDesc(NEDesc description) {
//        if(description==null){
//            return this;
//        }
//        return new NIteratorBaseWithDescription<>(this,description);
//    }
//
//}
