//package net.thevpc.nuts.runtime.standalone.util.stream;
//
//import net.thevpc.nuts.elem.NElement;
//import net.thevpc.nuts.util.NIterator;
//import net.thevpc.nuts.util.NStream;
//
//import java.util.List;
//import java.util.function.Supplier;
//
//public class NStreamWithDescription<T> extends NStreamDelegate<T> {
//    private NStreamBase<T> base;
//
//    public NStreamWithDescription(NStreamBase<T> base, Supplier<NElement> description, List<Runnable> closeRunnables) {
//        super(description,closeRunnables);
//        this.base = base;
//    }
//
//    @Override
//    public NStream<T> baseStream() {
//        return base;
//    }
//
//
//    @Override
//    public NIterator<T> iterator() {
//        return super.iterator().withDescription(description);
//    }
//
//}
