package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NutsTextTransformConfig;

public abstract class AbstractNutsTextNodeWriter implements NutsTextNodeWriter {
    private NutsTextTransformConfig config;

    @Override
    public NutsTextTransformConfig getWriteConfiguration() {
        return config;
    }

    @Override
    public NutsTextNodeWriter setWriteConfiguration(NutsTextTransformConfig config) {
        this.config = config;
        return this;
    }
}
