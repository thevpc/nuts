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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;

import net.thevpc.nuts.NIndexStore;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NIteratorBuilder;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

public class DefaultNIndexStore extends AbstractNIndexStore {


    public DefaultNIndexStore(NRepository repository) {
        super(repository);
    }

    @Override
    public NIterator<NId> searchVersions(NId id) {
        return NIteratorBuilder.ofSupplier(
                () -> {
                    if (isInaccessible()) {
                        return NIteratorBuilder.emptyIterator();
                    }
                    String uu = getIndexURL().resolve( NConstants.Folders.ID).resolve( "allVersions")
                            + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s"
                                    + "&os=%s&osdist=%s&arch=%s&face=%s&"/*alternative=%s*/,
                            getRepository().getUuid(),
                            NStringUtils.trim(id.getArtifactId()), NStringUtils.trim(id.getRepository()), NStringUtils.trim(id.getGroupId()),
                            NStringUtils.trim(String.join(",",id.getCondition().getOs())),
                            NStringUtils.trim(String.join(",",id.getCondition().getOsDist())),
                            NStringUtils.trim(String.join(",",id.getCondition().getArch())), NStringUtils.trim(id.getFace())
//                , NutsUtilStrings.trim(id.getAlternative())
                    );
                    try {
                        Map[] array = NElements.of().json().parse(new InputStreamReader(NPath.of(uu).getInputStream()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> NId.get(s.get("stringId").toString()).get())
                                .collect(Collectors.toList()).iterator();
                    } catch (UncheckedIOException | NIOException e) {
                        setInaccessible();
                        return NIteratorBuilder.emptyIterator();
                    }
                },
                ()-> NElements.of()
                        .ofObjectBuilder()
                        .set("type","SearchIndexVersions")
                        .set("source", getIndexURL().resolve( NConstants.Folders.ID).resolve( "allVersions").toString())
                        .build()
        ).build();
    }

    @Override
    public NIterator<NId> search(NIdFilter filter) {
        NElements elems = NElements.of();
        return NIteratorBuilder.ofSupplier(
                () -> {
                    if (isInaccessible()) {
                        throw new NIndexerNotAccessibleException(NMsg.ofC("index search failed for %s",getRepository().getName()));
//                        return IteratorUtils.emptyIterator();
                    }
                    String uu = getIndexURL().resolve(NConstants.Folders.ID) + "?repositoryUuid=" + getRepository().getUuid();
                    try {
                        Map[] array = elems.json().parse(new InputStreamReader(NPath.of(uu).getInputStream()), Map[].class);
                        return Arrays.stream(array)
                                .map(s -> NId.get(s.get("stringId").toString()).get())
                                .filter(filter != null ? new NIdFilterToNIdPredicate(filter) : NPredicates.always())
                                .iterator();
                    } catch (UncheckedIOException | NIOException e) {
                        setInaccessible();
                        throw new NIndexerNotAccessibleException(NMsg.ofC("index search failed for %s",getRepository().getName()));
//                        return IteratorUtils.emptyIterator();
                    }
                },
                ()-> NElements.of()
                        .ofObjectBuilder().set("type","SearchIndexPackages")
                        .set("source", getIndexURL().resolve(NConstants.Folders.ID).toString())
                        .set("filter", NEDesc.describeResolveOrToString(filter))
                        .build()
        ).build();
    }

    private NPath getIndexURL() {
        return NPath.of("http://localhost:7070/indexer/");
    }

    @Override
    public NIndexStore invalidate(NId id) {
        if (isInaccessible()) {
            return this;
        }
        String uu = getIndexURL().resolve( NConstants.Folders.ID).resolve("delete")
                + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                NStringUtils.trim(id.getArtifactId()), NStringUtils.trim(id.getRepository()), NStringUtils.trim(id.getGroupId()), NStringUtils.trim(id.getVersion().toString()),
                NStringUtils.trim(String.join(",",id.getCondition().getOs())),
                NStringUtils.trim(String.join(",",id.getCondition().getOsDist())),
                NStringUtils.trim(String.join(",",id.getCondition().getArch())),
                NStringUtils.trim(id.getFace())
//                ,NutsUtilStrings.trim(id.getAlternative())
        );
        try {
            NPath.of(uu).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NIndexStore revalidate(NId id) {
        if (isInaccessible()) {
            return this;
        }
        String uu = getIndexURL().resolve(NConstants.Folders.ID).resolve("addData")
                + String.format("?repositoryUuid=%s&name=%s&repo=%s&group=%s&version=%s"
                        + "&os=%s&osdist=%s&arch=%s&face=%s"/*&alternative=%s*/, getRepository().getUuid(),
                NStringUtils.trim(id.getArtifactId()), NStringUtils.trim(id.getRepository()), NStringUtils.trim(id.getGroupId()), NStringUtils.trim(id.getVersion().toString()),
                NStringUtils.trim(String.join(",",id.getCondition().getOs())),
                NStringUtils.trim(String.join(",",id.getCondition().getOsDist())),
                NStringUtils.trim(String.join(",",id.getCondition().getArch())),
                NStringUtils.trim(id.getFace())
//                ,NutsUtilStrings.trim(id.getAlternative())
        );
        try {
            NPath.of(uu).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            setInaccessible();
            //
        }
        return this;
    }

    @Override
    public NIndexStore subscribe() {
        String uu = "http://localhost:7070/indexer/subscription/subscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(NWorkspace.of().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NPath.of(uu).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            throw new NUnsupportedOperationException(NMsg.ofC("unable to subscribe for repository%s", getRepository().getName()), e);
        }
        return this;
    }

    @Override
    public NIndexStore unsubscribe() {
        String uu = "http://localhost:7070/indexer/subscription/unsubscribe?workspaceLocation="
                + CoreIOUtils.urlEncodeString(NWorkspace.of().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            NPath.of(uu).getInputStream();
        } catch (UncheckedIOException | NIOException e) {
            throw new NUnsupportedOperationException(NMsg.ofC("unable to unsubscribe for repository %s", getRepository().getName()), e);
        }
        return this;
    }

    @Override
    public boolean isSubscribed() {
        String uu = "http://localhost:7070/indexer/subscription/isSubscribed?workspaceLocation="
                + CoreIOUtils.urlEncodeString(NWorkspace.of().getWorkspaceLocation().toString())
                + "&repositoryUuid=" + CoreIOUtils.urlEncodeString(getRepository().getUuid());
        try {
            return new Scanner(NPath.of(uu).getInputStream()).nextBoolean();
        } catch (UncheckedIOException | NIOException e) {
            return false;
        }
    }

}
