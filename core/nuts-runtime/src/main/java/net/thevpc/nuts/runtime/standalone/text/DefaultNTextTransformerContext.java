package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.text.*;

public class DefaultNTextTransformerContext implements NTextTransformerContext {
    private NTitleSequence sequence;
    private NTextTransformer defaultTransformer;
    private NTextTransformConfig config;

    public DefaultNTextTransformerContext(NTextTransformerContext o) {
        this.sequence = o.getTitleSequence();
        this.defaultTransformer = o.getDefaultTransformer();
    }

    public DefaultNTextTransformerContext(NTextTransformConfig config, NWorkspace workspace) {
        if (config == null) {
            config = new NTextTransformConfig();
        }
        this.config = config;
        this.defaultTransformer = new DefaultNTextTransformer(config, workspace);
        this.sequence = config.getTitleNumberSequence();
        if (sequence == null) {
            sequence = new DefaultNTitleSequence();
        }
    }

    public DefaultNTextTransformerContext() {
    }

    public NTextTransformConfig getConfig() {
        return config;
    }

    @Override
    public NTitleSequence getTitleSequence() {
        return sequence;
    }

    public DefaultNTextTransformerContext setTitleSequence(NTitleSequence sequence) {
        this.sequence = sequence;
        return this;
    }

    @Override
    public NTextTransformer getDefaultTransformer() {
        return defaultTransformer;
    }

    @Override
    public DefaultNTextTransformerContext setDefaultTransformer(NTextTransformer defaultTransformer) {
        this.defaultTransformer = defaultTransformer;
        return this;
    }

    @Override
    public NTextTransformerContext copy() {
        return new DefaultNTextTransformerContext(this);
    }
}
