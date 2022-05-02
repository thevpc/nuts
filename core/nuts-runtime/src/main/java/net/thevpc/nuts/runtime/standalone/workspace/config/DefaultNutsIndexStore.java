/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.io.NutsIOException;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.util.NutsDescribables;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.util.NutsPredicates;
import net.thevpc.nuts.util.NutsUtilStrings;

import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DefaultNutsIndexStore extends AbstractNutsIndexStore {


    public DefaultNutsIndexStore(NutsRepository repository) {
        super(repository);
    }

    @Override
    public NutsIterator<NutsId> searchVersions(NutsId id, NutsSession session) {
        return IteratorBuilder.ofSupplier(
                () -> {
                    if (isInaccessible()) {
                        return IteratorBuilder.emptyIterator();
                    }
                    String uu = getIndexURL(session).resolve( NutsConstants.Folders.ID).resolve( "allVersions")
                            + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s"
                                    + "&os=%s&osdist=%s&arch=%s&face=%s&"/*alternative=%s*/,
                            getRepository().getUuid(),
                            NutsUtilStrings.trim(id.getArtifactId()), NutsUtilStrings.trim(id.getRepository()), NutsUtilStrings.trim(id.getGroupId()),
                            NutsUtilStrings.trim(String.join(",",id.getCondition().getOs())),
                            NutsUtilStrings.trim(String.join(",",id.getCondition().getOsDist())),
                            NutsUtilStrings.trim(String.join(",",id.getCondition().getArch())), NutsUtilStrings.trim(id.getFace())
//                , NutsUtilStrings.trim(id.getAlternative())
                    );
                    try {
                        Map[] array = NutsElements.of(session).json().parse(new InputStreamReader(NutsPath.of(uu,session).getInputStream()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> NutsId.of(s.get("stringId").toString()).get(session))
                                .collect(Collectors.toList()).iterator();
                    } catch (UncheckedIOException | NutsIOException e) {
                        setInaccessible();
                        return IteratorBuilder.emptyIterator();
                    }
                },
                e->NutsElements.of(e)
                        .ofObject()
                        .set("type","SearchIndexVersions")
                        .set("source", getIndexURL(session).resolve( NutsConstants.Folders.ID).resolve( "allVersions").toString())
                        .build(),
                session).build();
    }

    @Override
    public NutsIterator<NutsId> search(NutsIdFilter filter, NutsSession session) {
        NutsElements elems = NutsElements.of(session);
        return IteratorBuilder.ofSupplier(
                () -> {
                    if (isInaccessible()) {
                        throw new NutsIndexerNotAccessibleException(session,NutsMessage.cstyle("index search failed for %s",getRepository().getName()));
//                        return IteratorUtils.emptyIterator();
                    }
                    String uu = getIndexURL(session).resolve(NutsConstants.Folders.ID) + "?repositoryUuid=" + getRepository().getUuid();
                    try {
                        Map[] array = elems.json().parse(new InputStreamReader(NutsPath.of(uu,session).getInputStream()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> NutsId.of(s.get("stringId").toString()).get(session))
                                .filter(filter != null ? new NutsIdFilterToNutsIdPredicate(filter, session) : NutsPredicates.always())
                                .iterator();
                    } catch (UncheckedIOException | NutsIOException e) {
                        setInaccessible();
                        throw new NutsIndexerNotAccessibleException(session,NutsMessage.cstyle("index search failed for %s",getRepository().getName()));
//                        return IteratorUtils.emptyIterator();
                    }
                },
                e->NutsElements.of(e)
                        .ofObject().set("type","SearchIndexPackages")
                        .set("source", getIndexURL(session).resolve(NutsConstants.Folders.ID).toString())
                        .set("filter", NutsDescribables.resolveOrToString(filter,session))
                        .build(),
                session).build();
    }

    private NutsPath getIndexURL(NutsSession session) {
        return NutsPath.of("http://localhost:7070/indexer/",session);
    }

    @Override
    public NutsIndexStore invalidate(NutsId id, NutsSession session) {
        if (isInaccessible()) {
            return this;
        }
        String uu = getIndexURL(session).resolve( NutsConstants.Folders.ID).resolve("delete")
                + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                NutsUtilStrings.trim(id.getArtifactId()), NutsUtilStrings.trim(id.getRepository()), NutsUtilStrings.trim(id.getGroupId()), NutsUtilStrings.trim(id.getVersion().toString()),
                NutsUtilStrings.trim(String.join(",",id.getCondition().getOs())),
                NutsUtilStrings.trim(String.join(",",id.getCondition().getOsDist())),
                NutsUtilStrings.trim(String.join(",",id.getCondition().getArch())),
                NutsUtilStrings.trim(id.getFace())
//                ,NutsUtilStrings.trim(id.getAlternative())
        );
        try {
            NutsPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NutsIOException e) {
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
        String uu = getIndexURL(session).resolve(NutsConstants.Folders.ID).resolve("addData")
                + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                NutsUtilStrings.trim(id.getArtifactId()), NutsUtilStrings.trim(id.getRepository()), NutsUtilStrings.trim(id.getGroupId()), NutsUtilStrings.trim(id.getVersion().toString()),
                NutsUtilStrings.trim(String.join(",",id.getCondition().getOs())),
                NutsUtilStrings.trim(String.join(",",id.getCondition().getOsDist())),
                NutsUtilStrings.trim(String.join(",",id.getCondition().getArch())),
                NutsUtilStrings.trim(id.getFace())
//                ,NutsUtilStrings.trim(id.getAlternative())
        );
        try {
            NutsPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NutsIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NutsIndexStore subscribe(NutsSession session) {
        String uu = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(session.locations().getWorkspaceLocation().toString(),session)
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid(),session);
        try {
            NutsPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NutsIOException e) {
            throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unable to subscribe for repository%s", getRepository().getName()), e);
        }
        return this;
    }

    @Override
    public NutsIndexStore unsubscribe(NutsSession session) {
        String uu = "http://localhost:7070/indexer/subscription/unsubscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(session.locations().getWorkspaceLocation().toString(),session)
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid(),session);
        try {
            NutsPath.of(uu,session).getInputStream();
        } catch (UncheckedIOException | NutsIOException e) {
            throw new NutsUnsupportedOperationException(session, NutsMessage.cstyle("unable to unsubscribe for repository %s", getRepository().getName()), e);
        }
        return this;
    }

    @Override
    public boolean isSubscribed(NutsSession session) {
        String uu = "http://localhost:7070/indexer/subscription/isSubscribed?workspaceLocation="
                + CoreIOUtils.urlEncodeString(session.locations().getWorkspaceLocation().toString(),session)
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid(),session);
        try {
            return new Scanner(NutsPath.of(uu,session).getInputStream()).nextBoolean();
        } catch (UncheckedIOException | NutsIOException e) {
            return false;
        }
    }

}
