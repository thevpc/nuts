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
package net.thevpc.nuts.runtime.standalone.wscommands;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.iter.IteratorUtils;
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.repos.NutsInstalledRepository;
import net.thevpc.nuts.runtime.core.util.CoreNutsDependencyUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsCollectionResult;

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
        NutsId longNameId = id.getLongNameId();
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
        NutsSession ss = CoreNutsUtils.silent(session).copy();
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        def.definition = ws.fetch().setId(id).setSession(ss)
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
        if (session.isYes()) {
            cmdArgs.add(0, "--yes");
        }
        if (session.isTrace()) {
            cmdArgs.add(0, "--trace");
        }

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
    public NutsResultList<NutsDefinition> getResult() {
        checkSession();
        if (result == null) {
            run();
        }
        return new NutsCollectionResult<NutsDefinition>(getSession(),
                ids.isEmpty() ? null : ids.keySet().toArray()[0].toString(),
                Arrays.asList(result)
        );
    }

    @Override
    public NutsInstallCommand run() {
        checkSession();
        NutsWorkspace ws = getSession().getWorkspace();
        NutsWorkspaceExt dws = NutsWorkspaceExt.of(ws);
        NutsSession session = getSession();
        NutsSession searchSession = CoreNutsUtils.silent(session);
        NutsPrintStream out = session.out();
        ws.security().setSession(getSession()).checkAllowed(NutsConstants.Permissions.INSTALL, "install");
//        LinkedHashMap<NutsId, Boolean> allToInstall = new LinkedHashMap<>();
        InstallIdList list = new InstallIdList(NutsInstallStrategy.INSTALL);
        for (Map.Entry<NutsId, NutsInstallStrategy> idAndStrategy : this.getIdMap().entrySet()) {
            if (!list.isVisited(idAndStrategy.getKey())) {
                List<NutsId> allIds = ws.search().addId(idAndStrategy.getKey()).setSession(searchSession).setLatest(true).getResultIds().list();
                if (allIds.isEmpty()) {
                    throw new NutsNotFoundException(getSession(), idAndStrategy.getKey());
                }
                for (NutsId id0 : allIds) {
                    list.addForInstall(id0, idAndStrategy.getValue(), false);
                }
            }
        }
        if (this.isCompanions()) {
            for (NutsId sid : ws.getCompanionIds(session)) {
                if (!list.isVisited(sid)) {
                    List<NutsId> allIds = ws.search().setSession(searchSession).addId(sid).setLatest(true).setTargetApiVersion(ws.getApiVersion()).getResultIds().list();
                    if (allIds.isEmpty()) {
                        throw new NutsNotFoundException(getSession(), sid);
                    }
                    for (NutsId id0 : allIds) {
                        list.addForInstall(id0.builder().setNamespace(null).build(), this.getCompanions(), false);
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
            for (NutsId resultId : ws.search().setSession(searchSession).setInstallStatus(
                    ws.filters().installStatus().byInstalled(true)).getResultIds()) {
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
                        .map(x -> ws.id().formatter().omitImportedGroupId().value(x.getLongNameId()).format().toString())
                        .collect(Collectors.joining(", ")));
                sb.append("\n" + "the following ").append(stringListEntry.getValue().size() > 1 ? "artifacts are" : "artifact is").append(" cannot be installed (").append(stringListEntry.getKey()).append(") : ").append(stringListEntry.getValue().stream().map(x -> x.id)
                        .map(x -> ws.id().formatter().omitImportedGroupId().value(x.getLongNameId()).format().toString())
                        .collect(Collectors.joining(", ")));
            }
            throw new NutsInstallException(getSession(), null, NutsMessage.formatted(sb.toString().trim()), null);
        }

        NutsTextManager text = ws.text().setSession(session);
        if (getSession().isPlainTrace() || (!list.emptyCommand && getSession().getConfirm() == NutsConfirmationMode.ASK)) {
            printList(out, text.builder().append("new", NutsTextStyle.primary2()),
                    text.builder().append("installed", NutsTextStyle.primary1()),
                    list.ids(x -> x.doInstall && !x.isAlreadyExists()));

            printList(out, text.builder().append("new", NutsTextStyle.primary2()),
                    text.builder().append("required", NutsTextStyle.primary1()),
                    list.ids(x -> x.doRequire && !x.doInstall && !x.isAlreadyExists()));
            printList(out,
                    text.builder().append("required", NutsTextStyle.primary2()),
                    text.builder().append("re-required", NutsTextStyle.primary1()),
                    list.ids(x -> (!x.doInstall && x.doRequire) && x.isAlreadyRequired()));
            printList(out,
                    text.builder().append("required", NutsTextStyle.primary2()),
                    text.builder().append("installed", NutsTextStyle.primary1()),
                    list.ids(x -> x.doInstall && x.isAlreadyRequired() && !x.isAlreadyInstalled()));

            printList(out,
                    text.builder().append("required", NutsTextStyle.primary2()),
                    text.builder().append("re-reinstalled", NutsTextStyle.primary1()),
                    list.ids(x -> x.doInstall && x.isAlreadyInstalled()));
            printList(out,
                    text.builder().append("installed", NutsTextStyle.primary2()),
                    text.builder().append("set as default", NutsTextStyle.primary3()),
                    list.ids(x -> x.doSwitchVersion && x.isAlreadyInstalled()));
            printList(out,
                    text.builder().append("installed", NutsTextStyle.primary2()),
                    text.builder().append("ignored", NutsTextStyle.pale()),
                    list.ids(x -> x.ignored));
        }
        List<NutsId> nonIgnored = list.ids(x -> !x.ignored);
        if (!nonIgnored.isEmpty() && !ws.term().setSession(getSession()).getTerminal().ask()
                .resetLine()
                .setSession(session)
                .forBoolean("should we proceed?")
                .setDefaultValue(true)
                .setCancelMessage("installation cancelled : %s ", nonIgnored.stream().map(NutsId::getFullName).collect(Collectors.joining(", ")))
                .getBooleanValue()) {
            throw new NutsUserCancelException(getSession(), NutsMessage.cstyle("installation cancelled: %s", nonIgnored.stream().map(NutsId::getFullName).collect(Collectors.joining(", "))));
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
                        _LOGOP(session).error(ex).verb(NutsLogVerb.WARNING).level(Level.FINE).formatted().log("failed to install {0}", info.id);
                        failedList.add(info.id);
                        if (session.isPlainTrace()) {
                            if (!ws.term().setSession(getSession()).getTerminal().ask()
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

    private void printList(NutsPrintStream out, NutsString kind, NutsString action, List<NutsId> all) {
        if (all.size() > 0) {
            NutsWorkspace ws = getSession().getWorkspace();
            NutsTextBuilder msg = ws.text().builder();
            msg.append("the following ")
                    .append(kind).append(" ").append((all.size() > 1 ? "artifacts are" : "artifact is"))
                    .append(" going to be ").append(action).append(" : ")
                    .appendJoined(
                            ws.text().forPlain(", "),
                            all.stream().map(x
                                            -> ws.text().toText(
                                            x.builder().omitImportedGroupId().build()
                                    )
                            ).collect(Collectors.toList())
                    );
            out.resetLine().println(msg);
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
            return id.builder().setNamespace(null).setProperty("optional", null).build().toString();
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
