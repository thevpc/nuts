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
 * <br>
 * Copyright (C) 2016-2020 thevpc
 * <br>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.wscommands;

import net.vpc.app.nuts.runtime.wscommands.AbstractDefaultNutsPushCommand;
import net.vpc.app.nuts.runtime.CoreNutsConstants;
import net.vpc.app.nuts.core.NutsWorkspaceExt;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import net.vpc.app.nuts.NutsDefinition;
import net.vpc.app.nuts.NutsDescriptor;
import net.vpc.app.nuts.NutsFetchMode;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsIllegalArgumentException;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsPushCommand;
import net.vpc.app.nuts.NutsRepository;
import net.vpc.app.nuts.NutsRepositoryFilter;
import net.vpc.app.nuts.NutsRepositoryNotFoundException;
import net.vpc.app.nuts.core.NutsRepositorySupportedAction;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;
import net.vpc.app.nuts.runtime.util.NutsWorkspaceUtils;

/**
 *
 * @author vpc
 */
public class DefaultNutsPushCommand extends AbstractDefaultNutsPushCommand {


    public DefaultNutsPushCommand(NutsWorkspace ws) {
        super(ws);
    }

    @Override
    public NutsPushCommand run() {
        NutsSession session = NutsWorkspaceUtils.of(ws).validateSession(this.getSession());
        NutsRepositoryFilter repositoryFilter = null;
        Map<NutsId, NutsDefinition> toProcess = new LinkedHashMap<>();
        for (NutsId id : this.getIds()) {
            if (CoreStringUtils.trim(id.getVersion().getValue()).endsWith(CoreNutsConstants.Versions.CHECKED_OUT_EXTENSION)) {
                throw new NutsIllegalArgumentException(ws, "Invalid Version " + id.getVersion());
            }
            NutsDefinition file = ws.fetch().setId(id).setSession(session).setContent(true).setTransitive(false).getResultDefinition();
            if (file == null) {
                throw new NutsIllegalArgumentException(ws, "Nothing to push");
            }
            toProcess.put(id, file);
        }
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        if (toProcess.isEmpty()) {
            throw new NutsIllegalArgumentException(ws, "Missing component to push");
        }
        for (Map.Entry<NutsId, NutsDefinition> entry : toProcess.entrySet()) {
            NutsId id = entry.getKey();
            NutsDefinition file = entry.getValue();
            NutsFetchMode fetchMode = this.isOffline() ? NutsFetchMode.LOCAL : NutsFetchMode.REMOTE;
            if (CoreStringUtils.isBlank(this.getRepository())) {
                Set<String> errors = new LinkedHashSet<>();
                //TODO : CHECK ME, why offline?
                boolean ok = false;
                for (NutsRepository repo : NutsWorkspaceUtils.of(ws).filterRepositoriesDeploy(file.getId(), repositoryFilter, session)) {
                    NutsDescriptor descr = null;
                    try {
                        descr = repo.fetchDescriptor().setSession(session).setFetchMode(fetchMode).setId(file.getId()).getResult();
                    } catch (Exception e) {
                        errors.add(CoreStringUtils.exceptionToString(e));
                        //
                    }
                    if (descr != null && repo.config().isSupportedMirroring()) {
                        NutsId id2 = ws.config().createContentFaceId(dws.resolveEffectiveId(descr, session), descr);
                        try {
                            repo.push().setId(id2)
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
                    throw new NutsRepositoryNotFoundException(ws, this.getRepository() + " : " + CoreStringUtils.join("\n", errors));
                }
            } else {
                NutsRepository repo = ws.repos().getRepository(this.getRepository(), session.copy().setTransitive(true));
                if (!repo.config().isEnabled()) {
                    throw new NutsIllegalArgumentException(ws, "Repository " + repo.getName() + " is disabled");
                }
                NutsId effId = ws.config().createContentFaceId(id.builder().setProperties("").build(), file.getDescriptor())
//                        .setAlternative(CoreStringUtils.trim(file.getDescriptor().getAlternative()))
                        ;
                repo.deploy().setSession(session)
                        .setId(effId)
                        .setContent(file.getPath())
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
