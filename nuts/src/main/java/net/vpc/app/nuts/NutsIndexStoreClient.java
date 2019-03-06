package net.vpc.app.nuts;

import java.util.Iterator;
import java.util.List;

public interface NutsIndexStoreClient {

    List<NutsId> findVersions(NutsId id, NutsSession session);

    Iterator<NutsId> find(NutsIdFilter filter, NutsSession session);

    boolean isEnabled();

    void setEnabled(boolean enabled);

    void invalidate(NutsId id);

    void revalidate(NutsId id);

    boolean subscribe();

    void unsubscribe();

    boolean isSubscribed(NutsRepository repository);
}
