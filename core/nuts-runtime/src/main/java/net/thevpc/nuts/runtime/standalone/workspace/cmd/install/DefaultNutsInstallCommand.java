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
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.repository.impl.main.NutsInstalledRepository;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsDependencyUtils;
import net.thevpc.nuts.runtime.standalone.stream.NutsListStream;

import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * type: Command Class
 *
 * @author thevpc
 */
public class DefaultNutsInstallCommand extends AbstractNutsInstallCommand {

    public DefaultNutsInstallCommand(NutsWorkspace ws) {
        super(ws);
    }

    private NutsDefinition _loadIdContent(NutsId id, NutsId forId, NutsSession session, boolean includeDeps, InstallIdList loaded, NutsInstallStrategy installStrategy) {
        installStrategy = loaded.validateStrategy(installStrategy);
        NutsId longNameId = id.getLongId();
        InstallIdInfo def = loaded.get(longNameId);
        if (def != null) {

            if (forId != null) {
                def.forIds.add(forId);
            }

            if (def.definition != null) {
                if (
                        (def.strategy != NutsInstallStrategy.REQUIRE)
                                || (def.strategy == NutsInstallStrategy.REQUIRE && installStrategy == NutsInstallStrategy.REQUIRE)

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
        NutsSession ss = session.copy();
        checkSession();
        def.definition = ss.fetch().setId(id).setSession(ss)
                .setContent(true)
                .setEffective(true)
                .setDependencies(includeDeps)
                .setFailFast(true)
                //
                .setOptional(false)
                .addScope(NutsDependencyScopePattern.RUN)
                .setDependencyFilter(CoreNutsDependencyUtils.createJavaRunDependencyFilter(session))
                //
                .getResultDefinition();
        def.doRequire = true;
        if (includeDeps) {
            for (NutsDependency dependency : def.definition.getDependencies()) {
                _loadIdContent(dependency.toId(), id, session, false, loaded, NutsInstallStrategy.REQUIRE);
            }
        }
        return def.definition;
    }

    private boolean doThis(NutsId id, InstallIdList list, NutsSession session) {
        List<String> cmdArgs = new ArrayList<>(Arrays.asList(this.getArgs()));
//        if (session.isYes()) {
//            cmdArgs.add(0, "--yes");
//        }
//        if (session.isTrace()) {
//            cmdArgs.add(0, "--trace");
//        }

        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
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
            dws.installImpl(info.definition, cmdArgs.toArray(new String[0]), null, session, info.doSwitchVersion);
            return true;
        } else if (info.doRequire) {
            _loadIdContent(info.id, null, session, true, list, info.strategy);
            dws.requireImpl(info.definition, session, info.doRequireDependencies, new NutsId[0]);
            return true;
        } else if (info.doSwitchVersion) {
            dws.getInstalledRepository().setDefaultVersion(info.id, session);
            return true;
        } else if (info.ignored) {
            return false;
        } else {
            throw new NutsUnexpectedException(getSession(), NutsMessage.cstyle("unexpected"));
        }
    }

    @Override
    public NutsStream<NutsDefinition> getResult() {
        checkSession();
        if (result == null) {
            run();
        }
        return new NutsListStream<NutsDefinition>(getSession(),
                ids.isEmpty() ? null : ids.keySet().toArray()[0].toString(),
                Arrays.asList(result),e->e.ofString("InstallResult")
        );
    }

    @Override
    public NutsInstallCommand run() {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getSession();
        NutsPrintStream out = session.out();
        session.security().checkAllowed(NutsConstants.Permissions.INSTALL, "install");
//        LinkedHashMap<NutsId, Boolean> allToInstall = new LinkedHashMap<>();
        InstallIdList list = new InstallIdList(NutsInstallStrategy.INSTALL);
        for (Map.Entry<NutsId, NutsInstallStrategy> idAndStrategy : this.getIdMap().entrySet()) {
            if (!list.isVisited(idAndStrategy.getKey())) {
                List<NutsId> allIds = session.search().addId(idAndStrategy.getKey()).setSession(session).setLatest(true).getResultIds().toList();
                if (allIds.isEmpty()) {
                    throw new NutsNotFoundException(getSession(), idAndStrategy.getKey());
                }
                for (NutsId id0 : allIds) {
                    list.addForInstall(id0, idAndStrategy.getValue(), false);
                }
            }
        }
        if (this.isCompanions()) {
            for (NutsId sid : session.extensions().getCompanionIds()) {
                if (!list.isVisited(sid)) {
                    List<NutsId> allIds = session.search().setSession(session).addId(sid).setLatest(true).setTargetApiVersion(ws.getApiVersion()).getResultIds().toList();
                    if (allIds.isEmpty()) {
                        throw new NutsNotFoundException(getSession(), sid);
                    }
                    for (NutsId id0 : allIds) {
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
            for (NutsId resultId : session.search().setSession(session).setInstallStatus(
                    NutsInstallStatusFilters.of(session).byInstalled(true)).getResultIds()) {
                list.addForInstall(resultId, getInstalled(), true);
            }
            // This bloc is to handle packages that were installed but their jar/content was removed for any reason!
            NutsInstalledRepository ir = dws.getInstalledRepository();
            for (NutsInstallInformation y : IteratorUtils.toList(ir.searchInstallInformation(session))) {
                if (y != null && y.getInstallStatus().isInstalled() && y.getId() != null) {
                    list.addForInstall(y.getId(), getInstalled(), true);
                }
            }
        }

        for (InstallIdInfo info : list.infos()) {
            NutsId nid = info.id;
            info.oldInstallStatus = dws.getInstalledRepository().getInstallStatus(nid, session);
//            boolean _installed = installStatus.contains(NutsInstallStatus.INSTALLED);
//            boolean _defVer = dws.getInstalledRepository().isDefaultVersion(nid, session);

//            if (defsToInstallForced.containsKey(nid)) {
//                _installed = true;
//            }
//            boolean nForced = session.isForce() || nutsIdBooleanEntry.getValue();
            //must load dependencies because will be run later!!
            NutsInstallStrategy strategy = getStrategy();
            if (strategy == null) {
                strategy = NutsInstallStrategy.DEFAULT;
            }
            if (strategy == NutsInstallStrategy.DEFAULT) {
                strategy = NutsInstallStrategy.INSTALL;
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
                        throw new NutsUnexpectedException(getSession(), NutsMessage.cstyle("unsupported strategy %s", strategy));
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
                        throw new NutsUnexpectedException(getSession(), NutsMessage.cstyle("unsupported strategy %s", strategy));
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
                        throw new NutsUnexpectedException(getSession(), NutsMessage.cstyle("unsupported strategy %s", strategy));
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
                        throw new NutsUnexpectedException(getSession(), NutsMessage.cstyle("unsupported strategy %s", strategy));
                    }
                }
            } else {
                throw new NutsUnexpectedException(getSession(), NutsMessage.cstyle("unsupported status %s", info.oldInstallStatus));
            }
        }
        Map<String, List<InstallIdInfo>> error = list.infos().stream().filter(x -> x.doError != null).collect(Collectors.groupingBy(installIdInfo -> installIdInfo.doError));
        if (error.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, List<InstallIdInfo>> stringListEntry : error.entrySet()) {
                out.resetLine().println("the following " + (stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is") + " cannot be ```error installed``` (" + stringListEntry.getKey() + ") : "
                        + stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NutsIdFormat.of(session).setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
                sb.append("\n" + "the following ").append(stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is").append(" cannot be installed (").append(stringListEntry.getKey()).append(") : ").append(stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> NutsIdFormat.of(session).setOmitImportedGroupId(true).setValue(x.getLongId()).format().toString())
                        .collect(Collectors.joining(", ")));
            }
            throw new NutsInstallException(getSession(), null, NutsMessage.formatted(sb.toString().trim()), null);
        }
        NutsMemoryPrintStream mout = NutsMemoryPrintStream.of(session);
        List<NutsId> nonIgnored = list.ids(x -> !x.ignored);
        List<NutsId> list_new_installed = list.ids(x -> x.doInstall && !x.isAlreadyExists());
        List<NutsId> list_new_required = list.ids(x -> x.doRequire && !x.doInstall && !x.isAlreadyExists());
        List<NutsId> list_required_rerequired = list.ids(x -> (!x.doInstall && x.doRequire) && x.isAlreadyRequired());
        List<NutsId> list_required_installed = list.ids(x -> x.doInstall && x.isAlreadyRequired() && !x.isAlreadyInstalled());
        List<NutsId> list_required_reinstalled = list.ids(x -> x.doInstall && x.isAlreadyInstalled());
        List<NutsId> list_installed_setdefault = list.ids(x -> x.doSwitchVersion && x.isAlreadyInstalled());
        List<NutsId> installed_ignored = list.ids(x -> x.ignored);

        if (!nonIgnored.isEmpty()) {
            if (getSession().isPlainTrace() || (!list.emptyCommand && getSession().getConfirm() == NutsConfirmationMode.ASK)) {
                printList(mout, "new", "installed", list_new_installed);
                printList(mout, "new", "required", list_new_required);
                printList(mout, "required", "re-required", list_required_rerequired);
                printList(mout, "required", "installed", list_required_installed);
                printList(mout, "required", "re-reinstalled", list_required_reinstalled);
                printList(mout, "installed", "set as default", list_installed_setdefault);
                printList(mout, "installed", "ignored", installed_ignored);
            }
            mout.println("should we proceed?");
            if (!getSession().config().getDefaultTerminal().ask()
                    .resetLine()
                    .setSession(session)
                    .forBoolean(mout.toString())
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NutsMessage.cstyle("installation cancelled : %s ", nonIgnored.stream().map(NutsId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NutsUserCancelException(getSession(), NutsMessage.cstyle("installation cancelled: %s", nonIgnored.stream().map(NutsId::getFullName).collect(Collectors.joining(", "))));
            }
        } else if (!installed_ignored.isEmpty()) {
            //all packages are already installed, ask if we need to re-install!
            if (getSession().isPlainTrace() || (!list.emptyCommand && getSession().getConfirm() == NutsConfirmationMode.ASK)) {
                printList(mout, "installed", "re-reinstalled", installed_ignored);
            }
            mout.println("should we proceed?");
            if (!getSession().config().getDefaultTerminal().ask()
                    .resetLine()
                    .setSession(session)
                    .forBoolean(mout.toString())
                    .setDefaultValue(true)
                    .setCancelMessage(
                            NutsMessage.cstyle("installation cancelled : %s ", nonIgnored.stream().map(NutsId::getFullName).collect(Collectors.joining(", ")))
                    )
                    .getBooleanValue()) {
                throw new NutsUserCancelException(getSession(), NutsMessage.cstyle("installation cancelled: %s", nonIgnored.stream().map(NutsId::getFullName).collect(Collectors.joining(", "))));
            }
            //force installation
            for (InstallIdInfo info : list.infos()) {
                if(info.ignored){
                    info.ignored=false;
                    info.doInstall=true;
                    info.forced=true;
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
        List<NutsDefinition> resultList = new ArrayList<>();
        List<NutsId> failedList = new ArrayList<>();
        try {
            if (!list.ids(x -> !x.ignored).isEmpty()) {
                for (InstallIdInfo info : list.infos(x -> !x.ignored)) {
                    try {
                        if (doThis(info.id, list, session)) {
                            resultList.add(info.definition);
                        }
                    } catch (RuntimeException ex) {
                        _LOGOP(session).error(ex).verb(NutsLogVerb.WARNING).level(Level.FINE)
                                .log(NutsMessage.jstyle("failed to install {0}", info.id));
                        failedList.add(info.id);
                        if (session.isPlainTrace()) {
                            if (!getSession().config().getDefaultTerminal().ask()
                                    .resetLine()
                                    .setSession(session)
                                    .forBoolean("```error failed to install``` %s and its dependencies... Continue installation?", info.id)
                                    .setDefaultValue(true)
                                    .getBooleanValue()) {
                                session.out().resetLine().printf("%s ```error installation cancelled with error:``` %s%n", info.id, ex);
                                result = new NutsDefinition[0];
                                return this;
                            } else {
                                session.out().resetLine().printf("%s ```error installation cancelled with error:``` %s%n", info.id, ex);
                            }
                        } else {
                            throw ex;
                        }
                    }
                }
            }
        } finally {
            result = resultList.toArray(new NutsDefinition[0]);
            failed = failedList.toArray(new NutsId[0]);
        }
        if (list.emptyCommand) {
            throw new NutsExecutionException(getSession(), NutsMessage.cstyle("missing packages to install"), 1);
        }
        return this;
    }

    private void printList(NutsPrintStream out, String skind, String saction, List<NutsId> all) {
        if (all.size() > 0) {
            if (session.isPlainOut()) {
                NutsTexts text = NutsTexts.of(session);
                NutsText kind = text.ofStyled(skind, NutsTextStyle.primary2());
                NutsText action =
                        text.ofStyled(saction,
                                saction.equals("set as default") ? NutsTextStyle.primary3() :
                                        saction.equals("ignored") ? NutsTextStyle.pale() :
                                                NutsTextStyle.primary1()
                        );
                NutsSession session = getSession();
                NutsTextBuilder msg = NutsTexts.of(getSession()).builder();
                msg.append("the following ")
                        .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                        .append(" going to be ").append(action).append(" : ")
                        .appendJoined(
                                NutsTexts.of(session).ofPlain(", "),
                                all.stream().map(x
                                                -> NutsTexts.of(session).toText(
                                                x.builder().omitImportedGroupId().build()
                                        )
                                ).collect(Collectors.toList())
                        );
                out.resetLine().println(msg);
            } else {
                NutsElements elem = NutsElements.of(session);
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
        NutsId id;
        boolean forced;
        boolean doRequire;
        boolean doRequireDependencies;
        boolean doInstall;
        boolean ignored;
        boolean doSwitchVersion;
        NutsInstallStrategy strategy;
        String doError;
        NutsInstallStatus oldInstallStatus;
        Set<NutsId> forIds = new HashSet<>();
        NutsDefinition definition;

        //        public boolean isOldInstallStatus(NutsInstallStatus o0, NutsInstallStatus... o) {
//            if (!oldInstallStatus.contains(o0)) {
//                return false;
//            }
//            for (NutsInstallStatus s : o) {
//                if (!oldInstallStatus.contains(s)) {
//                    return false;
//                }
//            }
//            return true;
//        }
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

        public NutsInstallStatus getOldInstallStatus() {
            return oldInstallStatus;
        }
    }

    private static class InstallIdList {

        boolean emptyCommand = true;
        NutsInstallStrategy defaultStrategy;
        Map<String, InstallIdInfo> visited = new LinkedHashMap<>();

        public InstallIdList(NutsInstallStrategy defaultStrategy) {
            this.defaultStrategy = defaultStrategy;
        }

        public boolean isVisited(NutsId id) {
            return visited.containsKey(normalizeId(id));
        }

        private String normalizeId(NutsId id) {
            return id.builder().setRepository(null).setProperty("optional", null).build().toString();
        }

        public List<NutsId> ids(Predicate<InstallIdInfo> filter) {
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

        public NutsInstallStrategy validateStrategy(NutsInstallStrategy strategy) {
            if (strategy == null) {
                strategy = NutsInstallStrategy.DEFAULT;
            }
            if (strategy == NutsInstallStrategy.DEFAULT) {
                strategy = defaultStrategy;
            }
            return strategy;
        }

        public InstallIdInfo addForInstall(NutsId id, NutsInstallStrategy strategy, boolean forced) {
            emptyCommand = false;
            InstallIdInfo ii = new InstallIdInfo();
            ii.forced = forced;
            ii.id = id;
            ii.sid = normalizeId(id);

            ii.strategy = validateStrategy(strategy);
            visited.put(normalizeId(id), ii);
            return ii;
        }

        public InstallIdInfo get(NutsId id) {
            return visited.get(normalizeId(id));
        }
    }
}
