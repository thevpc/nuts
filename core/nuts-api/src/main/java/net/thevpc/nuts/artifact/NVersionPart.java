package net.thevpc.nuts.artifact;

import net.thevpc.nuts.util.NGetter;

public interface NVersionPart {
    @NGetter
    NVersionPartType type();

    @NGetter
    String value();
}
