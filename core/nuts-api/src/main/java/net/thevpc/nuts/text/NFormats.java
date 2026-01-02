//package net.thevpc.nuts.text;
//
//import net.thevpc.nuts.ext.NExtensions;
//import net.thevpc.nuts.spi.NComponent;
//import net.thevpc.nuts.util.NOptional;
//
//public interface NFormats extends NComponent {
//    static NOptional<NObjectWriter> of(Object any) {
//        return of().ofFormat(any);
//    }
//
//    static NFormats of() {
//        return NExtensions.of(NFormats.class);
//    }
//
//    NOptional<NObjectWriter>  ofFormat(Object t);
//
//}
