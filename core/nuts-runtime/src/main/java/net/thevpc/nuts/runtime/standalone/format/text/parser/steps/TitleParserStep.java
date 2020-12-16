package net.thevpc.nuts.runtime.standalone.format.text.parser.steps;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.standalone.format.text.DefaultNutsTextNodeFactory;
import net.thevpc.nuts.runtime.standalone.format.text.parser.*;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.format.text.TextFormat;

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
            p.applyStart(c, false, false);
        }
    }

    @Override
    public void appendChild(ParserStep tt) {
        children.add(tt);
    }

    @Override
    public NutsTextNode toNode() {
        String s = start.toString();
//        NutsTextNodeFactory factory = ws.formats().text().factory();
        DefaultNutsTextNodeFactory factory0 = (DefaultNutsTextNodeFactory) ws.formats().text().factory();
        String s0=s.trim();
        TextFormat style = factory0.createStyle(s0.substring(0,s0.length() - 1));
        if (children.size() == 1) {
            return factory0.createTitle(s, children.get(0).toNode(), style);
        }
        List<NutsTextNode> all = new ArrayList<>();
        for (ParserStep a : children) {
            all.add(a.toNode());
        }
        return factory0.createTitle(s, ws.formats().text().factory().list(all), style);
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
