package net.thevpc.nuts.runtime.format.text.renderer;

import net.thevpc.nuts.runtime.format.text.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.thevpc.nuts.runtime.format.text.renderer.ansi.AnsiStyle;
import net.thevpc.nuts.runtime.format.text.renderer.ansi.AnsiStyleStyleApplier;
import net.thevpc.nuts.runtime.format.text.renderer.ansi.BackgroundStyleApplier;
import net.thevpc.nuts.runtime.format.text.renderer.ansi.DoNothingAnsiStyleStyleApplier;
import net.thevpc.nuts.runtime.format.text.renderer.ansi.ForegroundStyleApplier;
import net.thevpc.nuts.runtime.format.text.renderer.ansi.ListAnsiStyleStyleApplier;

public class AnsiUnixTermPrintRenderer implements FormattedPrintStreamRenderer {

    public static final FormattedPrintStreamRenderer ANSI_RENDERER = new AnsiUnixTermPrintRenderer();
    private final Map<TextFormat, AnsiStyleStyleApplier> stylesAppliers = new HashMap<>();

    {
        defineEscape(TextFormats.FG_BLACK, new ForegroundStyleApplier("30", 0));
        defineEscape(TextFormats.FG_RED, new ForegroundStyleApplier("31", 0));
        defineEscape(TextFormats.FG_GREEN, new ForegroundStyleApplier("32", 0));
        defineEscape(TextFormats.FG_YELLOW, new ForegroundStyleApplier("33", 0));
        defineEscape(TextFormats.FG_MAGENTA, new ForegroundStyleApplier("35", 0));
        defineEscape(TextFormats.FG_BLUE, new ForegroundStyleApplier("34", 0));
        defineEscape(TextFormats.FG_CYAN, new ForegroundStyleApplier("36", 0));
        defineEscape(TextFormats.FG_WHITE, new ForegroundStyleApplier("37", 0));
        defineEscape(TextFormats.FG_GREY, new ForegroundStyleApplier("37", 1));

        defineEscape(TextFormats.BG_BLACK, new BackgroundStyleApplier("40"));
        defineEscape(TextFormats.BG_RED, new BackgroundStyleApplier("41"));
        defineEscape(TextFormats.BG_GREEN, new BackgroundStyleApplier("42"));
        defineEscape(TextFormats.BG_YELLOW, new BackgroundStyleApplier("43"));
        defineEscape(TextFormats.BG_BLUE, new BackgroundStyleApplier("44"));
        defineEscape(TextFormats.BG_MAGENTA, new BackgroundStyleApplier("45"));
        defineEscape(TextFormats.BG_CYAN, new BackgroundStyleApplier("46"));
        defineEscape(TextFormats.BG_GREY, new BackgroundStyleApplier("100"));
        defineEscape(TextFormats.BG_WHITE, new BackgroundStyleApplier("37"));
        defineEscape(TextFormats.BOLD, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.setBold(true);
            }
        });
        defineEscape(TextFormats.UNDERLINED, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.setUnderlined(true);
            }
        });
        defineEscape(TextFormats.REVERSED, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.setReversed(true);
            }
        });
        defineEscape(TextFormats.MOVE_LINE_START, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.addCommand("\u001b[1000D");
            }
        });
        defineEscape(TextFormats.LATER_RESET_LINE, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                AnsiStyle e = old.addLaterCommand("\u001b[1000D"
                                + CoreStringUtils.fillString(' ', 80)
                        +"\u001b[1000D"
                );

                return e;
            }
        });
        defineEscape(TextFormats.MOVE_UP, new AnsiStyleStyleApplier() {
            @Override
            public AnsiStyle apply(AnsiStyle old) {
                return old.addCommand("\u001b[1A");
            }
        });

//        defineEscape(TextFormats.MOVE_LINE_START, "\u001B[1000D", "");
//        defineEscape(TextFormats.BOLD, "\u001b[1m", "\u001B[0m");
//        defineEscape(TextFormats.UNDERLINED, "\u001b[4m", "\u001B[0m");
//        defineEscape(TextFormats.REVERSED, "\u001b[7m", "\u001B[0m");
    }

    public AnsiStyle createStyleRenderer(TextFormat format) {
        AnsiStyleStyleApplier applier = resolveStyleApplyer(format);
        return applier.apply(AnsiStyle.PLAIN);
    }

    @Override
    public void startFormat(RenderedRawStream out, TextFormat format)  throws IOException {
        createStyleRenderer(format).startFormat(out);
    }

    @Override
    public void endFormat(RenderedRawStream out, TextFormat format) throws IOException {
        createStyleRenderer(format).endFormat(out);
    }

    private AnsiStyleStyleApplier createAnsiStyleStyleApplier(TextFormatList list) {
        List<AnsiStyleStyleApplier> suppliers = new ArrayList<AnsiStyleStyleApplier>();
        for (TextFormat item : list) {
            suppliers.add(resolveStyleApplyer(item));
        }
        return new ListAnsiStyleStyleApplier(suppliers);
    }

    private AnsiStyleStyleApplier resolveStyleApplyer(TextFormat format) {
        if(format==null){
            return DoNothingAnsiStyleStyleApplier.INSTANCE;
        }
        if (format instanceof TextFormatList) {
            return createAnsiStyleStyleApplier((TextFormatList) format);
        }
        AnsiStyleStyleApplier s = stylesAppliers.get(format);
        if (s != null) {
            return s;
        }
        return DoNothingAnsiStyleStyleApplier.INSTANCE;
    }

    private void defineEscape(TextFormat a, AnsiStyleStyleApplier style) {
        stylesAppliers.put(a, style);
    }

}
