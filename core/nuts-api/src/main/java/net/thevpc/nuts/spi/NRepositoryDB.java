package net.thevpc.nuts.spi;

import net.thevpc.nuts.NAddRepositoryOptions;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.util.NOptional;

import java.util.Set;

public interface NRepositoryDB extends NComponent {
    static NRepositoryDB of() {
        return NExtensions.of(NRepositoryDB.class);
    }

    Set<String> findAllNamesByName(String name);

    NOptional<NAddRepositoryOptions> getRepositoryOptionsByName(String name);
    NOptional<NAddRepositoryOptions> getRepositoryOptionsByLocation(String name);
}
