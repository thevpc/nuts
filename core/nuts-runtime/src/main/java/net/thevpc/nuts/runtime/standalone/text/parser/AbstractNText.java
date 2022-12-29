package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.NImmutableString;
import net.thevpc.nuts.runtime.standalone.text.NTextNodeWriterStringer;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTexts;

import java.io.ByteArrayOutputStream;

public abstract class AbstractNText implements NText {

    private NSession session;

    public AbstractNText(NSession session) {
        this.session = session;
    }

    public NSession getSession() {
        return session;
    }

    protected NWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    @Override
    public String toString() {
        return immutable().toString();
    }

    @Override
    public NString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NTextNodeWriterStringer ss = new NTextNodeWriterStringer(out, session);
        ss.writeNode(this);
        return new NImmutableString(session, out.toString());
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
        return NTexts.of(session).ofBuilder().append(this);
    }

}
