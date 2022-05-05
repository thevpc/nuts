package net.thevpc.nuts.runtime.standalone.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.text.NutsImmutableString;
import net.thevpc.nuts.runtime.standalone.text.NutsTextNodeWriterStringer;
import net.thevpc.nuts.text.NutsText;
import net.thevpc.nuts.text.NutsTextBuilder;
import net.thevpc.nuts.text.NutsTexts;

import java.io.ByteArrayOutputStream;

public abstract class AbstractNutsText implements NutsText {

    private NutsSession session;

    public AbstractNutsText(NutsSession session) {
        this.session = session;
    }

    public NutsSession getSession() {
        return session;
    }

    protected NutsWorkspace getWorkspace() {
        return session.getWorkspace();
    }

    @Override
    public String toString() {
        return immutable().toString();
    }

    @Override
    public NutsString immutable() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        NutsTextNodeWriterStringer ss = new NutsTextNodeWriterStringer(out, session);
        ss.writeNode(this);
        return new NutsImmutableString(session, out.toString());
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
    public NutsText toText() {
        return this;
    }

    @Override
    public boolean isEmpty() {
        return immutable().isEmpty();
    }

    @Override
    public boolean isBlank() {
        return NutsBlankable.isBlank(filteredText());
    }

    @Override
    public NutsTextBuilder builder() {
        return NutsTexts.of(session).ofBuilder().append(this);
    }

}
