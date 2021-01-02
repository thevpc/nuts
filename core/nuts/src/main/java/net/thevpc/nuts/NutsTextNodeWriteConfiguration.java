package net.thevpc.nuts;

/**
 * @category Format
 */
public class NutsTextNodeWriteConfiguration {
    private boolean filtered;
    private boolean titleNumberEnabled;
    private boolean lineNumberEnabled;
    private NutsTitleNumberSequence titleNumberSequence;

    public boolean isLineNumberEnabled() {
        return lineNumberEnabled;
    }

    public NutsTextNodeWriteConfiguration setLineNumberEnabled(boolean lineNumberEnabled) {
        this.lineNumberEnabled = lineNumberEnabled;
        return this;
    }

    public boolean isTitleNumberEnabled() {
        return titleNumberEnabled;
    }

    public NutsTextNodeWriteConfiguration setTitleNumberEnabled(boolean titleNumberEnabled) {
        this.titleNumberEnabled = titleNumberEnabled;
        return this;
    }

    public NutsTitleNumberSequence getTitleNumberSequence() {
        return titleNumberSequence;
    }

    public NutsTextNodeWriteConfiguration setTitleNumberSequence(NutsTitleNumberSequence titleNumberSequence) {
        this.titleNumberSequence = titleNumberSequence;
        return this;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public NutsTextNodeWriteConfiguration setFiltered(boolean filtered) {
        this.filtered = filtered;
        return this;
    }

}
