package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NQuoteType;
import net.thevpc.nuts.util.NStringUtils;
import net.thevpc.nuts.runtime.standalone.text.DefaultNTexts;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.text.NText;

public class TitleParserStep extends ParserStep {

    StringBuilder start = new StringBuilder();
    List<ParserStep> children = new ArrayList<>();
    private NSession session;
    public TitleParserStep(String c, NSession session) {
        start.append(c);
        this.session = session;
    }

    @Override
    public void consume(char c, DefaultNTextNodeParser.State p, boolean wasNewLine) {
        if (c == ' ' && children.isEmpty()) {
            start.append(c);
        } else if (c == '\n' || c == '\r') {
            p.applyPopReplay(this, c);
        } else {
            p.applyPush(c, false, false, false);
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        children.add(tt);
    }

    @Override
    public NText toText() {
        String s = start.toString();
//        NutsTexts text = ws.text();
        DefaultNTexts factory0 = (DefaultNTexts) NTexts.of(session);
        String s0=s.trim();
        NText child=null;
        if (children.size() == 1) {
            child=children.get(0).toText();
        }else{
            List<NText> all = new ArrayList<>();
            for (ParserStep a : children) {
                all.add(a.toText());
            }
            child= NTexts.of(session).ofList(all).simplify();
        }
        return factory0.createTitle(s,s0.length()-1 ,child,isComplete());
    }

    @Override
    public void end(DefaultNTextNodeParser.State p) {
        p.applyPop(this);
    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Title(" + NStringUtils.formatStringLiteral(start.toString(), NQuoteType.DOUBLE));
        for (ParserStep parserStep : children) {
            sb.append(",");
            sb.append(parserStep.toString());
        }
        return sb.append(")").toString();
    }

}
