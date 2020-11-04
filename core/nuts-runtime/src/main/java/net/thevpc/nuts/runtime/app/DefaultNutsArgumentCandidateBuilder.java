package net.thevpc.nuts.runtime.app;

import net.thevpc.nuts.NutsArgumentCandidate;
import net.thevpc.nuts.NutsArgumentCandidateBuilder;

public class DefaultNutsArgumentCandidateBuilder implements NutsArgumentCandidateBuilder {
    private String value;
    private String display;

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public DefaultNutsArgumentCandidateBuilder setValue(String value) {
        this.value = value;
        return this;
    }

    @Override
    public String getDisplay() {
        return display;
    }

    @Override
    public DefaultNutsArgumentCandidateBuilder setDisplay(String display) {
        this.display = display;
        return this;
    }

    @Override
    public NutsArgumentCandidateBuilder setCandidate(NutsArgumentCandidate value) {
        setValue(value==null?null:value.getValue());
        setDisplay(value==null?null:value.getDisplay());
        return this;
    }

    @Override
    public NutsArgumentCandidate build() {
        return new NutsDefaultArgumentCandidate(value,display);
    }
}
