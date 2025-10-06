package net.thevpc.nuts.runtime.standalone.util;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.spi.NSupportLevelContext;
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
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

}
