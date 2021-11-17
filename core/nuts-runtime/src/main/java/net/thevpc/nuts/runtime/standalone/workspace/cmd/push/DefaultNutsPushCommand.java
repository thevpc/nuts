/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.push;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsConstants;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.thevpc.nuts.spi.NutsRepositorySPI;

/**
 *
 * @author thevpc
 */
public class DefaultNutsPushCommand extends AbstractDefaultNutsPushCommand {

    public DefaultNutsPushCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsPushCommand run() {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsSession session = this.getSession();
        NutsRepositoryFilter repositoryFilter = null;
        Map<NutsId, NutsDefinition> toProcess = new LinkedHashMap<>();
        for (NutsId id : this.getIds()) {
            if (NutsUtilStrings.trim(id.getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("invalid version %s", id.getVersion()));
            }
            NutsDefinition file = session.fetch().setId(id).setSession(session.copy().setTransitive(false)).setContent(true).getResultDefinition();
            if (file == null) {
                throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("nothing to push"));
            }
            toProcess.put(id, file);
        }
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        if (toProcess.isEmpty()) {
            throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("missing package to push"));
        }
        for (Map.Entry<NutsId, NutsDefinition> entry : toProcess.entrySet()) {
            NutsId id = entry.getKey();
            NutsDefinition file = entry.getValue();
            NutsFetchMode fetchMode = this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE;
            NutsWorkspaceUtils wu = NutsWorkspaceUtils.of(session);
            if (NutsBlankable.isBlank(this.getRepository())) {
                Set<String> errors = new LinkedHashSet<>();
                //TODO : CHECK ME, why offline?
                boolean ok = false;
                for (NutsRepository repo : wu.filterRepositoriesDeploy(file.getId(), repositoryFilter)) {
                    NutsDescriptor descr = null;
                    NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                    try {
                        descr = repoSPI.fetchDescriptor().setSession(session).setFetchMode(fetchMode).setId(file.getId()).getResult();
                    } catch (Exception e) {
                        errors.add(CoreStringUtils.exceptionToString(e));
                        //
                    }
                    if (descr != null && repo.config().isSupportedMirroring()) {
                        NutsId id2 = session.config().createContentFaceId(dws.resolveEffectiveId(descr, session), descr);
                        try {
                            repoSPI.push().setId(id2)
                                    .setOffline(offline)
                                    .setRepository(getRepository())
                                    .setArgs(args.toArray(new String[0]))
                                    .setSession(session)
                                    //                                    .setFetchMode(fetchMode)
                                    .run();
                            ok = true;
                            break;
                        } catch (Exception e) {
                            errors.add(CoreStringUtils.exceptionToString(e));
                            //
                        }
                    }
                }
                if (!ok) {
                    throw new NutsPushException(session,id, NutsMessage.cstyle(
                            "unable to push %s to repository %s : %s",
                            id == null ? "<null>" : id,
                            this.getRepository(),
                            String.join("\n", errors)
                            ));
                }
            } else {
                NutsRepository repo = session.repos().getRepository(this.getRepository());
                if (!repo.config().isEnabled()) {
                    throw new NutsIllegalArgumentException(getSession(), NutsMessage.cstyle("repository %s is disabled", repo.getName()));
                }
                NutsId effId = session.config().createContentFaceId(id.builder().setProperties("").build(), file.getDescriptor()) //                        .setAlternative(NutsUtilStrings.trim(file.getDescriptor().getAlternative()))
                        ;
                NutsRepositorySPI repoSPI = wu.repoSPI(repo);
                repoSPI.deploy().setSession(session)
                        .setId(effId)
                        .setContent(file.getFile())
                        .setDescriptor(file.getDescriptor())
                        //                        .setFetchMode(fetchMode)
                        //                        .setOffline(this.isOffline())
                        //                        .setTransitive(true)
                        .run();
            }
        }
        return this;
    }
}
