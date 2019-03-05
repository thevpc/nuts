package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreHttpUtils;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

public class DefaultNutsIndexStoreClient implements NutsIndexStoreClient {

    private NutsRepository repository;
    private boolean enabled = true;

    public DefaultNutsIndexStoreClient(NutsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<NutsId> findVersions(NutsId id, NutsSession session) {
        try {
//            String URL = String.format("http://localhost:7070/indexer/components/dependencies" +
//                            "?workspace=%s&name=%s&namespace=%s&group=%s&version=%s&" +
//                            "face=%s&os=%s&osdist=%s&scope=%s&alternative=%s&arch=%s",
//                    repository.getWorkspace().getUuid(), name, namespace, group, version, face, os, osdist, scope, alternative, arch)
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    "http://");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Iterator<NutsId> find(NutsIdFilter filter, NutsSession session) {
        return null;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public void invalidate(NutsId id) {

    }

    @Override
    public void revalidate(NutsId id) {

    }

    public NutsRepository getRepository() {
        return repository;
    }

    public void setRepository(NutsRepository repository) {
        this.repository = repository;
    }
}
