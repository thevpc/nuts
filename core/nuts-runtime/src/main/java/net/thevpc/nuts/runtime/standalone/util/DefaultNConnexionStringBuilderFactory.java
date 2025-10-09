package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.net.DefaultNConnexionStringBuilder;
import net.thevpc.nuts.net.NConnexionStringBuilder;
import net.thevpc.nuts.net.NConnexionStringBuilderFactory;
import net.thevpc.nuts.util.NOptional;

public class DefaultNConnexionStringBuilderFactory implements NConnexionStringBuilderFactory {
    @Override
    public NConnexionStringBuilder create() {
        return new DefaultNConnexionStringBuilder();
    }

    @Override
    public NOptional<NConnexionStringBuilder> create(String expression) {
        return DefaultNConnexionStringBuilder.of(expression);
    }
    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

}
