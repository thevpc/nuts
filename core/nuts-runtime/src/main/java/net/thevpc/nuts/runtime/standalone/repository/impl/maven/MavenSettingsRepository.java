/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.maven;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.core.NRepositorySpec;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.mon.NChronometer;
import net.thevpc.nuts.runtime.standalone.repository.impl.NRepositoryList;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.util.MavenUtils;
import net.thevpc.nuts.runtime.standalone.repository.util.NRepositoryUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.core.NRepositoryLocation;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.spi.NRepositorySelectorList;
import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by vpc on 1/15/17.
 */
public class MavenSettingsRepository extends NRepositoryList {
    /**
     * how long a cached {@code .nuts-repository} probe result stays valid
     */
    private static final long METADATA_CACHE_TTL_MS = 24L * 3600 * 1000;

    private NMavenSettings settings;
    private NLog LOG;

    public MavenSettingsRepository(NRepositorySpec options, NRepository parentRepository) {
        super(options, new NRepository[0], parentRepository, null, false, NConstants.RepoTypes.MAVEN, false);
        LOG = NLog.of(MavenSettingsRepository.class);
        this.settings = new NMavenSettingsLoader(LOG).loadSettingsRepos();
        List<NRepository> base = new ArrayList<>();
        Set<String> usedNames = new HashSet<>();

        addChild(base, options, usedNames, MavenUtils.LOCAL_MAVEN_REPO_NAME,
                MavenUtils.LOCAL_MAVEN_REPO_NAME, settings.getLocalRepository());
        addChild(base, options, usedNames, MavenUtils.CENTRAL_MAVEN_REPO_NAME,
                MavenUtils.CENTRAL_MAVEN_REPO_NAME, settings.getRemoteRepository());
        for (NRepositoryLocation activeRepository : settings.getActiveRepositories()) {
            String id = activeRepository.name();
            if (NBlankable.isBlank(id)) {
                continue;
            }
            addChild(base, options, usedNames, MavenUtils.EXTRA_MAVEN_REPO_NAME,
                    name() + "-" + id.trim(), activeRepository.path());
        }
        this.repoItems = base.toArray(base.toArray(new NRepository[0]));
    }

    /**
     * Creates a settings sub-repository unless an explicit {@code -} selector
     * rejects it, as in {@code --repos=-maven-local}.
     *
     * @param base      collected sub-repositories
     * @param options   parent repository options
     * @param usedNames names already taken, updated with the new name
     * @param type      sub-repository type
     * @param childName desired sub-repository name
     * @param path      sub-repository path
     */
    private void addChild(List<NRepository> base, NRepositorySpec options, Set<String> usedNames,
                          String type, String childName, String path) {
        String unique = uniqueName(name(), childName, usedNames);
        if (isExcluded(unique)) {
            return;
        }
        base.add(createChild(options, type, unique, path));
    }

    /**
     * Whether a settings sub-repository is rejected by an explicit {@code -}
     * selector, such as {@code --repos=-maven-local}.
     *
     * @param childName sub-repository name
     * @return true if the sub-repository must not be created
     */
    private boolean isExcluded(String childName) {
        NRepositorySelectorList selectors = NRepositoryUtils
                .createRepositorySelectorList(
                        NCollections.nonNullList(NWorkspace.of().bootOptions().repositories().orElseGet(java.util.Collections::emptyList)))
                .orNull();
        if (selectors == null) {
            return false;
        }
        boolean excluded = selectors.explicitlyExcludes(
                new NRepositorySpec().name(childName).sourceLocation(NRepositoryLocation.ofName(childName)));
        if (excluded) {
            LOG.log(NMsg.ofC("maven sub-repository %s excluded by %s", childName, selectors).asFine());
        }
        return excluded;
    }

    /**
     * Guarantees that every settings sub-repository gets a unique name. Maven
     * {@code settings.xml} files routinely reuse ids (including the reserved
     * {@code local} and {@code central}), and a duplicate name would make the
     * repository unreachable by {@code --repos} selection and would share its
     * store folder with the first definition.
     *
     * @param parent    parent repository name, such as {@code maven}
     * @param candidate desired child name
     * @param usedNames names already taken
     * @return a name not present in {@code usedNames}
     */
    private String uniqueName(String parent, String candidate, Set<String> usedNames) {
        String name = candidate;
        int i = 2;
        while (!usedNames.add(name)) {
            name = parent + "-" + candidate + "-" + i;
            i++;
        }
        if (!candidate.equals(name)) {
            LOG.log(NMsg.ofC("duplicate maven repository name %s, using %s instead", candidate, name).asWarning());
        }
        return name;
    }

