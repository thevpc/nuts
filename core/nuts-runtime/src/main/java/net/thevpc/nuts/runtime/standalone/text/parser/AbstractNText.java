package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NImmutableString;
import net.thevpc.nuts.runtime.standalone.text.NTextNodeWriterStringer;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NBlankable;

import java.io.ByteArrayOutputStream;

public abstract class AbstractNText implements NText {
    protected NWorkspace workspace;

    public AbstractNText(NWorkspace workspace) {
        this.workspace=workspace;
    }

    @Override
    public String toString() {
        return immutable().toString();
    }

    @Override
    public NString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out);
        ss.writeNode(this);
        return new NImmutableString(out.toString());
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
    public NText toText() {
        return this;
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
        return NTexts.of().ofBuilder().append(this);
    }

}
