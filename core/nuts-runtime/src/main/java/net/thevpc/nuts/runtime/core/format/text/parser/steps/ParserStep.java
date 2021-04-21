package net.thevpc.nuts.runtime.core.format.text.parser.steps;

import net.thevpc.nuts.runtime.core.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.NutsText;

public abstract class ParserStep {

    public abstract void consume(char c, DefaultNutsTextNodeParser.State p);

    public abstract void appendChild(ParserStep tt);

    public abstract NutsText toNode();

    public abstract void end(DefaultNutsTextNodeParser.State p);

    public abstract boolean isComplete();

}
