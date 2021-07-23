package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.thevpc.nuts.NutsText;

public class RootParserStep extends ParserStep {
    boolean spreadLines;
    LinkedList<ParserStep> available = new LinkedList<>();
    private NutsWorkspace ws;
    public RootParserStep(boolean spreadLines,NutsWorkspace ws) {
        this.spreadLines = spreadLines;
        this.ws = ws;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p, boolean wasNewLine) {
        p.applyStart(c, spreadLines, wasNewLine);
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

    public void appendChild(ParserStep tt) {
        available.add(tt);
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
            all.add(a.toText());
        }
        return ws.text().forList(all).simplify();
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
