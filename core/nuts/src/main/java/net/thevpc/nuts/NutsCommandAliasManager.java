package net.thevpc.nuts;

import java.util.List;

public interface NutsCommandAliasManager {

    NutsCommandAliasFactoryConfig[] getFactories(NutsSession session);

    void addFactory(NutsCommandAliasFactoryConfig commandFactory, NutsAddOptions options);

    boolean removeFactory(String name, NutsRemoveOptions options);

    boolean add(NutsCommandAliasConfig command, NutsAddOptions options);

    boolean remove(String name, NutsRemoveOptions options);

    /**
     * return alias definition for given name id and owner.
     *
     * @param name     alias name, not null
     * @param forId    if not null, the alias name should resolve to the given id
     * @param forOwner if not null, the alias name should resolve to the owner
     * @param session  session
     * @return alias definition or null
     */
    NutsWorkspaceCommandAlias find(String name, NutsId forId, NutsId forOwner, NutsSession session);

    NutsWorkspaceCommandAlias find(String name, NutsSession session);

    List<NutsWorkspaceCommandAlias> findAll(NutsSession session);

    List<NutsWorkspaceCommandAlias> findByOwner(NutsId id, NutsSession session);

}
