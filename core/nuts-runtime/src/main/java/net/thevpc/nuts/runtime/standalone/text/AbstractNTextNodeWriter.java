package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NTextTransformConfig;

public abstract class AbstractNTextNodeWriter implements NTextNodeWriter {
    private NTextTransformConfig config;

    @Override
    public NTextTransformConfig getWriteConfiguration() {
        return config;
    }

    @Override
    public NTextNodeWriter setWriteConfiguration(NTextTransformConfig config) {
        this.config = config;
        return this;
    }
}
