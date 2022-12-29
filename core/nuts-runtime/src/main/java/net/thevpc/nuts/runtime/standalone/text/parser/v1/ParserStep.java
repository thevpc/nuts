package net.thevpc.nuts.runtime.standalone.text.parser.v1;

import net.thevpc.nuts.text.NText;

public abstract class ParserStep {

    public abstract void consume(char c, DefaultNTextNodeParser.State p, boolean wasNewLine);

    public abstract void appendChild(ParserStep tt);

    public abstract NText toText();

    public abstract void end(DefaultNTextNodeParser.State p);

    public abstract boolean isComplete();

}
