package net.thevpc.nuts.runtime.format.text.parser.steps;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.format.text.parser.*;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.format.text.TextFormat;

import java.util.ArrayList;
import java.util.List;

public class TitleParserStep extends ParserStep {

    StringBuilder start = new StringBuilder();
    List<ParserStep> children = new ArrayList<>();
    private NutsWorkspace ws;
    public TitleParserStep(String c,NutsWorkspace ws) {
        start.append(c);
        this.ws=ws;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p) {
        if (c == ' ' && children.isEmpty()) {
            start.append(c);
        } else if (c == '\n' || c == '\r') {
            p.applyPopReject(c);
        } else {
            p.applyStart(c, true, false);
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        children.add(tt);
    }

    @Override
    public NutsTextNode toNode() {
        String s = start.toString();
        TextFormat style = DefaultNutsTextNodeParser.createStyle(start.substring(0, start.length() - 1));
        if (children.size() == 1) {
            return new DefaultNutsTextNodeTitle(s, style,children.get(0).toNode());
        }
        List<NutsTextNode> all = new ArrayList<>();
        boolean partial = false;
        for (ParserStep a : children) {
            if (!partial && !a.isComplete()) {
                partial = true;
            }
            all.add(a.toNode());
        }
        return new DefaultNutsTextNodeTitle(s,style, ws.formats().text().factory().list(all));
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyPop();
    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Title(" + CoreStringUtils.dblQuote(start.toString()));
        for (ParserStep parserStep : children) {
            sb.append(",");
            sb.append(parserStep.toString());
        }
        return sb.append(")").toString();
    }

}
