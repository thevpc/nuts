//package net.thevpc.nuts.runtime.standalone.util.nfo;
//
//import net.thevpc.nuts.NutsDescribable;
//import net.thevpc.nuts.NutsRunnable;
//import net.thevpc.nuts.NutsElement;
//import net.thevpc.nuts.NutsElements;
//
//import java.util.function.Function;
//
//class NamedRunnable implements NutsRunnable {
//    private final Runnable base;
//    private final Function<NutsElements, NutsElement> nfo;
//
//    public NamedRunnable(Runnable base, Function<NutsElements, NutsElement> nfo) {
//        this.base = base;
//        this.nfo = nfo;
//    }
//
//    @Override
//    public NutsElement describe(NutsElements elems) {
//        return nfo.apply(elems);
//    }
//
//    @Override
//    public void run() {
//        base.run();
//    }
//}
