package net.thevpc.nuts.runtime.core.format.text.renderer;

import net.thevpc.nuts.NutsOsFamily;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.*;
import net.thevpc.nuts.runtime.core.util.CachedValue;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.AnsiStyle;
import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.AnsiStyleStyleApplier;
import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.DoNothingAnsiStyleStyleApplier;
import net.thevpc.nuts.runtime.core.format.text.renderer.ansi.ListAnsiStyleStyleApplier;

public class AnsiUnixTermPrintRenderer implements FormattedPrintStreamRenderer {

    public static final FormattedPrintStreamRenderer ANSI_RENDERER = new AnsiUnixTermPrintRenderer();
    private final Map<AnsiEscapeCommand, AnsiStyleStyleApplier> stylesAppliers = new HashMap<>();
    private static CachedValue<Integer> tput_cols;


    {
//        defineEscape(AnsiEscapeCommands.FG_BLACK, new ForegroundStyleApplier("30", 0));
//        defineEscape(AnsiEscapeCommands.FG_RED, new ForegroundStyleApplier("31", 0));
//        defineEscape(AnsiEscapeCommands.FG_GREEN, new ForegroundStyleApplier("32", 0));
//        defineEscape(AnsiEscapeCommands.FG_YELLOW, new ForegroundStyleApplier("33", 0));
//        defineEscape(AnsiEscapeCommands.FG_MAGENTA, new ForegroundStyleApplier("35", 0));
//        defineEscape(AnsiEscapeCommands.FG_BLUE, new ForegroundStyleApplier("34", 0));
//        defineEscape(AnsiEscapeCommands.FG_CYAN, new ForegroundStyleApplier("36", 0));
//        defineEscape(AnsiEscapeCommands.FG_WHITE, new ForegroundStyleApplier("37", 0));
//        defineEscape(AnsiEscapeCommands.FG_GREY, new ForegroundStyleApplier("37", 1));
//
//        defineEscape(AnsiEscapeCommands.BG_BLACK, new BackgroundStyleApplier("40"));
//        defineEscape(AnsiEscapeCommands.BG_RED, new BackgroundStyleApplier("41"));
//        defineEscape(AnsiEscapeCommands.BG_GREEN, new BackgroundStyleApplier("42"));
//        defineEscape(AnsiEscapeCommands.BG_YELLOW, new BackgroundStyleApplier("43"));
//        defineEscape(AnsiEscapeCommands.BG_BLUE, new BackgroundStyleApplier("44"));
//        defineEscape(AnsiEscapeCommands.BG_MAGENTA, new BackgroundStyleApplier("45"));
//        defineEscape(AnsiEscapeCommands.BG_CYAN, new BackgroundStyleApplier("46"));
//        defineEscape(AnsiEscapeCommands.BG_GREY, new BackgroundStyleApplier("100"));
//        defineEscape(AnsiEscapeCommands.BG_WHITE, new BackgroundStyleApplier("37"));


        defineEscape(AnsiEscapeCommands.MOVE_LINE_START, new MoveLineStartCommandAnsiStyleStyleApplier());
        defineEscape(AnsiEscapeCommands.LATER_RESET_LINE, new LaterResetLineCommandAnsiStyleStyleApplier());
        defineEscape(AnsiEscapeCommands.MOVE_UP, new MoveUpCommandAnsiStyleStyleApplier());

//        defineEscape(AnsiEscapeCommands.MOVE_LINE_START, "\u001B[1000D", "");
//        defineEscape(AnsiEscapeCommands.BOLD, "\u001b[1m", "\u001B[0m");
//        defineEscape(AnsiEscapeCommands.UNDERLINED, "\u001b[4m", "\u001B[0m");
//        defineEscape(AnsiEscapeCommands.REVERSED, "\u001b[7m", "\u001B[0m");
    }

    public AnsiStyle createStyleRenderer(AnsiEscapeCommand format, RenderedRawStream out, NutsWorkspace ws) {
        AnsiStyleStyleApplier applier = resolveStyleApplyer(format);
        return applier.apply(AnsiStyle.PLAIN, out, ws);
    }

