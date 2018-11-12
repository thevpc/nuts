package net.vpc.app.nuts;

import java.util.Collection;

public interface NutsSearchBuilder {
    NutsSearchBuilder addJs(Collection<String> value);

    NutsSearchBuilder addJs(String... value);

    NutsSearchBuilder addId(Collection<String> value);

    NutsSearchBuilder addId(String... value);

    NutsSearchBuilder addArch(Collection<String> value);

    NutsSearchBuilder addArch(String... value);

    NutsSearchBuilder addPackaging(Collection<String> value);

    NutsSearchBuilder addPackaging(String... value);

    NutsSearchBuilder addRepository(Collection<String> value);

    NutsSearchBuilder addRepository(String... value);

    NutsSearch build();
}
