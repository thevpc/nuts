package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.runtime.standalone.text.parser.v2.MTFParser;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;

public class MtfCodeHighlighter implements NCodeHighlighter {

    public MtfCodeHighlighter() {
    }

    @Override
    public String id() {
        return "mtf";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if(s==null){
            return NScorable.DEFAULT_SCORE;
        }
        if(NConstants.Mtf.MIME_TYPES.contains(s)){
            return NScorable.DEFAULT_SCORE;
        }
        if(NConstants.Mtf.NAMES.contains(s)){
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText stringToText(String text) {
        if (text == null) {
            return NText.ofPlain("");
        }
        MTFParser parser = new MTFParser();
        parser.reset();
        parser.offer(text);
        parser.eof(true);
        return parser.readFully();
    }

    @Override
    public NText tokenToText(String text, String tokenType) {
        return NText.ofPlain(text == null ? "" : text);
    }
}
