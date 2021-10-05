package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextPlain;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.thevpc.nuts.NutsText;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextPlain;

public class RootParserStep extends ParserStep {
    boolean spreadLines;
    LinkedList<ParserStep> available = new LinkedList<>();
    private NutsSession session;
    public RootParserStep(boolean spreadLines, NutsSession session) {
        this.spreadLines = spreadLines;
        this.session = session;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p, boolean wasNewLine) {
        p.applyPush(c, spreadLines, wasNewLine, false);
    }

    public ParserStep pop() {
        return available.pop();
    }
    public ParserStep poll() {
        return available.poll();
    }
    public ParserStep peek() {
        return available.peek();
    }
    public boolean isEmpty() {
        return available.isEmpty();
    }
    public int size() {
        return available.size();
    }

    public void appendChild(ParserStep n) {
        available.add(n);
    }

    @Override
    public NutsText toText() {
        if (available.size() == 1) {
            return available.get(0).toText();
        }
        List<NutsText> all = new ArrayList<>();
        boolean partial = false;
        for (ParserStep a : available) {
            if (!partial && !a.isComplete()) {
                partial = true;
            }
            NutsText n = a.toText();
            if(n instanceof DefaultNutsTextPlain
                    && !all.isEmpty()
                    && all.get(all.size()-1) instanceof  DefaultNutsTextPlain) {
                //consecutive plain text
                NutsTextPlain p1=(NutsTextPlain) n;
                NutsTextPlain p2=(NutsTextPlain) all.remove(all.size()-1);
                all.add(new DefaultNutsTextPlain(
                        session,p1.getText()+p2.getText()
                ));
            }else{
                all.add(n);
            }
        }
        return session.text().ofList(all).simplify();
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {

    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        return "Root(" + available + ')';
    }
}
