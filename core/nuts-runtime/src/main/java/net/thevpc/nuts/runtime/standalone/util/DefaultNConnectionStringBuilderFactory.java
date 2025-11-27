package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.net.NConnectionStringBuilderFactory;
import net.thevpc.nuts.util.NScorableContext;
import net.thevpc.nuts.net.DefaultNConnectionStringBuilder;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.util.NOptional;

public class DefaultNConnectionStringBuilderFactory implements NConnectionStringBuilderFactory {
    @Override
    public NConnectionStringBuilder create() {
        return new DefaultNConnectionStringBuilder();
    }

    @Override
    public NOptional<NConnectionStringBuilder> create(String expression) {
        return DefaultNConnectionStringBuilder.of(expression);
    }
    @Override
    public int getScore(NScorableContext context) {
        return DEFAULT_SCORE;
    }

}
