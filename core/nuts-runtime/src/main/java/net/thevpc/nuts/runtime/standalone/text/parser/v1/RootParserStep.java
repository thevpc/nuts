package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.text.NTextPlain;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.runtime.standalone.text.parser.DefaultNTextPlain;

public class RootParserStep extends ParserStep {
    boolean spreadLines;
    LinkedList<ParserStep> available = new LinkedList<>();
    private NWorkspace workspace;
    public RootParserStep(boolean spreadLines, NWorkspace workspace) {
        this.spreadLines = spreadLines;
        this.workspace = workspace;
    }

    @Override
    public void consume(char c, DefaultNTextNodeParser.State p, boolean wasNewLine) {
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
    public NText toText() {
        if (available.size() == 1) {
            return available.get(0).toText();
        }
        List<NText> all = new ArrayList<>();
        boolean partial = false;
        for (ParserStep a : available) {
            if (!partial && !a.isComplete()) {
                partial = true;
            }
            NText n = a.toText();
            if(n instanceof DefaultNTextPlain
                    && !all.isEmpty()
                    && all.get(all.size()-1) instanceof DefaultNTextPlain) {
                //consecutive plain text
                NTextPlain p1=(NTextPlain) n;
                NTextPlain p2=(NTextPlain) all.remove(all.size()-1);
                all.add(new DefaultNTextPlain(
                        p1.getText()+p2.getText()
                ));
            }else{
                all.add(n);
            }
        }
        return NText.ofList(all).simplify();
    }

    @Override
    public void end(DefaultNTextNodeParser.State p) {

    }

    public boolean isComplete() {
        return true;
    }

    @Override
    public String toString() {
        return "Root(" + available + ')';
    }
}