    private MavenFolderRepository createChild(NRepositorySpec options0, String type, String id, String url) {
        NPath p = NPath.of(url);
        String pr = NStringUtils.strip(p.protocol());
        MavenFolderRepository mavenChild = null;
        NRepositorySpec options = new NRepositorySpec();
        options.name(id);
        options.location(
                NPath.of(id).toAbsolute(NWorkspaceExt.of().getConfigModel().getRepositoriesRoot()).toString()
        );
        options.enabled(true);
        options.temporary(true);
        options.failSafe(false);
        options.env(options0.env()==null?null:new HashMap<>(options0.env()));
        options.sourceLocation(new NRepositoryLocation(id, "maven", url));
        switch (pr) {
            //non traversable!
            case "http":
            case "https": {
                if(MavenUtils.EXTRA_MAVEN_REPO_NAME.equals(type)){
                    RepositoryMetadata m = loadRepositoryMetadata(id, url);
                    if(m.found) {
                        if(!NBlankable.isBlank(m.layout)) {
                            options.sourceLocation(new NRepositoryLocation(id, "maven", NStringUtils.strip(m.layout)+"+"+url));
                        }
                        mavenChild = new MavenFolderRepository(options, null);
                    }else{
                        mavenChild = new MavenRemoteXmlRepository(options, null);
                    }
                }else {
                    mavenChild = new MavenRemoteXmlRepository(options, null);
                }
                break;
            }
            default: {
                mavenChild = new MavenFolderRepository(options, null);
            }
        }
        mavenChild.getCache().setReadEnabled(false);
        mavenChild.getCache().setWriteEnabled(false);
        mavenChild.getLib().setReadEnabled(false);
        mavenChild.getLib().setWriteEnabled(false);
        mavenChild.setLockEnabled(false);
        return mavenChild;
    }

    /**
     * Resolves the {@code .nuts-repository} metadata of a remote settings
     * repository. This runs on every workspace boot, so the result is kept in
     * the workspace cache and the remote is only probed when that entry is
     * missing, older than {@link #METADATA_CACHE_TTL_MS} or obsoleted by
     * {@code -N}/{@code --expire}. When offline, the remote is never probed.
     *
     * @param id  sub-repository name
     * @param url sub-repository url
     * @return resolved metadata, never null
     */
    private RepositoryMetadata loadRepositoryMetadata(String id, String url) {
        NPath cacheFile = null;
        try {
            cacheFile = NPath.of(NStoreKey.ofWorkspace(NStoreType.CACHE))
                    .resolve("maven-settings")
                    .resolve(id + "-" + Integer.toHexString(url.hashCode()) + ".nuts-repository");
        } catch (Exception ex) {
            // no cache available
        }
        if (cacheFile != null && cacheFile.isRegularFile()) {
            Instant lm = cacheFile.lastModifiedInstant();
            boolean fresh = lm != null
                    && lm.isAfter(Instant.now().minusMillis(METADATA_CACHE_TTL_MS))
                    && !CoreIOUtils.isObsoletePath(cacheFile);
            if (fresh || isOffline()) {
                try {
                    List<String> lines = NStringUtils.splitLines(cacheFile.readString());
                    if (!lines.isEmpty()) {
                        RepositoryMetadata m = new RepositoryMetadata(
                                "found".equals(lines.get(0).trim()),
                                lines.size() > 1 ? NStringUtils.stripToNull(lines.get(1)) : null
                        );
                        LOG.log(NMsg.ofC("repository metadata of %s loaded from cache %s", url, cacheFile).asDebug());
                        return m;
                    }
                } catch (Exception ex) {
                    // reload
                }
            }
        }
        if (isOffline()) {
            LOG.log(NMsg.ofC("offline, skip repository metadata check for %s", url).asDebug());
            return new RepositoryMetadata(false, null);
        }
        NPath nr = NPath.of(url).resolve(".nuts-repository");
        LOG.log(NMsg.ofC("check repository metadata at %s", nr).asDebug());
        NChronometer c = NChronometer.of();
        RepositoryMetadata m = new RepositoryMetadata(false, null);
        if (nr.exists()) {
            String repositoryLayout = null;
            try {
                NElement e = NElementReader.ofJson().read(nr);
                if (e != null && e.isAnyObject()) {
                    NObjectElement o = e.asObject().get();
                    repositoryLayout = o.getStringValue("repositoryLayout").orNull();
                }
            } catch (Exception ex) {
                // just ignore
            }
            m = new RepositoryMetadata(true, NStringUtils.stripToNull(repositoryLayout));
        }
        c.stop();
        LOG.log(NMsg.ofC("check repository metadata at %s took %s", nr, c.duration()).asDebug());
        if (cacheFile != null) {
            try {
                cacheFile.mkParentDirs().writeString((m.found ? "found" : "missing") + "\n" + (m.layout == null ? "" : m.layout) + "\n");
            } catch (Exception ex) {
                LOG.log(NMsg.ofC("unable to cache repository metadata of %s : %s", url, ex).asWarning());
            }
        }
        return m;
    }

    private static boolean isOffline() {
        try {
            return NSession.of().fetchStrategy().orDefault() == NFetchStrategy.OFFLINE;
        } catch (Exception ex) {
            return false;
        }
    }

    private static class RepositoryMetadata {
        private final boolean found;
        private final String layout;

        RepositoryMetadata(boolean found, String layout) {
            this.found = found;
            this.layout = layout;
        }
    }
}
