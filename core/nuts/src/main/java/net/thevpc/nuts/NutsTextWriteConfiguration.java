package net.thevpc.nuts;

/**
 * @category Format
 */
public class NutsTextWriteConfiguration implements Cloneable{
    private boolean filtered;
    private boolean titleNumberEnabled;
    private boolean lineNumberEnabled;
    private NutsTextNumbering titleNumberSequence;

    public boolean isLineNumberEnabled() {
        return lineNumberEnabled;
    }

    public NutsTextWriteConfiguration setLineNumberEnabled(boolean lineNumberEnabled) {
        this.lineNumberEnabled = lineNumberEnabled;
        return this;
    }

    public boolean isTitleNumberEnabled() {
        return titleNumberEnabled;
    }

    public NutsTextWriteConfiguration setTitleNumberEnabled(boolean titleNumberEnabled) {
        this.titleNumberEnabled = titleNumberEnabled;
        return this;
    }

    public NutsTextNumbering getTitleNumberSequence() {
        return titleNumberSequence;
    }

    public NutsTextWriteConfiguration setTitleNumberSequence(NutsTextNumbering titleNumberSequence) {
        this.titleNumberSequence = titleNumberSequence;
        return this;
    }

    public boolean isFiltered() {
        return filtered;
    }

    public NutsTextWriteConfiguration setFiltered(boolean filtered) {
        this.filtered = filtered;
        return this;
    }

    public NutsTextWriteConfiguration copy(){
        return clone();
    }

    @Override
    protected NutsTextWriteConfiguration clone(){
        try {
            return (NutsTextWriteConfiguration) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
