//package net.thevpc.nuts.runtime.standalone.text.renderer;
//
//import net.thevpc.nuts.runtime.standalone.text.*;
//import net.thevpc.nuts.runtime.standalone.util.NutsCachedValue;
//
//import java.util.HashMap;
//import java.util.Map;
//
//import net.thevpc.nuts.NutsSession;
//
//import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyle;
//import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.AnsiStyleStyleApplier;
//import net.thevpc.nuts.runtime.standalone.text.renderer.ansi.DoNothingAnsiStyleStyleApplier;
//
//public class AnsiUnixTermPrintRenderer implements FormattedPrintStreamRenderer {
//
//    public static final FormattedPrintStreamRenderer ANSI_RENDERER = new AnsiUnixTermPrintRenderer();
//    private static NutsCachedValue<Integer> tput_cols;
//    private static AnsiStyleStyleApplierResolver applierResolver = new AnsiStyleStyleApplierResolver() {
//        private final Map<AnsiEscapeCommand, AnsiStyleStyleApplier> stylesAppliers = new HashMap<>();
//
//        public AnsiStyleStyleApplier resolveStyleApplier(AnsiEscapeCommand format) {
//            if (format == null) {
//                return DoNothingAnsiStyleStyleApplier.INSTANCE;
//            }
//            if (format instanceof AnsiStyleStyleApplier) {
//                return (AnsiStyleStyleApplier) format;
//            }
//            AnsiStyleStyleApplier s = stylesAppliers.get(format);
//            if (s != null) {
//                return s;
//            }
//            return DoNothingAnsiStyleStyleApplier.INSTANCE;
//        }
//    };
//
//
//    public AnsiStyle createStyleRenderer(AnsiEscapeCommand format, RenderedRawStream out, NutsSession session) {
//        AnsiStyleStyleApplier applier = applierResolver.resolveStyleApplier(format);
//        return applier.apply(AnsiStyle.PLAIN, out, session, applierResolver);
//    }
//
//    @Override
//    public void startFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsSession session) {
//        createStyleRenderer(format, out, session).startFormat(out, session);
//    }
//
//    @Override
//    public void endFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsSession session) {
//        createStyleRenderer(format, out, session).endFormat(out, session);
//    }
//}
