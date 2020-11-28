package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsTextNodeWriteConfiguration;

public abstract class AbstractNutsTextNodeWriter implements NutsTextNodeWriter {
    private NutsTextNodeWriteConfiguration config;

    @Override
    public NutsTextNodeWriteConfiguration getWriteConfiguration() {
        return config;
    }

    @Override
    public NutsTextNodeWriter setWriteConfiguration(NutsTextNodeWriteConfiguration config) {
        this.config = config;
        return this;
    }
}
