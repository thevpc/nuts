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
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNExecutionContext implements NExecutionContext {

    private NDefinition definition;
    private Map<String, String> env;
    private List<String> executorOptions;
    private List<String> workspaceOptions;
    private Map<String, String> executorProperties;
    private List<String> arguments;
    private NSession execSession;
    private NSession session;
    private NWorkspace workspace;
    private NArtifactCall executorDescriptor;
    private String cwd;
    private String commandName;
    private boolean failFast;
    private boolean temporary;
    private long sleepMillis;
    private NExecutionType executionType;
    private NRunAs runAs;
    private boolean inheritSystemIO;
    private NPath redirectOutputFile;
    private NPath redirectInputFile;

    //    public NutsExecutionContextImpl(NutsDefinition nutsDefinition, NutsSession session, NutsWorkspace workspace,String cwd) {
//        this.nutsDefinition = nutsDefinition;
//        this.session = session;
//        if (nutsDefinition != null && nutsDefinition.getDescriptor() != null && nutsDefinition.getDescriptor().getInstaller() != null) {
//            NutsExecutorDescriptor ii = nutsDefinition.getDescriptor().getInstaller();
//            executorOptions = ii.getArguments();
//            executorProperties = ii.getProperties();
//        }
//        this.workspace = workspace;
//        if (args == null) {
//            args = new String[0];
//        }
//        if (executorOptions == null) {
//            executorOptions = new String[0];
//        }
//        if (executorProperties == null) {
//            executorProperties = new Properties();
//        }
//        this.cwd = cwd;
//    }
    public DefaultNExecutionContext(NDefinition definition,
                                    List<String> arguments, List<String> executorArgs, List<String> workspaceOptions, Map<String, String> env, Map<String, String> executorProperties,
                                    String cwd, NSession session, NSession execSession, NWorkspace workspace, boolean failFast,
                                    boolean temporary,
                                    NExecutionType executionType,
                                    String commandName,
                                    long sleepMillis,
                                    boolean inheritSystemIO,
                                    NPath redirectOutputFile,
                                    NPath redirectInputFile
    ) {
        this.commandName = commandName;
        this.definition = definition;
        this.arguments = CoreCollectionUtils.unmodifiableList(arguments);
        this.execSession = execSession;
        this.session = session;
        this.workspace = workspace;
        this.executorOptions = CoreCollectionUtils.unmodifiableList(executorArgs);
        this.workspaceOptions = CoreCollectionUtils.unmodifiableList(workspaceOptions);
        this.executorProperties = CoreCollectionUtils.unmodifiableMap(executorProperties);
        this.sleepMillis = sleepMillis;
        this.cwd = cwd;
        if (env == null) {
            env = new LinkedHashMap<>();
        }
        this.env = env;
        this.failFast = failFast;
        this.temporary = temporary;
        this.executionType = executionType;
        this.executorDescriptor = definition.getDescriptor().getExecutor();
        this.inheritSystemIO = inheritSystemIO;
        this.redirectOutputFile = redirectOutputFile;
        this.redirectInputFile = redirectInputFile;
    }

    public DefaultNExecutionContext(NExecutionContext other) {
        this.commandName = other.getCommandName();
        this.definition = other.getDefinition();
        this.arguments = other.getArguments();
        this.execSession = other.getExecSession();
        this.session = other.getSession();
        this.workspace = other.getWorkspace();
        this.executorOptions = other.getExecutorOptions();
        this.executorProperties = other.getExecutorProperties();
        this.workspaceOptions = other.getWorkspaceOptions();
        this.cwd = other.getCwd();
        this.env = other.getEnv();
        this.failFast = other.isFailFast();
        this.temporary = other.isTemporary();
        this.executionType = other.getExecutionType();
        this.executorDescriptor = other.getExecutorDescriptor();
        this.sleepMillis = other.getSleepMillis();
        this.inheritSystemIO = other.isInheritSystemIO();
        this.redirectOutputFile = other.getRedirectOutputFile();
        this.redirectInputFile = other.getRedirectInputFile();
    }

    public boolean isInheritSystemIO() {
        return inheritSystemIO;
    }

    public NPath getRedirectOutputFile() {
        return redirectOutputFile;
    }

    public NPath getRedirectInputFile() {
        return redirectInputFile;
    }
    

    public long getSleepMillis() {
        return sleepMillis;
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public List<String> getExecutorOptions() {
        return executorOptions;
    }

    @Override
    public List<String> getWorkspaceOptions() {
        return workspaceOptions;
    }

    @Override
    public Map<String, String> getExecutorProperties() {
        return executorProperties;
    }

    @Override
    public NDefinition getDefinition() {
        return definition;
    }

    @Override
    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NArtifactCall getExecutorDescriptor() {
        return executorDescriptor;
    }

    @Override
    public NSession getExecSession() {
        return execSession;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public Map<String, String> getEnv() {
        return env;
    }

    @Override
    public String getCwd() {
        return cwd;
    }

    public boolean isFailFast() {
        return failFast;
    }

    @Override
    public boolean isTemporary() {
        return temporary;
    }

    @Override
    public NExecutionType getExecutionType() {
        return executionType;
    }

    public DefaultNExecutionContext setDefinition(NDefinition definition) {
        this.definition = definition;
        return this;
    }

    public DefaultNExecutionContext setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public DefaultNExecutionContext setExecutorOptions(String[] executorOptions) {
        this.executorOptions = CoreCollectionUtils.unmodifiableList(Arrays.asList(executorOptions));
        return this;
    }

    public DefaultNExecutionContext setExecutorProperties(Map<String, String> executorProperties) {
        this.executorProperties = executorProperties;
        return this;
    }

    public DefaultNExecutionContext setArguments(String[] arguments) {
        this.arguments = CoreCollectionUtils.unmodifiableList(Arrays.asList(arguments));
        return this;
    }

    public DefaultNExecutionContext setExecSession(NSession execSession) {
        this.execSession = execSession;
        return this;
    }

    public DefaultNExecutionContext setSession(NSession session) {
        this.session = session;
        return this;
    }

    public DefaultNExecutionContext setWorkspace(NWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public DefaultNExecutionContext setExecutorDescriptor(NArtifactCall executorDescriptor) {
        this.executorDescriptor = executorDescriptor;
        return this;
    }

    public DefaultNExecutionContext setCwd(String cwd) {
        this.cwd = cwd;
        return this;
    }

    public DefaultNExecutionContext setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    public DefaultNExecutionContext setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public DefaultNExecutionContext setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    public DefaultNExecutionContext setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NRunAs getRunAs() {
        return runAs;
    }

    public DefaultNExecutionContext setRunAs(NRunAs runAs) {
        this.runAs = runAs;
        return this;
    }
}
