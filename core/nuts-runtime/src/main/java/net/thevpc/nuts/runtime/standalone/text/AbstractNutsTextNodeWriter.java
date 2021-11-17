package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsTextWriteConfiguration;

public abstract class AbstractNutsTextNodeWriter implements NutsTextNodeWriter {
    private NutsTextWriteConfiguration config;

    @Override
    public NutsTextWriteConfiguration getWriteConfiguration() {
        return config;
    }

    @Override
    public NutsTextNodeWriter setWriteConfiguration(NutsTextWriteConfiguration config) {
        this.config = config;
        return this;
    }
}
