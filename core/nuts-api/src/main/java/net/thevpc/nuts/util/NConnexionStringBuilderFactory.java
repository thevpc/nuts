package net.thevpc.nuts.util;

import net.thevpc.nuts.spi.NComponent;

public interface NConnexionStringBuilderFactory extends NComponent {
    NConnexionStringBuilder create();

    NOptional<NConnexionStringBuilder> create(String expression);
}
