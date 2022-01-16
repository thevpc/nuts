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
package net.thevpc.nuts.runtime.standalone.executor.zip;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.executor.embedded.ClassloaderAwareRunnable;
import net.thevpc.nuts.runtime.standalone.executor.exec.NutsExecHelper;
import net.thevpc.nuts.runtime.standalone.executor.system.ProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.standalone.io.net.util.NetUtils;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsDebugString;
import net.thevpc.nuts.runtime.standalone.util.collections.StringKeyValueList;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsExecutorComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class ZipExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        execHelper(executionContext).exec();
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        execHelper(executionContext).dryExec();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext ctx) {
        this.ws = ctx.getSession();
        if (ID == null) {
            ID = NutsId.of("net.thevpc.nuts.exec:zip", ws);
        }
        NutsDefinition def = ctx.getConstraints(NutsDefinition.class);
        if (def != null) {
            String shortName = def.getId().getShortName();
            //for executors
            if ("net.thevpc.nuts.exec:exec-zip".equals(shortName)) {
                return DEFAULT_SUPPORT + 10;
            }
            if ("zip".equals(shortName)) {
                return DEFAULT_SUPPORT + 10;
            }
            switch (NutsUtilStrings.trim(def.getDescriptor().getPackaging())) {
                case "zip": {
                    return DEFAULT_SUPPORT + 10;
                }
            }
        }
        return NO_SUPPORT;
    }

    //@Override
    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        NutsDefinition def = executionContext.getDefinition();
        NutsSession session = executionContext.getSession();
        final NutsSession execSession = executionContext.getExecSession();
        StringKeyValueList runnerProps = new StringKeyValueList();
        if (executionContext.getExecutorDescriptor() != null) {
            runnerProps.add(executionContext.getExecutorDescriptor().getProperties());
        }

        if (executionContext.getEnv() != null) {
            runnerProps.add(executionContext.getEnv());
        }

        HashMap<String, String> osEnv = new HashMap<>();
        NutsWorkspaceOptionsBuilder options = ws.boot().getBootOptions().builder();

        //copy session parameters to the newly created workspace
        options.setDry(execSession.isDry());
        options.setGui(execSession.isGui());
        options.setOutLinePrefix(execSession.getOutLinePrefix());
        options.setErrLinePrefix(execSession.getErrLinePrefix());
        options.setDebug(execSession.getDebug());
        options.setTrace(execSession.isTrace());
        options.setBot(execSession.isBot());
        options.setCached(execSession.isCached());
        options.setIndexed(execSession.isIndexed());
        options.setConfirm(execSession.getConfirm());
        options.setTransitive(execSession.isTransitive());
        options.setOutputFormat(execSession.getOutputFormat());
        if (null == options.getTerminalMode()) {
            options.setTerminalMode(execSession.getTerminal().out().mode());
        } else {
            switch (options.getTerminalMode()) {
                //retain filtered
                case FILTERED:
                    break;
                //retain inherited
                case INHERITED:
                    break;
                default:
                    options.setTerminalMode(execSession.getTerminal().out().mode());
                    break;
            }
        }
        options.setExpireTime(execSession.getExpireTime());
        Filter logFileFilter = execSession.getLogFileFilter();
        Filter logTermFilter = execSession.getLogTermFilter();
        Level logTermLevel = execSession.getLogTermLevel();
        Level logFileLevel = execSession.getLogFileLevel();
        if (logFileFilter != null || logTermFilter != null || logTermLevel != null || logFileLevel != null) {
            NutsLogConfig lc = options.getLogConfig();
            if (lc == null) {
                lc = new NutsLogConfig();
            } else {
                lc = lc.copy();
            }
            if (logTermLevel != null) {
                lc.setLogTermLevel(logTermLevel);
            }
            if (logFileLevel != null) {
                lc.setLogFileLevel(logFileLevel);
            }
            if (logTermFilter != null) {
                lc.setLogTermFilter(logTermFilter);
            }
            if (logFileFilter != null) {
                lc.setLogFileFilter(logFileFilter);
            }
        }

        NutsArtifactCall executor = def.getDescriptor().getExecutor();
        if (executor == null) {
            throw new NutsIOException(session, NutsMessage.cstyle("missing executor for %s", def.getId()));
        }
        String[] execArgs = executionContext.getExecutorArguments();
        if (executor.getId() == null || executor.getId().toString().equals("exec")) {
            //accept this
        } else {
            throw new NutsIOException(session, NutsMessage.cstyle("unsupported executor %s for %s", executor.getId(), def.getId()));
        }

        String directory = null;
        return NutsExecHelper.ofDefinition(def,
                execArgs, osEnv, directory, executionContext.getExecutorProperties(), true, true, executionContext.getSleepMillis(), false, false, null, null, executionContext.getRunAs(), executionContext.getSession(),
                executionContext.getExecSession()
        );
    }
}
