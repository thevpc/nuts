package net.vpc.app.nuts.runtime.config;

import net.vpc.app.nuts.*;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.vpc.app.nuts.runtime.filters.NutsSearchIdById;
import net.vpc.app.nuts.runtime.util.io.CoreIOUtils;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

public class DefaultNutsIndexStore extends AbstractNutsIndexStore {


    public DefaultNutsIndexStore(NutsRepository repository) {
        super(repository);
    }

    @Override
    public List<NutsId> searchVersions(NutsId id, NutsRepositorySession session) {
        if (isInaccessible()) {
            return null;
        }
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/allVersions"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s&"/*alternative=%s*/, getRepository().getUuid(),
                        CoreStringUtils.trim(id.getArtifactId()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroupId()), CoreStringUtils.trim(id.getOs()),
                        CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace())
//                , CoreStringUtils.trim(id.getAlternative())
        );
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(getRepository().getWorkspace(),
                    URL);
            Map[] array = getRepository().getWorkspace().json().parse(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> getRepository().getWorkspace().id().parse(s.get("stringId").toString()))
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
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "?repositoryUuid=" + getRepository().getUuid();
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(getRepository().getWorkspace(),
                    URL);
            Map[] array = getRepository().getWorkspace().json().parse(new InputStreamReader(clientFacade.open()), Map[].class);
            return Arrays.stream(array)
                    .map(s -> getRepository().getWorkspace().id().parse(s.get("stringId").toString()))
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
    public NutsIndexStore invalidate(NutsId id) {
        if (isInaccessible()) {
            return this;
        }
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/delete"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                        CoreStringUtils.trim(id.getArtifactId()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroupId()), CoreStringUtils.trim(id.getVersion().toString()),
                        CoreStringUtils.trim(id.getOs()), CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace())
//                ,CoreStringUtils.trim(id.getAlternative())
        );
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(getRepository().getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NutsIndexStore revalidate(NutsId id) {
        if (isInaccessible()) {
            return this;
        }
        String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/addData"
                + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                        CoreStringUtils.trim(id.getArtifactId()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroupId()), CoreStringUtils.trim(id.getVersion().toString()),
                        CoreStringUtils.trim(id.getOs()), CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace())
//                ,CoreStringUtils.trim(id.getAlternative())
        );
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(getRepository().getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NutsIndexStore subscribe() {
        String URL = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(getRepository().getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(getRepository().getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            throw new NutsUnsupportedOperationException(getRepository().getWorkspace(),"Unable to subscribe for repository"+getRepository().config().name(),e);
        }
        return this;
    }

    @Override
    public NutsIndexStore unsubscribe() {
        String URL = "http://localhost:7070/indexer/subscription/unsubscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(getRepository().getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(getRepository().getWorkspace(),
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException e) {
            throw new NutsUnsupportedOperationException(getRepository().getWorkspace(),"Unable to unsubscribe for repository"+getRepository().config().name(),e);
        }
        return this;
    }

    @Override
    public boolean isSubscribed() {
        String URL = "http://localhost:7070/indexer/subscription/isSubscribed?workspaceLocation="
                + CoreIOUtils.urlEncodeString(getRepository().getWorkspace().config().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(getRepository().getWorkspace(),
                    URL);
            return new Scanner(clientFacade.open()).nextBoolean();
        } catch (UncheckedIOException e) {
            return false;
        }
    }
}
