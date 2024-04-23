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
 * <p>
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
package net.thevpc.nuts.runtime.standalone.workspace.cmd.install;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NMemoryPrintStream;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.runtime.standalone.dependency.util.NDependencyUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.stream.NStreamFromList;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextBuilder;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.log.NLogVerb;
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

    public DefaultNInstallCmd(NSession session) {
        super(session);
    }

    private NDefinition _loadIdContent(NId id, NId forId, NSession session, boolean includeDeps, InstallIdList loaded, NInstallStrategy installStrategy) {
        installStrategy = loaded.validateStrategy(installStrategy);
        NId longNameId = id.getLongId();
        InstallIdInfo def = loaded.get(longNameId);
        if (def != null) {

            if (forId != null) {
                def.forIds.add(forId);
            }

            if (def.definition != null) {
                if (
                        (def.strategy != NInstallStrategy.REQUIRE)
                                || (def.strategy == NInstallStrategy.REQUIRE && installStrategy == NInstallStrategy.REQUIRE)

                ) {
                    return def.definition;
                }
            }
        } else {
            def = loaded.addForInstall(id, installStrategy, false);
            def.extra = true;
            def.doRequire = true;
            if (forId != null) {
                def.forIds.add(forId);
            }
        }
        try {
            def.definition = NFetchCmd.of(id,session)
                    .content()
                    .effective()
                    .setDependencies(includeDeps)
                    .failFast()
                    //
                    .setOptional(false)
                    .addScope(NDependencyScopePattern.RUN)
                    .setDependencyFilter(NDependencyFilters.of(session).byRunnable())
                    //
                    .getResultDefinition();
        } catch (NNotFoundException ee) {
            if (!NDependencyUtils.isRequiredDependency(id.toDependency())) {
                includeDeps = false;
            } else {
                throw ee;
            }
        }
        def.doRequire = true;
        if (def.definition != null) {
            for (NId parent : def.definition.getDescriptor().getParents()) {
                _loadIdContent(parent, id, session, false, loaded, NInstallStrategy.REQUIRE);
            }
            if (includeDeps) {
                NDependencies nDependencies = def.definition.getDependencies().get();
                for (NDependency dependency : def.definition.getDependencies().get(session)) {
                    NId did = dependency.toId();
                    _loadIdContent(did, id, session, false, loaded, NInstallStrategy.REQUIRE);
                }
            }
        } else {
            _LOGOP(session).verb(NLogVerb.WARNING).level(Level.FINE)
                    .log(NMsg.ofC("failed to retrieve %s", def.id));
        }
        return def.definition;
    }

    private boolean doThis(NId id, InstallIdList list, NSession session) {
        List<String> cmdArgs = new ArrayList<>(this.getArgs());
//        if (session.isYes()) {
//            cmdArgs.add(0, "--yes");
//        }
//        if (session.isTrace()) {
//            cmdArgs.add(0, "--trace");
//        }

        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        NWorkspaceExt dws = NWorkspaceExt.of(ws);
        InstallIdInfo info = list.get(id);
        if (info.doInstall) {
            _loadIdContent(info.id, null, session, true, list, info.strategy);
            if (info.definition != null) {
                for (ConditionalArguments conditionalArgument : conditionalArguments) {
                    if (conditionalArgument.getPredicate().test(info.definition)) {
                        cmdArgs.addAll(conditionalArgument.getArgs());
                    }
                }
            }
            dws.installImpl(info.definition, cmdArgs.toArray(new String[0]), info.doSwitchVersion, session);
            return true;
        } else if (info.doRequire) {
            _loadIdContent(info.id, null, session, true, list, info.strategy);
            dws.requireImpl(info.definition, info.doRequireDependencies, new NId[0], session);
            return true;
        } else if (info.doSwitchVersion) {
            dws.getInstalledRepository().setDefaultVersion(info.id, session);
            return true;
        } else if (info.ignored) {
            return false;
        } else {
            throw new NUnexpectedException(getSession(), NMsg.ofPlain("unexpected"));
        }
    }

    @Override
    public NStream<NDefinition> getResult() {
        checkSession();
        if (result == null) {
            run();
        }
        return new NStreamFromList<NDefinition>(getSession(),
                ids.isEmpty() ? null : ids.keySet().toArray()[0].toString(),
                Arrays.asList(result)
        ).withDesc(NEDesc.of("InstallResult"));
    }

    @Override
    public NInstallCmd run() {
        checkSession();
        NWorkspace ws = getSession().getWorkspace();
        NWorkspaceExt dws = NWorkspaceExt.of(ws);
        NSession session = getSession();
        NPrintStream out = session.out();
        NWorkspaceSecurityManager.of(session).checkAllowed(NConstants.Permissions.INSTALL, "install");
//        LinkedHashMap<NutsId, Boolean> allToInstall = new LinkedHashMap<>();
        InstallIdList list = new InstallIdList(NInstallStrategy.INSTALL);
        for (Map.Entry<NId, NInstallStrategy> idAndStrategy : this.getIdMap().entrySet()) {
            if (!list.isVisited(idAndStrategy.getKey())) {
                List<NId> allIds = NSearchCmd.of(session).addId(idAndStrategy.getKey()).setLatest(true).getResultIds().toList();
                if (allIds.isEmpty()) {
                    throw new NNotFoundException(getSession(), idAndStrategy.getKey());
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
            for (NId sid : session.extensions().getCompanionIds()) {
                if (!list.isVisited(sid)) {
                    List<NId> allIds = NSearchCmd.of(session).addId(sid).setLatest(true).setTargetApiVersion(ws.getApiVersion()).getResultIds().toList();
                    if (allIds.isEmpty()) {
                        throw new NNotFoundException(getSession(), sid);
                    }
                    for (NId id0 : allIds) {
                        list.addForInstall(id0.builder().setRepository(null).build(), this.getCompanions(), false);
                    }
                }
            }
        }
//        Map<NutsId, NutsDefinition> defsAll = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToInstall = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToInstallForced = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToDefVersion = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsToIgnore = new LinkedHashMap<>();
//        Map<NutsId, NutsDefinition> defsOk = new LinkedHashMap<>();
        if (isInstalled()) {
            // In all cases, even though search may be empty we considere that the list is not empty
            // so that no empty exception is thrown
            list.emptyCommand = false;
            for (NId resultId : NSearchCmd.of(session).setInstallStatus(
                    NInstallStatusFilters.of(session).byInstalled(true)).getResultIds()) {
                list.addForInstall(resultId, getInstalled(), true);
            }
            // This bloc is to handle packages that were installed but their jar/content was removed for any reason!
            NInstalledRepository ir = dws.getInstalledRepository();
            for (NInstallInformation y : IteratorUtils.toList(ir.searchInstallInformation(session))) {
                if (y != null && y.getInstallStatus().isInstalled() && y.getId() != null) {
                    list.addForInstall(y.getId(), getInstalled(), true);
                }
            }
        }

        for (InstallIdInfo info : list.infos()) {
            NId nid = info.id;
            info.oldInstallStatus = dws.getInstalledRepository().getInstallStatus(nid, session);
//            boolean _installed = installStatus.contains(NutsInstallStatus.INSTALLED);
//            boolean _defVer = dws.getInstalledRepository().isDefaultVersion(nid, session);

//            if (defsToInstallForced.containsKey(nid)) {
//                _installed = true;
//            }
//            boolean nForced = session.isForce() || nutsIdBooleanEntry.getValue();
            //must load dependencies because will be run later!!
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
                        throw new NUnexpectedException(getSession(), NMsg.ofC("unsupported strategy %s", strategy));
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
                        throw new NUnexpectedException(getSession(), NMsg.ofC("unsupported strategy %s", strategy));
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
                        throw new NUnexpectedException(getSession(), NMsg.ofC("unsupported strategy %s", strategy));
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
                        throw new NUnexpectedException(getSession(), NMsg.ofC("unsupported strategy %s", strategy));
                    }
                }
            } else {
                throw new NUnexpectedException(getSession(), NMsg.ofC("unsupported status %s", info.oldInstallStatus));
            }
        }
        Map<String, List<InstallIdInfo>> error = list.infos().stream().filter(x -> x.doError != null).collect(Collectors.groupingBy(installIdInfo -> installIdInfo.doError));
        if (error.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<InstallIdInfo>> stringListEntry : error.entrySet()) {
                out.resetLine().println("the following " + (stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is") + " cannot be ```error installed``` (" + stringListEntry.getKey() + ") : "
                        + stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NIdFormat.of(session).setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
                sb.append("\n" + "the following ").append(stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is").append(" cannot be installed (").append(stringListEntry.getKey()).append(") : ").append(stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NIdFormat.of(session).setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
            }
            throw new NInstallException(getSession(), null, NMsg.ofNtf(sb.toString().trim()), null);
        }
        NMemoryPrintStream mout = NMemoryPrintStream.of(session);
        List<NId> nonIgnored = list.ids(x -> !x.ignored);
        List<NId> list_new_installed = list.ids(x -> x.doInstall && !x.isAlreadyExists());
        List<NId> list_new_required = list.ids(x -> x.doRequire && !x.doInstall && !x.isAlreadyExists());
        List<NId> list_required_rerequired = list.ids(x -> (!x.doInstall && x.doRequire) && x.isAlreadyRequired());
        List<NId> list_required_installed = list.ids(x -> x.doInstall && x.isAlreadyRequired() && !x.isAlreadyInstalled());
        List<NId> list_required_reinstalled = list.ids(x -> x.doInstall && x.isAlreadyInstalled());
        List<NId> list_installed_setdefault = list.ids(x -> x.doSwitchVersion && x.isAlreadyInstalled());
        List<NId> installed_ignored = list.ids(x -> x.ignored);

        if (!nonIgnored.isEmpty()) {
            if (getSession().isPlainTrace() || (!list.emptyCommand && getSession().getConfirm().orDefault() == NConfirmationMode.ASK)) {
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
            if (!NIO.of(getSession()).getDefaultTerminal().ask()
                    .setSession(session)
                    .forBoolean(NMsg.ofNtf(mout.toString()))
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NMsg.ofC("installation cancelled : %s ", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NCancelException(getSession(), NMsg.ofC("installation cancelled: %s", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", "))));
            }
        } else if (!installed_ignored.isEmpty()) {
            //all packages are already installed, ask if we need to re-install!
            if (getSession().isPlainTrace() || (!list.emptyCommand && getSession().getConfirm().orDefault() == NConfirmationMode.ASK)) {
                printList(mout, "installed", "re-reinstalled", installed_ignored);
            }
            mout.println("should we proceed?");
            if (!NIO.of(getSession()).getDefaultTerminal().ask()
                    .setSession(session)
                    .forBoolean(NMsg.ofNtf(mout.toString()))
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NMsg.ofC("installation cancelled : %s ", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NCancelException(getSession(), NMsg.ofC("installation cancelled: %s", nonIgnored.stream().map(NId::getFullName).collect(Collectors.joining(", "))));
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
//        List<String> cmdArgs = new ArrayList<>(Arrays.asList(this.getArgs()));
//        if (session.isForce()) {
//            cmdArgs.add(0, "--force");
//        }
//        if (session.isTrace()) {
//            cmdArgs.add(0, "--force");
//            cmdArgs.add(0, "--trace");
//        }
        List<NDefinition> resultList = new ArrayList<>();
        List<NId> failedList = new ArrayList<>();
        try {
            if (!list.ids(x -> !x.ignored).isEmpty()) {
                for (InstallIdInfo info : list.infos(x -> !x.ignored)) {
                    try {
                        if (doThis(info.id, list, session)) {
                            resultList.add(info.definition);
                        }
                    } catch (RuntimeException ex) {
                        _LOGOP(session).error(ex).verb(NLogVerb.WARNING).level(Level.FINE)
                                .log(NMsg.ofC("failed to install %s", info.id));
                        failedList.add(info.id);
                        if (session.isPlainTrace()) {
                            if (!NIO.of(getSession()).getDefaultTerminal().ask()
                                    .setSession(session)
                                    .forBoolean(NMsg.ofC("%s %s and its dependencies... Continue installation?",
                                            NMsg.ofStyled("failed to install", NTextStyle.error()),
                                            info.id))
                                    .setDefaultValue(true)
                                    .getBooleanValue()) {
                                session.out().resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
                                result = new NDefinition[0];
                                return this;
                            } else {
                                session.out().resetLine().println(NMsg.ofC("%s ```error installation cancelled with error:``` %s%n", info.id, ex));
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
            throw new NExecutionException(getSession(), NMsg.ofPlain("missing packages to install"), NExecutionException.ERROR_1);
        }
        return this;
    }

    private void printList(NPrintStream out, String skind, String saction, List<NId> all) {
        if (all.size() > 0) {
            if (session.isPlainOut()) {
                NTexts text = NTexts.of(session);
                NText kind = text.ofStyled(skind, NTextStyle.primary2());
                NText action =
                        text.ofStyled(saction,
                                saction.equals("set as default") ? NTextStyle.primary3() :
                                        saction.equals("ignored") ? NTextStyle.pale() :
                                                NTextStyle.primary1()
                        );
                NSession session = getSession();
                NTextBuilder msg = NTexts.of(getSession()).ofBuilder();
                msg.append("the following ")
                        .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                        .append(" going to be ").append(action).append(" : ")
                        .appendJoined(
                                NTexts.of(session).ofPlain(", "),
                                all.stream().map(x
                                                -> NTexts.of(session).ofText(
                                                x.builder().build()
                                        )
                                ).collect(Collectors.toList())
                        );
                out.resetLine().println(msg);
            } else {
                NElements elem = NElements.of(session);
                session.eout().add(elem.ofObject()
                        .set("command", "warning")
                        .set("artifact-kind", skind)
                        .set("action-warning", saction)
                        .set("artifacts", elem.ofArray().addAll(
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
