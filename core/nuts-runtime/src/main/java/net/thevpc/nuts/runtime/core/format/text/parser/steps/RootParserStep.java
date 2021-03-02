package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.NutsTextNode;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class RootParserStep extends ParserStep {
    boolean spreadLines;
    LinkedList<ParserStep> available = new LinkedList<>();
    private NutsWorkspace ws;
    public RootParserStep(boolean spreadLines,NutsWorkspace ws) {
        this.spreadLines = spreadLines;
        this.ws = ws;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p) {
        boolean lineStart = available.isEmpty();
        p.applyStart(c, spreadLines, lineStart);
    }

    public ParserStep pop() {
        return available.pop();
    }
    public ParserStep poll() {
        return available.poll();
    }
    public ParserStep peek() {
        return available.pop();
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
    public NutsTextNode toNode() {
        if (available.size() == 1) {
            return available.get(0).toNode();
        }
        List<NutsTextNode> all = new ArrayList<>();
        boolean partial = false;
        for (ParserStep a : available) {
            if (!partial && !a.isComplete()) {
                partial = true;
            }
            all.add(a.toNode());
        }
        return ws.formats().text().list(all);
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
