package net.thevpc.nuts.runtime.standalone.net;

import net.thevpc.nuts.net.NConnectionStringBuilderFactory;
import net.thevpc.nuts.util.NScore;
import net.thevpc.nuts.util.NScorable;
import net.thevpc.nuts.runtime.standalone.net.DefaultNConnectionStringBuilder;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.util.NOptional;

@NScore(fixed = NScorable.DEFAULT_SCORE)
public class DefaultNConnectionStringBuilderFactory implements NConnectionStringBuilderFactory {
    @Override
    public NConnectionStringBuilder create() {
        return new DefaultNConnectionStringBuilder();
    }

    @Override
    public NOptional<NConnectionStringBuilder> create(String expression) {
        return DefaultNConnectionStringBuilder.of(expression);
    }

}
