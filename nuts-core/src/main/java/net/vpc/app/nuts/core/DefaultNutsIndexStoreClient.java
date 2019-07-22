package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.vpc.app.nuts.core.filters.NutsSearchIdById;
import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;

public class DefaultNutsIndexStoreClient implements NutsIndexStoreClient {

    private NutsRepository repository;
    private boolean enabled = true;
    private Date inaccessibleDate = null;

    public DefaultNutsIndexStoreClient(NutsRepository repository) {
        this.repository = repository;
    }

    private void setInaccessible() {
        inaccessibleDate = new Date();
    }

    private boolean isInaccessible() {
        if (inaccessibleDate == null) {
            return false;
        }
        long elapsed = System.currentTimeMillis() - inaccessibleDate.getTime();
        if (elapsed > 1000 * 60 * 5) {
            inaccessibleDate = null;
            return false;
        }
        return true;
    }

    @Override
    public List<NutsId> searchVersions(NutsId id, NutsRepositorySession session) {
        if (isInaccessible()) {
            return null;
        }
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/allVersions"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        CoreStringUtils.trim(id.getName()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroup()), CoreStringUtils.trim(id.getScope()), CoreStringUtils.trim(id.getOs()),
                        CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace()), CoreStringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnection clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().json().parse(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().id().parse(s.get("stringId").toString()))
                    .collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            setInaccessible();
            return null;
        }
    }

    @Override
    public Iterator<NutsId> search(NutsIdFilter filter, NutsRepositorySession session) {
        if (isInaccessible()) {
            return null;
        }
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "?repositoryUuid=" + repository.getUuid();
        try {
            NutsHttpConnection clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().json().parse(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().id().parse(s.get("stringId").toString()))
                    .filter(filter != null ? new Predicate<NutsId>() {
                        @Override
                        public boolean test(NutsId t) {
                            return filter.acceptSearchId(new NutsSearchIdById(t), session.getSession());
                        }
                    } : (Predicate<NutsId>) id -> true)
                    .iterator();
        } catch (UncheckedIOException e) {
            setInaccessible();
            return null;
        }
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
        if (isInaccessible()) {
            return;
        }
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/delete"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        CoreStringUtils.trim(id.getName()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroup()), CoreStringUtils.trim(id.getVersion().toString()),
                        CoreStringUtils.trim(id.getScope()), CoreStringUtils.trim(id.getOs()), CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace()),
                        CoreStringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnection clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            setInaccessible();
            //
        }
    }

    @Override
    public void revalidate(NutsId id) {
        if (isInaccessible()) {
            return;
        }
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/addData"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        CoreStringUtils.trim(id.getName()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroup()), CoreStringUtils.trim(id.getVersion().toString()),
                        CoreStringUtils.trim(id.getScope()), CoreStringUtils.trim(id.getOs()), CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace()),
                        CoreStringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnection clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            setInaccessible();
            //
        }
    }

    @Override
    public boolean subscribe() {
        String URL = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(repository.getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(repository.getUuid());
        try {
            NutsHttpConnection clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
            return true;
        } catch (UncheckedIOException e) {
            return false;
        }
    }

    @Override
    public void unsubscribe() {
        String URL = "http://localhost:7070/indexer/subscription/unsubscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(repository.getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(repository.getUuid());
        try {
            NutsHttpConnection clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            //
        }
    }

    @Override
    public boolean isSubscribed(NutsRepository repository) {
        boolean subscribed;
        String URL = "http://localhost:7070/indexer/subscription/isSubscribed?workspaceLocation="
                + CoreIOUtils.urlEncodeString(repository.getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(repository.getUuid());
        try {
            NutsHttpConnection clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            return new Scanner(clientFacade.open()).nextBoolean();
        } catch (UncheckedIOException e) {
            return false;
        }
    }

    public NutsRepository getRepository() {
        return repository;
    }

    public void setRepository(NutsRepository repository) {
        this.repository = repository;
    }
}
