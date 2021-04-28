package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.NutsText;

public class DispatchAction extends ParserStep {
    private boolean spreadLines;
    private boolean lineStart;

    public DispatchAction(boolean spreadLines, boolean lineStart) {
        this.spreadLines = spreadLines;
        this.lineStart = lineStart;
    }

    @Override
    public void consume(char c, DefaultNutsTextNodeParser.State p) {
        p.applyDrop();
        p.applyStart(c, spreadLines, lineStart);
    }

    @Override
    public void appendChild(ParserStep tt) {
        throw new IllegalArgumentException();
    }

    @Override
    public NutsText toText() {
        throw new IllegalArgumentException();
    }

    @Override
    public void end(DefaultNutsTextNodeParser.State p) {
        p.applyDrop();
    }

    @Override
    public boolean isComplete() {
        return false;
    }
}
