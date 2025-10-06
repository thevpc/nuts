package net.thevpc.nuts.net;

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

public interface NConnexionStringBuilderFactory extends NComponent {
    NConnexionStringBuilder create();

    NOptional<NConnexionStringBuilder> create(String expression);
}
