package net.thevpc.nuts;

import net.thevpc.nuts.spi.NutsComponent;

import java.util.regex.Pattern;

public interface NutsGlob extends NutsComponent {
    static NutsGlob of(NutsSession session) {
        return session.extensions().createSupported(NutsGlob.class, true, session);
    }

    char getSeparator();

    NutsGlob setSeparator(char c);

    boolean isGlob(String pattern);

    Pattern toPattern(String pattern);
}
