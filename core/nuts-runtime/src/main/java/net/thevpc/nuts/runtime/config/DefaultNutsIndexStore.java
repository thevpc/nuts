package net.thevpc.nuts.runtime.config;

import net.thevpc.nuts.*;

import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import net.thevpc.nuts.runtime.filters.NutsSearchIdById;
import net.thevpc.nuts.runtime.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.util.iter.IteratorUtils;

public class DefaultNutsIndexStore extends AbstractNutsIndexStore {


    public DefaultNutsIndexStore(NutsRepository repository) {
        super(repository);
    }

    @Override
    public Iterator<NutsId> searchVersions(NutsId id, NutsSession session) {
        return IteratorUtils.supplier(
                () -> {
                    if (isInaccessible()) {
                        return IteratorUtils.emptyIterator();
                    }
                    String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "/allVersions"
                            + String.format("?repositoryUuid=%s&name=%s&namespace=%s&group=%s"
                                    + "&os=%s&osdist=%s&arch=%s&face=%s&"/*alternative=%s*/, getRepository().getUuid(),
                            CoreStringUtils.trim(id.getArtifactId()), CoreStringUtils.trim(id.getNamespace()), CoreStringUtils.trim(id.getGroupId()), CoreStringUtils.trim(id.getOs()),
                            CoreStringUtils.trim(id.getOsdist()), CoreStringUtils.trim(id.getArch()), CoreStringUtils.trim(id.getFace())
//                , CoreStringUtils.trim(id.getAlternative())
                    );
                    try {
                        NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(session,
                                URL);
                        Map[] array = getRepository().getWorkspace().formats().json().parse(new InputStreamReader(clientFacade.open()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> getRepository().getWorkspace().id().parser().parse(s.get("stringId").toString()))
                                .collect(Collectors.toList()).iterator();
                    } catch (UncheckedIOException|NutsIOException e) {
                        setInaccessible();
                        return IteratorUtils.emptyIterator();
                    }
                },
                "searchIndex"
        );
    }

    @Override
    public Iterator<NutsId> search(NutsIdFilter filter, NutsSession session) {
        return IteratorUtils.supplier(
                () -> {
                    if (isInaccessible()) {
                        throw new NutsIndexerNotAccessibleException(session.getWorkspace());
//                        return IteratorUtils.emptyIterator();
                    }
                    String URL = "http://localhost:7070/indexer/" + NutsConstants.Folders.ID + "?repositoryUuid=" + getRepository().getUuid();
                    try {
                        NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(session,
                                URL);
                        Map[] array = getRepository().getWorkspace().formats().json().parse(new InputStreamReader(clientFacade.open()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> getRepository().getWorkspace().id().parser().parse(s.get("stringId").toString()))
                                .filter(filter != null ? new Predicate<NutsId>() {
                                    @Override
                                    public boolean test(NutsId t) {
                                        return filter.acceptSearchId(new NutsSearchIdById(t), session);
                                    }
                                } : (Predicate<NutsId>) id -> true)
                                .iterator();
                    } catch (UncheckedIOException|NutsIOException e) {
                        setInaccessible();
                        throw new NutsIndexerNotAccessibleException(session.getWorkspace());
//                        return IteratorUtils.emptyIterator();
                    }
                },
                "searchIndex"
        );
    }

    @Override
    public NutsIndexStore invalidate(NutsId id, NutsSession session) {
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
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(session,
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException|NutsIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NutsIndexStore revalidate(NutsId id, NutsSession session) {
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
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(session,
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException|NutsIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NutsIndexStore subscribe(NutsSession session) {
        String URL = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(getRepository().getWorkspace().locations().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(session,
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException|NutsIOException e) {
            throw new NutsUnsupportedOperationException(getRepository().getWorkspace(), "Unable to subscribe for repository" + getRepository().getName(), e);
        }
        return this;
    }

    @Override
    public NutsIndexStore unsubscribe(NutsSession session) {
        String URL = "http://localhost:7070/indexer/subscription/unsubscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(getRepository().getWorkspace().locations().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(session,
                    URL);
            clientFacade.open();
        } catch (UncheckedIOException|NutsIOException e) {
            throw new NutsUnsupportedOperationException(getRepository().getWorkspace(), "Unable to unsubscribe for repository" + getRepository().getName(), e);
        }
        return this;
    }

    @Override
    public boolean isSubscribed(NutsSession session) {
        String URL = "http://localhost:7070/indexer/subscription/isSubscribed?workspaceLocation="
                + CoreIOUtils.urlEncodeString(getRepository().getWorkspace().locations().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NutsTransportConnection clientFacade = CoreIOUtils.getHttpClientFacade(session,
                    URL);
            return new Scanner(clientFacade.open()).nextBoolean();
        } catch (UncheckedIOException|NutsIOException e) {
            return false;
        }
    }
}
