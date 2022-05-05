package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.text.*;

public class DefaultNutsTextTransformerContext implements NutsTextTransformerContext {
    private NutsTitleSequence sequence;
    private NutsTextTransformer defaultTransformer;

    public DefaultNutsTextTransformerContext(NutsTextTransformerContext o) {
        this.sequence = o.getTitleSequence();
        this.defaultTransformer = o.getDefaultTransformer();
    }

    public DefaultNutsTextTransformerContext(NutsTextTransformConfig config, NutsSession session) {
        if (config == null) {
            config = new NutsTextTransformConfig();
        }
        this.defaultTransformer = new DefaultNutsTextTransformer(config, session);
        this.sequence = config.getTitleNumberSequence();
        if (sequence == null) {
            sequence = new DefaultNutsTitleSequence();
        }
    }

    public DefaultNutsTextTransformerContext() {
    }

    @Override
    public NutsTitleSequence getTitleSequence() {
        return sequence;
    }

    public DefaultNutsTextTransformerContext setTitleSequence(NutsTitleSequence sequence) {
        this.sequence = sequence;
        return this;
    }

    @Override
    public NutsTextTransformer getDefaultTransformer() {
        return defaultTransformer;
    }

    @Override
    public DefaultNutsTextTransformerContext setDefaultTransformer(NutsTextTransformer defaultTransformer) {
        this.defaultTransformer = defaultTransformer;
        return this;
    }

    @Override
    public NutsTextTransformerContext copy() {
        return new DefaultNutsTextTransformerContext(this);
    }
}
