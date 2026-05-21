package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.*;

public class DefaultNTextTransformerContext implements NTextTransformerContext {
    private NTitleSequence sequence;
    private NTextTransformer defaultTransformer;
    private NTextTransformConfig config;

    public DefaultNTextTransformerContext(NTextTransformerContext o) {
        this.sequence = o.titleSequence();
        this.defaultTransformer = o.defaultTransformer();
    }

    public DefaultNTextTransformerContext(NTextTransformConfig config) {
        if (config == null) {
            config = new NTextTransformConfig();
        }
        this.config = config;
        this.defaultTransformer = new DefaultNTextTransformer(config);
        this.sequence = config.titleNumberSequence();
        if (sequence == null) {
            sequence = new DefaultNTitleSequence();
        }
    }

    public DefaultNTextTransformerContext() {
    }

    public NTextTransformConfig config() {
        return config;
    }

    @Override
    public NTitleSequence titleSequence() {
        return sequence;
    }

    public DefaultNTextTransformerContext titleSequence(NTitleSequence sequence) {
        this.sequence = sequence;
        return this;
    }

    @Override
    public NTextTransformer defaultTransformer() {
        return defaultTransformer;
    }

    @Override
    public DefaultNTextTransformerContext defaultTransformer(NTextTransformer defaultTransformer) {
        this.defaultTransformer = defaultTransformer;
        return this;
    }

    @Override
    public NTextTransformerContext copy() {
        return new DefaultNTextTransformerContext(this);
    }
}