    @Override
    public void startFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsWorkspace ws)  throws IOException {
        createStyleRenderer(format, out, ws).startFormat(out);
    }

    @Override
    public void endFormat(RenderedRawStream out, AnsiEscapeCommand format, NutsWorkspace ws) throws IOException {
        createStyleRenderer(format, out, ws).endFormat(out);
    }

    private AnsiStyleStyleApplier createAnsiStyleStyleApplier(AnsiEscapeCommandList list) {
        List<AnsiStyleStyleApplier> suppliers = new ArrayList<AnsiStyleStyleApplier>();
        for (AnsiEscapeCommand item : list) {
            suppliers.add(resolveStyleApplyer(item));
        }
        return new ListAnsiStyleStyleApplier(suppliers);
    }

    private AnsiStyleStyleApplier resolveStyleApplyer(AnsiEscapeCommand format) {
        if(format==null){
            return DoNothingAnsiStyleStyleApplier.INSTANCE;
        }
        if(format instanceof AnsiStyleStyleApplier){
            return (AnsiStyleStyleApplier) format;
        }
        if (format instanceof AnsiEscapeCommandList) {
            return createAnsiStyleStyleApplier((AnsiEscapeCommandList) format);
        }
        AnsiStyleStyleApplier s = stylesAppliers.get(format);
        if (s != null) {
            return s;
        }
        return DoNothingAnsiStyleStyleApplier.INSTANCE;
    }

    private void defineEscape(AnsiEscapeCommand a, AnsiStyleStyleApplier style) {
        stylesAppliers.put(a, style);
    }

    private static class MoveUpCommandAnsiStyleStyleApplier implements AnsiStyleStyleApplier {
        @Override
        public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsWorkspace ws) {
            return old.addCommand("\u001b[1A");
        }
    }

    private static class LaterResetLineCommandAnsiStyleStyleApplier implements AnsiStyleStyleApplier {
        public LaterResetLineCommandAnsiStyleStyleApplier() {
        }

        @Override
        public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsWorkspace ws) {
            int tputCallTimeout = ws.env().getPropertyAsInt("nuts.term.tput.call.timeout",true,60);
            Integer w = ws.env().getPropertyAsInt("nuts.term.width",true,null);
            if(w==null){
                if(tput_cols==null) {
                    tput_cols=new CachedValue<>(
                            new Supplier<Integer>() {
                                boolean wasError=false;
                                @Override
                                public Integer get() {
                                    switch (ws.env().getOsFamily()) {
                                        case LINUX:
                                        case UNIX:
                                        case MACOS: {
                                            try {
                                                String d = ws.exec().userCmd().grabOutputString()
                                                        .setSession(ws.createSession())
                                                        .addCommand("tput", "cols")
                                                        .getOutputString();String s=d.trim();if(d.isEmpty()){return null;};
                                                return Integer.parseInt(d.trim());
                                            } catch (Exception ex) {
                                                wasError=true;
                                                return null;
                                            }
                                        }
                                    }
                                    return null;
                                }
                            },tputCallTimeout
                    );
                }
                if (out.baseOutput() == System.out) {
                    w=tput_cols.getValue();
                }
            }
            if(w==null){
                w=120;
            }
            if(w>0) {
//                return old.addLaterCommand("\u001b[1000D"
//                        + CoreStringUtils.fillString(' ', w)
//                        + "\u001b[1000D"
//                );
                return old.addLaterCommand("\r"
                        + CoreStringUtils.fillString(' ', w)
                        +"\r"
                );
            }
            return old;
        }
    }

    private static class MoveLineStartCommandAnsiStyleStyleApplier implements AnsiStyleStyleApplier {
        @Override
        public AnsiStyle apply(AnsiStyle old, RenderedRawStream out, NutsWorkspace ws) {
//            return old.addCommand("\u001b[1000D");
            return old.addCommand("\r");
        }
    }
}
