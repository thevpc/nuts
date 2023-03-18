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
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;

import java.util.*;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNExecutionContextBuilder implements NExecutionContextBuilder {

    private NDefinition definition;
    private Map<String, String> env;
    private List<String> executorOptions = new ArrayList<>();
    private List<String> workspaceOptions = new ArrayList<>();
    private Map<String, String> executorProperties = new LinkedHashMap<>();
    private List<String> arguments;
    private NSession execSession;
    private NSession session;
    private NWorkspace workspace;
    private NArtifactCall executorDescriptor;
    private NPath cwd;
    private String commandName;
    private boolean failFast;
    private boolean temporary;
    private long sleepMillis = 1000;
    private boolean inheritSystemIO;
    private NPath redirectOutputFile;
    private NPath redirectInputFile;

    private NExecutionType executionType;
    private NRunAs runAs = NRunAs.currentUser();

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
    public DefaultNExecutionContextBuilder() {
    }

    public DefaultNExecutionContextBuilder(NDefinition definition,
                                           List<String> arguments, List<String> executorArgs, Map<String, String> env, Map<String, String> executorProperties,
                                           NPath cwd, NSession session, NSession execSession, NWorkspace workspace, boolean failFast,
                                           boolean temporary,
                                           NExecutionType executionType,
                                           String commandName,
                                           long sleepMillis
    ) {
        if (executorProperties == null) {
            executorProperties = new LinkedHashMap<>();
        }
        this.commandName = commandName;
        this.definition = definition;
        this.arguments = CoreCollectionUtils.nonNullList(arguments);
        this.execSession = execSession;
        this.session = session;
        this.workspace = workspace;
        this.executorOptions = CoreCollectionUtils.nonNullList(executorArgs);
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
    }

    public DefaultNExecutionContextBuilder(NExecutionContext other) {
        this.commandName = other.getCommandName();
        this.definition = other.getDefinition();
        this.arguments = CoreCollectionUtils.nonNullList(other.getArguments());
        this.execSession = other.getExecSession();
        this.session = other.getSession();
        this.workspace = other.getWorkspace();
        this.executorOptions.addAll(CoreCollectionUtils.nonNullList(other.getExecutorOptions()));
        this.executorProperties.putAll(CoreCollectionUtils.nonNullMap(other.getExecutorProperties()));
        this.cwd = other.getDirectory();
        this.env = other.getEnv();
        this.failFast = other.isFailFast();
        this.temporary = other.isTemporary();
        this.executionType = other.getExecutionType();
        this.executorDescriptor = other.getExecutorDescriptor();
        this.sleepMillis = other.getSleepMillis();
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    public long getSleepMillis() {
        return sleepMillis;
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions.toArray(new String[0]);
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
    public NPath getDirectory() {
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

    @Override
    public NRunAs getRunAs() {
        return runAs;
    }

    @Override
    public NExecutionContextBuilder setDefinition(NDefinition definition) {
        this.definition = definition;
        return this;
    }

    public NExecutionContextBuilder setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    @Override
    public NExecutionContextBuilder setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    @Override
    public NExecutionContextBuilder setExecutorOptions(List<String> executorOptions) {
        this.executorOptions.clear();
        if (executorOptions != null) {
            this.executorOptions.addAll(CoreCollectionUtils.nonNullList(executorOptions));
        }
        return this;
    }

    @Override
    public NExecutionContextBuilder setWorkspaceOptions(List<String> workspaceOptions) {
        this.workspaceOptions.clear();
        if (workspaceOptions != null) {
            this.workspaceOptions.addAll(CoreCollectionUtils.nonNullList(workspaceOptions));
        }
        return this;
    }

    @Override
    public NExecutionContextBuilder setExecutorOptions(String[] executorOptions) {
        this.executorOptions.clear();
        if (executorOptions != null) {
            this.executorOptions.addAll(Arrays.asList(executorOptions));
        }
        return this;
    }

    @Override
    public NExecutionContextBuilder addExecutorOptions(List<String> executorOptions) {
        if (executorOptions != null) {
            this.executorOptions.addAll(executorOptions);
        }
        return this;
    }

    @Override
    public NExecutionContextBuilder addExecutorOptions(String[] executorOptions) {
        if (executorOptions != null) {
            this.executorOptions.addAll(Arrays.asList(executorOptions));
        }
        return this;
    }

    @Override
    public NExecutionContextBuilder addExecutorProperties(Map<String, String> executorProperties) {
        if (executorProperties != null) {
            this.executorProperties.putAll(executorProperties);
        }
        return this;
    }

    @Override
    public NExecutionContextBuilder setExecutorProperties(Map<String, String> executorProperties) {
        this.executorProperties.clear();
        if (executorProperties != null) {
            this.executorProperties.putAll(executorProperties);
        }
        return this;
    }

    @Override
    public NExecutionContextBuilder setArguments(String[] arguments) {
        this.arguments = CoreCollectionUtils.nonNullList(Arrays.asList(arguments));
        return this;
    }

    @Override
    public NExecutionContextBuilder setExecSession(NSession execSession) {
        this.execSession = execSession;
        return this;
    }

    @Override
    public NExecutionContextBuilder setSession(NSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NExecutionContextBuilder setWorkspace(NWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NExecutionContextBuilder setExecutorDescriptor(NArtifactCall executorDescriptor) {
        this.executorDescriptor = executorDescriptor;
        return this;
    }

    @Override
    public NExecutionContextBuilder setDirectory(NPath cwd) {
        this.cwd = cwd;
        return this;
    }

    @Override
    public NExecutionContextBuilder setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public NExecutionContextBuilder setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NExecutionContextBuilder setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    @Override
    public NExecutionContextBuilder setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NExecutionContextBuilder setRunAs(NRunAs runAs) {
        this.runAs = runAs == null ? NRunAs.currentUser() : runAs;
        return this;
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

    public NExecutionContextBuilder setInheritSystemIO(boolean inheritSystemIO) {
        this.inheritSystemIO = inheritSystemIO;
        return this;
    }

    public NExecutionContextBuilder setRedirectOutputFile(NPath redirectOutputFile) {
        this.redirectOutputFile = redirectOutputFile;
        return this;
    }

    public NExecutionContextBuilder setRedirectInputFile(NPath redirectInputFile) {
        this.redirectInputFile = redirectInputFile;
        return this;
    }

    @Override
    public NExecutionContext build() {
        return new DefaultNExecutionContext(
                definition, arguments, executorOptions, workspaceOptions, env, executorProperties, cwd, session, execSession,
                workspace, failFast, temporary, executionType,
                commandName, sleepMillis, inheritSystemIO, redirectOutputFile, redirectInputFile
        );
    }

    public NExecutionContextBuilder setAll(NExecutionContext other) {
        this.commandName = other.getCommandName();
        this.definition = other.getDefinition();
        this.arguments = other.getArguments();
        this.execSession = other.getExecSession();
        this.session = other.getSession();
        this.workspace = other.getWorkspace();
        this.executorOptions.clear();
        this.executorOptions.addAll(other.getExecutorOptions());
        this.workspaceOptions.clear();
        this.workspaceOptions.addAll(other.getWorkspaceOptions());
        this.executorProperties = other.getExecutorProperties();
        this.cwd = other.getDirectory();
        this.env = other.getEnv();
        this.failFast = other.isFailFast();
        this.temporary = other.isTemporary();
        this.executionType = other.getExecutionType();
        this.executorDescriptor = other.getExecutorDescriptor();
        this.sleepMillis = other.getSleepMillis();
        this.inheritSystemIO = other.isInheritSystemIO();
        this.redirectOutputFile = other.getRedirectOutputFile();
        this.redirectInputFile = other.getRedirectInputFile();
        return this;
    }
}
