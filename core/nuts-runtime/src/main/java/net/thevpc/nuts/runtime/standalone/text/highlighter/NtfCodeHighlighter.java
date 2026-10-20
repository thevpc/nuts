package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.runtime.standalone.text.parser.v2.NTFParser2;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.reflect.NScore;
import net.thevpc.nuts.reflect.NScorable;
import net.thevpc.nuts.reflect.NScorableContext;

import java.util.ArrayList;
import java.util.List;

public class NtfCodeHighlighter implements NCodeHighlighter {
    public NtfCodeHighlighter() {
    }

    @Override
    public String id() {
        return "ntf";
    }

    @NScore
    public static int getScore(NScorableContext context) {
        String s = context.criteria();
        if(s==null){
            return NScorable.DEFAULT_SCORE;
        }
        if(NConstants.Ntf.MIME_TYPES.contains(s)){
            return NScorable.DEFAULT_SCORE;
        }
        if(NConstants.Ntf.NAMES.contains(s)){
            return NScorable.DEFAULT_SCORE;
        }
        return NScorable.UNSUPPORTED_SCORE;
    }

    @Override
    public NText stringToText(String text) {
        if (text == null || text.isEmpty()) {
            return NText.ofPlain("");
        }
        NTFParser2 parser = new NTFParser2();
        parser.reset();
        parser.offer(text);
        parser.eof(true);
        List<NText> nodes = new ArrayList<>();
        parser.parseRemaining(nodes::add);
        return NText.ofList(nodes.toArray(new NText[0])).simplify();
    }

    @Override
    public NText tokenToText(String text, String tokenType) {
        return NText.of(text);
    }
}
