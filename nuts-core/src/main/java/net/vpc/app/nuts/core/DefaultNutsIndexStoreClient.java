package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreHttpUtils;
import net.vpc.app.nuts.core.util.CoreStringUtils;
import net.vpc.common.strings.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DefaultNutsIndexStoreClient implements NutsIndexStoreClient {

    private NutsRepository repository;
    private boolean enabled = true;

    public DefaultNutsIndexStoreClient(NutsRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<NutsId> findVersions(NutsId id, NutsSession session) {
        String URL = "http://localhost:7070/indexer/components/allVersions" +
                String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s" +
                                "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        StringUtils.trim(id.getName()), StringUtils.trim(id.getNamespace()), StringUtils.trim(id.getGroup()), StringUtils.trim(id.getScope()), StringUtils.trim(id.getOs()),
                        StringUtils.trim(id.getOsdist()), StringUtils.trim(id.getArch()), StringUtils.trim(id.getFace()), StringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().getIOManager().readJson(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().getParseManager().parseId(s.get("stringId").toString()))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public Iterator<NutsId> find(NutsIdFilter filter, NutsSession session) {
        String URL = "http://localhost:7070/indexer/components?repositoryUuid=" + repository.getUuid();
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            Map[] array = repository.getWorkspace().getIOManager().readJson(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> repository.getWorkspace().getParseManager().parseId(s.get("stringId").toString()))
                    .filter(filter != null ? filter::accept : (Predicate<NutsId>) id -> true)
                    .iterator();
        } catch (IOException e) {
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
        String URL = "http://localhost:7070/indexer/components/delete" +
                String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s" +
                                "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        StringUtils.trim(id.getName()), StringUtils.trim(id.getNamespace()), StringUtils.trim(id.getGroup()), StringUtils.trim(id.getVersion().toString()),
                        StringUtils.trim(id.getScope()), StringUtils.trim(id.getOs()), StringUtils.trim(id.getOsdist()), StringUtils.trim(id.getArch()), StringUtils.trim(id.getFace()),
                        StringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public void revalidate(NutsId id) {
        String URL = "http://localhost:7070/indexer/components/addData" +
                String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s" +
                                "&scope=%s&os=%s&osdist=%s&arch=%s&face=%s&alternative=%s", repository.getUuid(),
                        StringUtils.trim(id.getName()), StringUtils.trim(id.getNamespace()), StringUtils.trim(id.getGroup()), StringUtils.trim(id.getVersion().toString()),
                        StringUtils.trim(id.getScope()), StringUtils.trim(id.getOs()), StringUtils.trim(id.getOsdist()), StringUtils.trim(id.getArch()), StringUtils.trim(id.getFace()),
                        StringUtils.trim(id.getAlternative()));
        try {
            NutsHttpConnectionFacade clientFacade = CoreHttpUtils.getHttpClientFacade(repository.getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (IOException e) {
            //
        }
    }

    @Override
    public boolean subscribe() {
        String URL = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreHttpUtils.urlEncodeString(repository.getWorkspace().getConfigManager().getWorkspaceLocation())
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
                + CoreHttpUtils.urlEncodeString(repository.getWorkspace().getConfigManager().getWorkspaceLocation())
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
                + CoreHttpUtils.urlEncodeString(repository.getWorkspace().getConfigManager().getWorkspaceLocation())
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
