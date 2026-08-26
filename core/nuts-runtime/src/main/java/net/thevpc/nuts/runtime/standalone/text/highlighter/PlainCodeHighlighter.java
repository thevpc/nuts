package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;
import net.thevpc.nuts.text.NText;

public class PlainCodeHighlighter implements NCodeHighlighter {


    public PlainCodeHighlighter() {
    }

    @Override
    public String id() {
        return "plain";
    }

    @Override
    public NText tokenToText(String text, String nodeType) {
        return NText.ofPlain(text);
    }

    @Override
    public NText stringToText(String text) {
        return NText.ofPlain(text);
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if(s==null){
            return NScorable.DEFAULT_SCORE;
        }
        switch (s){
            case "plain":
            case "text":
            case "txt":
            case "text/plain":
            {
                return NScorable.DEFAULT_SCORE;
            }
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

}
