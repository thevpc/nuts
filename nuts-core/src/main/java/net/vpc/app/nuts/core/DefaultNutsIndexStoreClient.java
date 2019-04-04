package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreHttpUtils;
import net.vpc.common.strings.StringUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
        String URL = "http://localhost:7070/indexer/components/allVersions"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        StringUtils.trim(id.getName()), StringUtils.trim(id.getNamespace()), StringUtils.trim(id.getGroup()), StringUtils.trim(id.getScope()), StringUtils.trim(id.getOs()),
                        StringUtils.trim(id.getOsdist()), StringUtils.trim(id.getArch()), StringUtils.trim(id.getFace()), StringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().io().readJson(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().parser().parseId(s.get("stringId").toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            setInaccessible();
            return null;
        }
    }

    @Override
    public Iterator<NutsId> find(NutsIdFilter filter, NutsRepositorySession session) {
        if(isInaccessible()){
            return null;
        }
        String URL = "http://localhost:7070/indexer/components?repositoryUuid=" + repository.getUuid();
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().io().readJson(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().parser().parseId(s.get("stringId").toString()))
                    .filter(filter != null ? filter::accept : (Predicate<NutsId>) id -> true)
                    .iterator();
        } catch (IOException e) {
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
        String URL = "http://localhost:7070/indexer/components/delete"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        StringUtils.trim(id.getName()), StringUtils.trim(id.getNamespace()), StringUtils.trim(id.getGroup()), StringUtils.trim(id.getVersion().toString()),
                        StringUtils.trim(id.getScope()), StringUtils.trim(id.getOs()), StringUtils.trim(id.getOsdist()), StringUtils.trim(id.getArch()), StringUtils.trim(id.getFace()),
                        StringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (IOException e) {
            setInaccessible();
            //
        }
    }

    @Override
    public void revalidate(NutsId id) {
        if(isInaccessible()){
            return;
        }
        String URL = "http://localhost:7070/indexer/components/addData"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        StringUtils.trim(id.getName()), StringUtils.trim(id.getNamespace()), StringUtils.trim(id.getGroup()), StringUtils.trim(id.getVersion().toString()),
                        StringUtils.trim(id.getScope()), StringUtils.trim(id.getOs()), StringUtils.trim(id.getOsdist()), StringUtils.trim(id.getArch()), StringUtils.trim(id.getFace()),
                        StringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (IOException e) {
            setInaccessible();
            //
        }
    }

    @Override
    public boolean subscribe() {
        String URL = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreHttpUtils.urlEncodeString(repository.getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreHttpUtils.urlEncodeString(repository.getUuid());
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Override
    public void unsubscribe() {
        String URL = "http://localhost:7070/indexer/subscription/unsubscribe?workspaceLocation="
                + CoreHttpUtils.urlEncodeString(repository.getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreHttpUtils.urlEncodeString(repository.getUuid());
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public boolean isSubscribed(NutsRepository repository) {
        boolean subscribed;
        String URL = "http://localhost:7070/indexer/subscription/isSubscribed?workspaceLocation="
                + CoreHttpUtils.urlEncodeString(repository.getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreHttpUtils.urlEncodeString(repository.getUuid());
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            return new Scanner(clientFacade.open()).nextBoolean();
        } catch (IOException e) {
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
