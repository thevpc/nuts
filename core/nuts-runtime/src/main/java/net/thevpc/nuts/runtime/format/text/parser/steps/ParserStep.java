package net.thevpc.nuts.runtime.format.text.parser.steps;

import net.thevpc.nuts.runtime.format.text.parser.DefaultNutsTextNodeParser;
import net.thevpc.nuts.NutsTextNode;

public abstract class ParserStep {

    public abstract void consume(char c, DefaultNutsTextNodeParser.State p);

    public abstract void appendChild(ParserStep tt);

    public abstract NutsTextNode toNode();

    public abstract void end(DefaultNutsTextNodeParser.State p);

    public abstract boolean isComplete();

}
