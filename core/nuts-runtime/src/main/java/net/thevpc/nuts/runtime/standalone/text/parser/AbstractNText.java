package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.runtime.standalone.text.NTextNodeWriterStringer;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.util.NBlankable;

import java.io.ByteArrayOutputStream;

public abstract class AbstractNText implements NText {

    public AbstractNText() {
    }

    @Override
    public String toString() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out);
        ss.writeNode(this);
        return out.toString();
    }

    @Override
    public String filteredText() {
        return immutable().filteredText();
    }

    @Override
    public int textLength() {
        return immutable().textLength();
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return NBlankable.isBlank(filteredText());
    }

    @Override
    public NTextBuilder builder() {
        return NTextBuilder.of().append(this);
    }

}
