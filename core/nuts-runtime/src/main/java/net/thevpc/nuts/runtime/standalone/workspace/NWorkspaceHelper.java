/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.app.NApplicationHandleMode;
import net.thevpc.nuts.app.NApplications;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.boot.NBootWorkspaceImpl;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.NBootOptions;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NRepositoryFilter;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.runtime.standalone.DefaultNBootOptionsBuilder;
import net.thevpc.nuts.text.NI18n;
import net.thevpc.nuts.text.NMsg;

import java.util.*;
import java.util.logging.Level;

/**
 *
 * @author thevpc
 */
public class NWorkspaceHelper {
    public static NFetchStrategy validate(NFetchStrategy mode) {
        return mode == null ? NFetchStrategy.ONLINE : mode;
    }

    public static List<NRepository> _getEnabledRepositories(NRepository parent, NRepositoryFilter repositoryFilter) {
        List<NRepository> repos = new ArrayList<>();
        if (parent.config().isSupportedMirroring()) {
            List<NRepository> subrepos = new ArrayList<>();
            boolean ok = false;
            for (NRepository repository : parent.config().getMirrors()) {
                if (repository.isEnabled()) {
                    if (repositoryFilter == null || repositoryFilter.acceptRepository(repository)) {
                        repos.add(repository);
                        ok = true;
                    }
                    if (!ok) {
                        subrepos.add(repository);
                    }
                }
            }
            for (NRepository subrepo : subrepos) {
                repos.addAll(_getEnabledRepositories(subrepo, repositoryFilter));
            }
        }
        return repos;
    }

    public static void runBootCommand(NWorkspace workspace) {
        workspace.runWith(() -> {
            NBootOptions info2 = new DefaultNBootOptionsBuilder(((NWorkspaceExt)workspace).getCallerBootOptionsInfo()).build();
            NApp.of().setId(workspace.getApiId());
            NLog LOG = NLog.of(NBootWorkspaceImpl.class);
            LOG.log(NMsg.ofC("running workspace in %s mode", getRunModeString(info2))
                    .withLevel(Level.CONFIG).withIntent(NMsgIntent.SUCCESS)
            );
            NExec execCmd = NExec.of()
                    .setExecutionType(info2.getExecutionType().orNull())
                    .setRunAs(info2.getRunAs().orNull())
                    .failFast();
            List<String> executorOptions = info2.getExecutorOptions().orNull();
            if (executorOptions != null) {
                execCmd.configure(true, executorOptions.toArray(new String[0]));
            }
            NCmdLine executorOptionsCmdLine = NCmdLine.of(executorOptions).setExpandSimpleOptions(false);
            while (executorOptionsCmdLine.hasNext()) {
                execCmd.configureLast(executorOptionsCmdLine);
            }
            if (info2.getApplicationArguments().get().isEmpty()) {
                if (info2.getSkipWelcome().orElse(false)) {
                    return;
                }
                execCmd.addCommand("welcome");
            } else {
                execCmd.addCommand(info2.getApplicationArguments().get());
            }
            execCmd.run();
        });

    }
    public static void runApplication(NWorkspace workspace,NApplicationHandleMode handleMode) {
        NApplicationHandleMode.runHandled(() ->
                workspace.runWith(() -> {
                    boolean inherited = NWorkspace.of().getBootOptions().getInherited().orElse(false);
                    NApp nApp = NApp.of();
                    // Resolve the application class name (explicit or fallback)
                    String appClassName = nApp.getSourceType() == null ? null : nApp.getSourceType().getName();
                    if (appClassName == null) {
                        appClassName = nApp.getSource() == null ? null : nApp.getSource().getClass().getName();
                    }
                    NId appId = nApp.getId().orNull();
                    NLog.of(NApplications.class)
                            .log(
                                    NMsg.ofC(
                                            NI18n.of("running application %s: %s (%s) %s"),
                                            inherited ? ("(" + NI18n.of("inherited") + ")") : "",
                                            appId == null ? ("<" + NI18n.of("unresolved-id") + ">") : appId,
                                            appClassName,
                                            nApp.getCmdLine()
                                    ).asFine().withIntent(NMsgIntent.START)
                            );
                    try {
                        switch (nApp.getMode()) {
                            //both RUN and AUTO_COMPLETE execute the run branch. Later
                            //session.isExecMode()
                            case RUN:
                            case AUTO_COMPLETE: {
                                nApp.getApplication().run();
                                return;
                            }
                            case INSTALL: {
                                nApp.getApplication().onInstallApplication();
                                return;
                            }
                            case UPDATE: {
                                nApp.getApplication().onUpdateApplication();
                                return;
                            }
                            case UNINSTALL: {
                                nApp.getApplication().onUninstallApplication();
                                return;
                            }
                        }
                    } catch (NExecutionException e) {
                        if (e.getExitCode() == NExecutionException.SUCCESS) {
                            return;
                        }
                        throw e;
                    }
                    throw new NExecutionException(NMsg.ofC(NI18n.of("unsupported execution mode %s"), nApp.getMode()), NExecutionException.ERROR_255);
                }), handleMode
        );
    }

    protected static String getRunModeString(NBootOptions options) {
        if (options.getReset().orElse(false)) {
            return "reset";
        } else if (options.getRecover().orElse(false)) {
            return "recover";
        } else {
            return "exec";
        }
    }
}
