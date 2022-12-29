package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.text.NText;

public class DispatchAction extends ParserStep {
    private boolean spreadLines;
    private boolean lineStart;
    private boolean exitOnBrace;

    public DispatchAction(boolean spreadLines, boolean lineStart,boolean exitOnBrace) {
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
        this.exitOnBrace = exitOnBrace;
    }

    @Override
    public void consume(char c, DefaultNTextNodeParser.State p, boolean wasNewLine) {
        p.applyDrop(this);
        p.applyPush(c, spreadLines, lineStart, exitOnBrace);
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new IllegalArgumentException();
    }

    @Override
    public NText toText() {
        throw new IllegalArgumentException();
    }

    @Override
    public void end(DefaultNTextNodeParser.State p) {
        p.applyDrop(this);
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
