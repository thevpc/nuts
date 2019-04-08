package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.vpc.app.nuts.core.util.CoreIOUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;

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
    public List<NutsId> findVersions(NutsId id, NutsRepositorySession session) {
        if(isInaccessible()){
            return null;
        }
        String URL = "http://localhost:7070/indexer/"+NutsConstants.Folders.LIB+"/allVersions"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        CoreStringUtils.trim(id.getName()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroup()), CoreStringUtils.trim(id.getScope()), CoreStringUtils.trim(id.getOs()),
                        CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace()), CoreStringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().io().readJson(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().parser().parseId(s.get("stringId").toString()))
                    .collect(Collectors.toList());
        } catch (UncheckedIOException e) {
            setInaccessible();
            return null;
        }
    }

    @Override
    public Iterator<NutsId> find(NutsIdFilter filter, NutsRepositorySession session) {
        if(isInaccessible()){
            return null;
        }
        String URL = "http://localhost:7070/indexer/"+NutsConstants.Folders.LIB+"?repositoryUuid=" + repository.getUuid();
        try {
            NutsHttpConnectionFacade clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().io().readJson(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().parser().parseId(s.get("stringId").toString()))
                    .filter(filter != null ? filter::accept : (Predicate<NutsId>) id -> true)
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
        if(isInaccessible()){
            return;
        }
        String URL = "http://localhost:7070/indexer/"+NutsConstants.Folders.LIB+"/delete"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        CoreStringUtils.trim(id.getName()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroup()), CoreStringUtils.trim(id.getVersion().toString()),
                        CoreStringUtils.trim(id.getScope()), CoreStringUtils.trim(id.getOs()), CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace()),
                        CoreStringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            setInaccessible();
            //
        }
    }

    @Override
    public void revalidate(NutsId id) {
        if(isInaccessible()){
            return;
        }
        String URL = "http://localhost:7070/indexer/"+NutsConstants.Folders.LIB+"/addData"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        CoreStringUtils.trim(id.getName()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroup()), CoreStringUtils.trim(id.getVersion().toString()),
                        CoreStringUtils.trim(id.getScope()), CoreStringUtils.trim(id.getOs()), CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace()),
                        CoreStringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
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
            NutsHttpConnectionFacade clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
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
            NutsHttpConnectionFacade clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
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
            NutsHttpConnectionFacade clientFacade = CoreIOUtils.getHttpClientFacade(repository.getWorkspace(),
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