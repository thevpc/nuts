package net.thevpc.nuts.net;

import net.thevpc.nuts.spi.NComponent;
import net.thevpc.nuts.util.NOptional;

public interface NConnectionStringBuilderFactory extends NComponent {
    NConnectionStringBuilder create();

    NOptional<NConnectionStringBuilder> create(String expression);
}
