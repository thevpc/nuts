package net.thevpc.nuts.runtime.core.format.text.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.format.text.NutsTextNodeWriterRaw;

import java.util.List;

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
        return session.text().builder().append(this).immutable();
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
        return session.text().builder().append(this);
    }

}
