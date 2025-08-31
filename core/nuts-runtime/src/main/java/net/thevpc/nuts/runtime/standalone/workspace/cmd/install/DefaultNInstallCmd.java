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

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamFromList;
import net.thevpc.nuts.util.NIteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStream;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNInstallCmd extends AbstractNInstallCmd {

    public DefaultNInstallCmd(NWorkspace workspace) {
        super(workspace);
    }

    private NDefinition _loadIdContent(NId id, NId forId, boolean includeDeps, InstallIdList loaded, NInstallStrategy installStrategy, boolean mandatory) {
        installStrategy = loaded.validateStrategy(installStrategy);
        NId longNameId = id.getLongId();
        InstallIdInfo currInstallInfo = loaded.get(longNameId);
        boolean doAddForInstall = false;
        if (currInstallInfo != null) {
            if (forId != null) {
                currInstallInfo.forIds.add(forId);
            }
            if (currInstallInfo.definition != null) {
                if (currInstallInfo.strategy != NInstallStrategy.REQUIRE || installStrategy == NInstallStrategy.REQUIRE) {
                    return currInstallInfo.definition;
                }
            }
        } else {
            doAddForInstall = true;
        }
        try {
            NDefinition ndef = NSearchCmd.of(id)
                    .failFast()
                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                    .latest()
                    .getResultDefinitions()
                    .findFirst().get();
            if (!ndef.getDescriptor().isNoContent()) {
                ndef.getContent().get();
            }
            ndef.getEffectiveDescriptor().get();
            if (includeDeps) {
                ndef.getDependencies().get();
            }
            if (doAddForInstall) {
                currInstallInfo = loaded.addForInstall(id, installStrategy, false);
                currInstallInfo.extra = true;
                currInstallInfo.doRequire = true;
                if (forId != null) {
                    currInstallInfo.forIds.add(forId);
                }
            }
            currInstallInfo.definition = ndef;
            currInstallInfo.doRequire = true;
            for (NId parent : currInstallInfo.definition.getDescriptor().getParents()) {
                _loadIdContent(parent, id, false, loaded, NInstallStrategy.REQUIRE, true);
            }
            if (includeDeps) {
                NDependencies nDependencies = currInstallInfo.definition.getDependencies().get();
                for (NDependency dependency : nDependencies) {
                    NId did = dependency.toId();
                    _loadIdContent(did, id, false, loaded, NInstallStrategy.REQUIRE, NDependencyUtils.isRequiredDependency(dependency));
                }
            }
            return currInstallInfo.definition;
        } catch (RuntimeException ex) {
            _LOG()
                    .log(NMsg.ofC("failed to retrieve %s", id)
                            .withIntent(NMsgIntent.ALERT).withLevel(Level.FINE)
                    );
            if (mandatory) {
                throw ex;
            }
            return null;
        }
    }

    private boolean doInstallOneImpl(NId id, InstallIdList list) {
        List<String> cmdArgs = new ArrayList<>(this.getArgs());
        NWorkspaceExt dws = NWorkspaceExt.of();
        InstallIdInfo info = list.get(id);
        if (info.doInstall) {
            _loadIdContent(info.id, null, true, list, info.strategy, NDependencyUtils.isRequiredDependency(id.toDependency()));
            if (info.definition != null) {
                for (ConditionalArguments conditionalArgument : conditionalArguments) {
                    if (conditionalArgument.getPredicate().test(info.definition)) {
                        cmdArgs.addAll(conditionalArgument.getArgs());
                    }
                }
            }
            dws.installImpl(info.definition, cmdArgs.toArray(new String[0]), info.doSwitchVersion);
            return true;
        } else if (info.doRequire) {
            _loadIdContent(info.id, null, true, list, info.strategy, NDependencyUtils.isRequiredDependency(id.toDependency()));
            dws.requireImpl(info.definition, info.doRequireDependencies, new NId[0]);
            return true;
        } else if (info.doSwitchVersion) {
            dws.getInstalledRepository().setDefaultVersion(info.id);
            return true;
        } else if (info.ignored) {
            return false;
        } else {
            throw new NUnexpectedException(NMsg.ofPlain("unexpected"));
        }
    }

    @Override
    public NStream<NDefinition> getResult() {
        if (result == null) {
            run();
        }
        return new NStreamFromList<NDefinition>(
                ids.isEmpty() ? null : ids.keySet().toArray()[0].toString(),
                Arrays.asList(result)
        ).redescribe(NDescribables.ofDesc("InstallResult"));
    }

    @Override
    public NInstallCmd run() {
        NWorkspace ws = NWorkspace.of();
        NWorkspaceExt dws = NWorkspaceExt.of();
        NWorkspaceSecurityManager.of().checkAllowed(NConstants.Permissions.INSTALL, "install");
        InstallIdList list = new InstallIdList(NInstallStrategy.INSTALL);
        for (Map.Entry<NId, NInstallStrategy> idAndStrategy : this.getIdMap().entrySet()) {
            if (!list.isVisited(idAndStrategy.getKey())) {
                List<NId> allIds = NSearchCmd.of().addId(idAndStrategy.getKey()).setLatest(true).getResultIds().toList();
                if (allIds.isEmpty()) {
                    throw new NNotFoundException(idAndStrategy.getKey());
                }
                for (NId id0 : allIds) {
                    list.addForInstall(id0, idAndStrategy.getValue(), false);
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
                        throw new NNotFoundException(sid);
                    }
                    for (NId id0 : allIds) {
                        list.addForInstall(id0.builder().setRepository(null).build(), this.getCompanions(), false);
                    }
                }
            }
        }
        if (isInstalled()) {
            // In all cases, even though search may be empty we consider that the list is not empty
            // so that no empty exception is thrown
            list.emptyCommand = false;
            for (NId resultId : NSearchCmd.of().setDefinitionFilter(NDefinitionFilters.of().byInstalled(true)).getResultIds()) {
                list.addForInstall(resultId, getInstalled(), true);
            }
            // This bloc is to handle packages that were installed but their jar/content was removed for any reason!
            NInstalledRepository ir = dws.getInstalledRepository();
            for (NInstallInformation y : NIteratorUtils.toList(ir.searchInstallInformation())) {
                if (y != null && y.getInstallStatus().isInstalled() && y.getId() != null) {
                    list.addForInstall(y.getId(), getInstalled(), true);
                }
            }
        }

        for (InstallIdInfo info : list.infos()) {
            NId nid = info.id;
            info.oldInstallStatus = dws.getInstalledRepository().getInstallStatus(nid);
            NInstallStrategy strategy = getStrategy();
            if (strategy == null) {
                strategy = NInstallStrategy.DEFAULT;
            }
            if (strategy == NInstallStrategy.DEFAULT) {
                strategy = NInstallStrategy.INSTALL;
            }
            if (!info.getOldInstallStatus().isInstalled()) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        info.doSwitchVersion = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doError = "cannot repair non installed package";
                        break;
                    }
                    case SWITCH_VERSION: {
                        info.doError = "cannot switch version for non installed package";
                        break;
                    }
                    default: {
                        throw new NUnexpectedException(NMsg.ofC("unsupported strategy %s", strategy));
                    }
                }
            } else if (info.getOldInstallStatus().isObsolete()) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doInstall = info.getOldInstallStatus().isInstalled();
                        info.doRequire = !info.doInstall;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doRequire = true;
                        break;
                    }
                    case SWITCH_VERSION: {
                        if (info.getOldInstallStatus().isInstalled()) {
                            info.doSwitchVersion = true;
                        } else {
                            info.doError = "cannot switch version for non installed package";
                        }
                        break;
                    }
                    default: {
                        throw new NUnexpectedException(NMsg.ofC("unsupported strategy %s", strategy));
                    }
                }
            } else if (info.getOldInstallStatus().isInstalled()) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.ignored = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doRequire = true;
                        break;
                    }
                    case SWITCH_VERSION: {
                        info.doSwitchVersion = true;
                        break;
                    }
                    default: {
                        throw new NUnexpectedException(NMsg.ofC("unsupported strategy %s", strategy));
                    }
                }
            } else if (info.getOldInstallStatus().isRequired()) {
                switch (strategy) {
                    case REQUIRE: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case INSTALL: {
                        info.doInstall = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REINSTALL: {
                        info.doRequire = true;
                        info.doRequireDependencies = true;
                        break;
                    }
                    case REPAIR: {
                        info.doRequire = true;
                        break;
                    }
                    case SWITCH_VERSION: {
                        info.doError = "cannot switch version for non installed package";
                        break;
                    }
                    default: {
                        throw new NUnexpectedException(NMsg.ofC("unsupported strategy %s", strategy));
                    }
                }
            } else {
                throw new NUnexpectedException(NMsg.ofC("unsupported status %s", info.oldInstallStatus));
            }
        }
        Map<String, List<InstallIdInfo>> error = list.infos().stream().filter(x -> x.doError != null).collect(Collectors.groupingBy(installIdInfo -> installIdInfo.doError));
        if (error.size() > 0) {
            StringBuilder sb = new StringBuilder();
            NPrintStream out = NOut.out();
            for (Map.Entry<String, List<InstallIdInfo>> stringListEntry : error.entrySet()) {
                out.resetLine().println("the following " + (stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is") + " cannot be ```error installed``` (" + stringListEntry.getKey() + ") : "
                        + stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NIdFormat.of().setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
                sb.append("\n" + "the following ").append(stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is").append(" cannot be installed (").append(stringListEntry.getKey()).append(") : ").append(stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NIdFormat.of().setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
            }
            throw new NInstallException(null, NMsg.ofNtf(sb.toString().trim()), null);
        }
        NMemoryPrintStream mout = NMemoryPrintStream.of();
        List<NId> nonIgnored = list.ids(x -> !x.ignored);
        List<NId> list_new_installed = list.ids(x -> x.doInstall && !x.isAlreadyExists());
        List<NId> list_new_required = list.ids(x -> x.doRequire && !x.doInstall && !x.isAlreadyExists());
        List<NId> list_required_rerequired = list.ids(x -> (!x.doInstall && x.doRequire) && x.isAlreadyRequired());
        List<NId> list_required_installed = list.ids(x -> x.doInstall && x.isAlreadyRequired() && !x.isAlreadyInstalled());
        List<NId> list_required_reinstalled = list.ids(x -> x.doInstall && x.isAlreadyInstalled());
        List<NId> list_installed_setdefault = list.ids(x -> x.doSwitchVersion && x.isAlreadyInstalled());
        List<NId> installed_ignored = list.ids(x -> x.ignored);

        NSession session = NSession.of();
        if (!nonIgnored.isEmpty()) {
            if (session.isPlainTrace() || (!list.emptyCommand && session.getConfirm().orDefault() == NConfirmationMode.ASK)) {
                printList(mout, "new", "installed", list_new_installed);
                printList(mout, "new", "required", list_new_required);
                printList(mout, "required", "re-required", list_required_rerequired);
                printList(mout, "required", "installed", list_required_installed);
                printList(mout, "required", "re-reinstalled", list_required_reinstalled);
                printList(mout, "installed", "set as default", list_installed_setdefault);
                printList(mout, "installed", "ignored", installed_ignored);
            }
            if (!list_required_reinstalled.isEmpty() || !list_required_rerequired.isEmpty()) {
                mout.println("should we proceed re-installation?");
            } else {
                mout.println("should we proceed installation?");
            }
            if (!NIO.of().getDefaultTerminal().ask()
                    .forBoolean(NMsg.ofNtf(mout.toString()))
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NMsg.ofC("installation cancelled : %s ", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NCancelException(NMsg.ofC("installation cancelled: %s", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", "))));
            }
        } else if (!installed_ignored.isEmpty()) {
            //all packages are already installed, ask if we need to re-install!
            if (session.isPlainTrace() || (!list.emptyCommand && session.getConfirm().orDefault() == NConfirmationMode.ASK)) {
                printList(mout, "installed", "re-reinstalled", installed_ignored);
            }
            mout.println("should we proceed?");
            if (!NIO.of().getDefaultTerminal().ask()
                    .forBoolean(NMsg.ofNtf(mout.toString()))
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NMsg.ofC("installation cancelled : %s ", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NCancelException(NMsg.ofC("installation cancelled: %s", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", "))));
            }
            //force installation
            for (InstallIdInfo info : list.infos()) {
                if (info.ignored) {
                    info.ignored = false;
                    info.doInstall = true;
                    info.forced = true;
                }
            }
        }
        List<NDefinition> resultList = new ArrayList<>();
        List<NId> failedList = new ArrayList<>();
        try {
            if (!list.ids(x -> !x.ignored).isEmpty()) {
                for (InstallIdInfo info : list.infos(x -> !x.ignored)) {
                    try {
                        if (doInstallOneImpl(info.id, list)) {
                            resultList.add(info.definition);
                        }
                    } catch (RuntimeException ex) {
                        _LOG()
                                .log(NMsg.ofC("failed to install %s", info.id).asFine(ex)
                                        .withIntent(NMsgIntent.ALERT)
                                );
                        failedList.add(info.id);
                        if (session.isPlainTrace()) {
                            if (!NIO.of().getDefaultTerminal().ask()
                                    .forBoolean(NMsg.ofC("%s %s and its dependencies... Continue installation?",
                                            NMsg.ofStyledError("failed to install"),
                                            info.id))
                                    .setDefaultValue(true)
                                    .getBooleanValue()) {
                                NOut.resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
                                result = new NDefinition[0];
                                return this;
                            } else {
                                NOut.resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
                            }
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        } finally {
            result = resultList.toArray(new NDefinition[0]);
            failed = failedList.toArray(new NId[0]);
        }
        if (list.emptyCommand) {
            throw new NExecutionException(NMsg.ofPlain("missing packages to install"), NExecutionException.ERROR_1);
        }
        return this;
    }

    private void printList(NPrintStream out, String skind, String saction, List<NId> all) {
        if (!all.isEmpty()) {
            NSession session = NSession.of();
            if (NOut.isPlain()) {
                NTexts text = NTexts.of();
                NText kind = text.ofStyled(skind, NTextStyle.primary2());
                NText action =
                        text.ofStyled(saction,
                                saction.equals("set as default") ? NTextStyle.primary3() :
                                        saction.equals("ignored") ? NTextStyle.pale() :
                                                NTextStyle.primary1()
                        );
                NTextBuilder msg = NTextBuilder.of();
                msg.append("the following ")
                        .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                        .append(" going to be ").append(action).append(" : ")
                        .appendJoined(
                                NText.ofPlain(", "),
                                all.stream().map(x
                                                -> NText.of(
                                                x.builder().build()
                                        )
                                ).collect(Collectors.toList())
                        );
                out.resetLine().println(msg);
            } else {
                session.eout().add(NElement.ofObjectBuilder()
                        .set("command", "warning")
                        .set("artifact-kind", skind)
                        .set("action-warning", saction)
                        .set("artifacts", NElement.ofArrayBuilder().addAll(
                                all.stream().map(x -> x.toString()).toArray(String[]::new)
                        ).build())
                        .build()
                );
            }
        }
    }

    private static class InstallIdInfo {

        boolean extra;
        String sid;
        NId id;
        boolean forced;
        boolean doRequire;
        boolean doRequireDependencies;
        boolean doInstall;
        boolean ignored;
        boolean doSwitchVersion;
        NInstallStrategy strategy;
        String doError;
        NInstallStatus oldInstallStatus;
        Set<NId> forIds = new HashSet<>();
        NDefinition definition;

        public boolean isAlreadyRequired() {
            return oldInstallStatus.isRequired();
        }

        public boolean isAlreadyInstalled() {
            return oldInstallStatus.isInstalled();
        }

        public boolean isAlreadyExists() {
            return oldInstallStatus.isInstalled()
                    || oldInstallStatus.isRequired();
        }

        public NInstallStatus getOldInstallStatus() {
            return oldInstallStatus;
        }
    }

    private static class InstallIdList {

        boolean emptyCommand = true;
        NInstallStrategy defaultStrategy;
        Map<String, InstallIdInfo> visited = new LinkedHashMap<>();

        public InstallIdList(NInstallStrategy defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }

        public boolean isVisited(NId id) {
            return visited.containsKey(normalizeId(id));
        }

        private String normalizeId(NId id) {
            return id.builder().setRepository(null).setProperty("optional", null).build().toString();
        }

        public List<NId> ids(Predicate<InstallIdInfo> filter) {
            return infos().stream().filter(filter).map(x -> x.id).collect(Collectors.toList());
        }

        public List<InstallIdInfo> infos(Predicate<InstallIdInfo> filter) {
            if (filter == null) {
                return infos();
            }
            return infos().stream().filter(filter).collect(Collectors.toList());
        }

        public List<InstallIdInfo> infos() {
            return new ArrayList<>(visited.values());
        }

        public NInstallStrategy validateStrategy(NInstallStrategy strategy) {
            if (strategy == null) {
                strategy = NInstallStrategy.DEFAULT;
            }
            if (strategy == NInstallStrategy.DEFAULT) {
                strategy = defaultStrategy;
            }
            return strategy;
        }

        public InstallIdInfo addForInstall(NId id, NInstallStrategy strategy, boolean forced) {
            emptyCommand = false;
            InstallIdInfo ii = new InstallIdInfo();
            ii.forced = forced;
            ii.id = id;
            ii.sid = normalizeId(id);

            ii.strategy = validateStrategy(strategy);
            visited.put(normalizeId(id), ii);
            return ii;
        }

        public InstallIdInfo get(NId id) {
            return visited.get(normalizeId(id));
        }
    }
}
