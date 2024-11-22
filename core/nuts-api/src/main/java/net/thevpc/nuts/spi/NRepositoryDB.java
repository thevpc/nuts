package net.thevpc.nuts.spi;

import net.thevpc.nuts.NAddRepositoryOptions;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.reserved.boot.NReservedBootRepositoryDB;
import net.thevpc.nuts.util.NOptional;

import java.util.Set;

public interface NRepositoryDB extends NComponent {
    static NRepositoryDB ofDefault() {
        return new NReservedBootRepositoryDB();
    }

    static NRepositoryDB of() {
        if (!NSession.of().isPresent()) {
            return ofDefault();
        }
        return NExtensions.of().createComponent(NRepositoryDB.class).get();
    }

    Set<String> findAllNamesByName(String name);

    NOptional<NAddRepositoryOptions> getRepositoryOptionsByName(String name);
    NOptional<NAddRepositoryOptions> getRepositoryOptionsByLocation(String name);
}
