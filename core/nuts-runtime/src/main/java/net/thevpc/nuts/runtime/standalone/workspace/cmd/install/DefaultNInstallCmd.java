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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElementDescribables;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamFromList;
import net.thevpc.nuts.runtime.standalone.workspace.DefaultNWorkspace;
import net.thevpc.nuts.security.NWorkspaceSecurityManager;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;

import java.util.*;
import java.util.logging.Level;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNInstallCmd extends AbstractNInstallCmd {

    public DefaultNInstallCmd(NWorkspace workspace) {
        super(workspace);
    }

//    private NDefinition _loadIdContent(NId id, NId forId, boolean includeDeps, InstallIdList loaded, NInstallStrategy installStrategy, boolean mandatory) {
//        installStrategy = loaded.validateStrategy(installStrategy);
//        NId longNameId = id.getLongId();
//        InstallIdInfo currInstallInfo = loaded.get(longNameId);
//        boolean doAddForInstall = false;
//        if (currInstallInfo != null) {
//            if (forId != null) {
//                currInstallInfo.forIds.add(forId);
//            }
//            if (currInstallInfo.definition != null) {
//                if (currInstallInfo.flags != NInstallStrategy.REQUIRE || installStrategy == NInstallStrategy.REQUIRE) {
//                    return currInstallInfo.definition;
//                }
//            }
//        } else {
//            doAddForInstall = true;
//        }
//        try {
//            NDefinition ndef = NSearchCmd.of(id)
//                    .failFast()
//                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
//                    .latest()
//                    .getResultDefinitions()
//                    .findFirst().get();
//            if (!ndef.getDescriptor().isNoContent()) {
//                ndef.getContent().get();
//            }
//            if (doAddForInstall) {
//                currInstallInfo = loaded.addAsInstalled(id, installStrategy, false);
//                currInstallInfo.extra = true;
//                currInstallInfo.doRequire = true;
//                if (forId != null) {
//                    currInstallInfo.forIds.add(forId);
//                }
//            }
//            currInstallInfo.definition = ndef;
//            currInstallInfo.doRequire = true;
//            for (NId parent : currInstallInfo.definition.getDescriptor().getParents()) {
//                _loadIdContent(parent, id, false, loaded, NInstallStrategy.REQUIRE, true);
//            }
//            if (includeDeps) {
//                currInstallInfo.effectiveDescriptor = ndef.getEffectiveDescriptor().get();
//                ;
//                NDependencies nDependencies = currInstallInfo.definition.getDependencies().get();
//                currInstallInfo.dependencies.clear();
//                currInstallInfo.dependencies.addAll(nDependencies.transitive().toList());
//                for (NDependency dependency : nDependencies.toList()) {
//                    NId did = dependency.toId();
//                    _loadIdContent(did, id, false, loaded, NInstallStrategy.REQUIRE, NDependencyUtils.isRequiredDependency(dependency));
//                }
//            }
//            return currInstallInfo.definition;
//        } catch (RuntimeException ex) {
//            _LOG()
//                    .log(NMsg.ofC("failed to retrieve %s", id)
//                            .withIntent(NMsgIntent.ALERT).withLevel(Level.FINE)
//                    );
//            if (mandatory) {
//                throw ex;
//            }
//            return null;
//        }
//    }

    @Override
    public NStream<NDefinition> getResult() {
        if (result == null) {
            run();
        }
        return new NStreamFromList<NDefinition>(
                ids.isEmpty() ? null : ids.keySet().toArray()[0].toString(),
                Arrays.asList(result)
        ).redescribe(NElementDescribables.ofDesc("InstallResult"));
    }

    @Override
    public NInstallCmd run() {
        NWorkspace ws = NWorkspace.of();
        NWorkspaceExt dws = NWorkspaceExt.of();
        NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.INSTALL, "install");
        InstallIdList list = new InstallIdList();
        for (Map.Entry<NId, InstallFlags> idAndStrategy : ids.entrySet()) {
            if (!list.isVisited(idAndStrategy.getKey())) {
                List<NId> allIds = NSearchCmd.of().addId(idAndStrategy.getKey()).setLatest(true).getResultIds().toList();
                if (allIds.isEmpty()) {
                    throw new NArtifactNotFoundException(idAndStrategy.getKey().getLongId());
                }
                for (NId id0 : allIds) {
                    list.addAsInstalled(id0, idAndStrategy.getValue());
                }
            }
        }
        if (this.isCompanions()) {
            // In all cases, even though search may be empty we consider that the list is not empty
            // so that no empty exception is thrown
            list.emptyCommand = false;
            for (NId sid : NExtensions.of().getCompanionIds()) {
                if (!list.isVisited(sid)) {
                    List<NId> allIds = NSearchCmd.of().addId(sid).setLatest(true).setTargetApiVersion(ws.getApiVersion()).getResultIds().toList();
                    if (allIds.isEmpty()) {
                        throw new NArtifactNotFoundException(sid.getLongId());
                    }
                    for (NId id0 : allIds) {
                        list.addAsInstalled(id0.builder().setRepository(null).build(), companionsInstallFlags);
                    }
                }
            }
        }
        if (isInstalled()) {
            // In all cases, even though search may be empty we consider that the list is not empty
            // so that no empty exception is thrown
            list.emptyCommand = false;
            InstallFlags v = installedInstallFlags.copy();
            v.force = true;
            for (NId resultId : NSearchCmd.of().setDefinitionFilter(NDefinitionFilters.of().byInstalled(true)).getResultIds()) {
                list.addAsInstalled(resultId, v);
            }
            // This bloc is to handle packages that were installed but their jar/content was removed for any reason!
            NInstalledRepository ir = dws.getInstalledRepository();
            for (NInstallInformation y : NIteratorUtils.toList(ir.searchInstallInformation())) {
                if (y != null && y.getInstallStatus().isInstalled() && y.getId() != null) {
                    list.addAsInstalled(y.getId(), v);
                }
            }
        }

        InstallHelper h = new InstallHelper((DefaultNWorkspace) dws, list, false, args, conditionalArguments);
        h.installAll();
        this.result = h.result;
        this.failed = h.failed;
        return this;
    }


}
