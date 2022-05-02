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
import net.thevpc.nuts.reserved.NutsReservedCollectionUtils;

import java.util.*;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNutsExecutionContextBuilder implements NutsExecutionContextBuilder {

    private NutsDefinition definition;
    private Map<String, String> env;
    private List<String> executorOptions = new ArrayList<>();
    private List<String> workspaceOptions = new ArrayList<>();
    private Map<String, String> executorProperties = new LinkedHashMap<>();
    private List<String> arguments;
    private NutsSession execSession;
    private NutsSession session;
    private NutsWorkspace workspace;
    private NutsArtifactCall executorDescriptor;
    private String cwd;
    private String commandName;
    private boolean failFast;
    private boolean temporary;
    private long sleepMillis = 1000;
    private boolean inheritSystemIO;
    private String redirectOutputFile;
    private String redirectInputFile;

    private NutsExecutionType executionType;
    private NutsRunAs runAs = NutsRunAs.currentUser();

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
    public DefaultNutsExecutionContextBuilder() {
    }

    public DefaultNutsExecutionContextBuilder(NutsDefinition definition,
                                              List<String> arguments, List<String> executorArgs, Map<String, String> env, Map<String, String> executorProperties,
                                              String cwd, NutsSession session, NutsSession execSession, NutsWorkspace workspace, boolean failFast,
                                              boolean temporary,
                                              NutsExecutionType executionType,
                                              String commandName,
                                              long sleepMillis
    ) {
        if (executorProperties == null) {
            executorProperties = new LinkedHashMap<>();
        }
        this.commandName = commandName;
        this.definition = definition;
        this.arguments = NutsReservedCollectionUtils.nonNullList(arguments);
        this.execSession = execSession;
        this.session = session;
        this.workspace = workspace;
        this.executorOptions = NutsReservedCollectionUtils.nonNullList(executorArgs);
        this.executorProperties = NutsReservedCollectionUtils.unmodifiableMap(executorProperties);
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

    public DefaultNutsExecutionContextBuilder(NutsExecutionContext other) {
        this.commandName = other.getCommandName();
        this.definition = other.getDefinition();
        this.arguments = NutsReservedCollectionUtils.nonNullList(other.getArguments());
        this.execSession = other.getExecSession();
        this.session = other.getSession();
        this.workspace = other.getWorkspace();
        this.executorOptions.addAll(NutsReservedCollectionUtils.nonNullList(other.getExecutorOptions()));
        this.executorProperties.putAll(NutsReservedCollectionUtils.nonNullMap(other.getExecutorProperties()));
        this.cwd = other.getCwd();
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
    public NutsDefinition getDefinition() {
        return definition;
    }

    @Override
    public List<String> getArguments() {
        return arguments;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsArtifactCall getExecutorDescriptor() {
        return executorDescriptor;
    }

    @Override
    public NutsSession getExecSession() {
        return execSession;
    }

    @Override
    public NutsSession getSession() {
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
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    @Override
    public NutsRunAs getRunAs() {
        return runAs;
    }

    @Override
    public NutsExecutionContextBuilder setDefinition(NutsDefinition definition) {
        this.definition = definition;
        return this;
    }

    public NutsExecutionContextBuilder setSleepMillis(long sleepMillis) {
        this.sleepMillis = sleepMillis;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setExecutorOptions(List<String> executorOptions) {
        this.executorOptions.clear();
        if (executorOptions != null) {
            this.executorOptions.addAll(NutsReservedCollectionUtils.nonNullList(executorOptions));
        }
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setWorkspaceOptions(List<String> workspaceOptions) {
        this.workspaceOptions.clear();
        if (workspaceOptions != null) {
            this.workspaceOptions.addAll(NutsReservedCollectionUtils.nonNullList(workspaceOptions));
        }
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setExecutorOptions(String[] executorOptions) {
        this.executorOptions.clear();
        if (executorOptions != null) {
            this.executorOptions.addAll(Arrays.asList(executorOptions));
        }
        return this;
    }

    @Override
    public NutsExecutionContextBuilder addExecutorOptions(List<String> executorOptions) {
        if (executorOptions != null) {
            this.executorOptions.addAll(executorOptions);
        }
        return this;
    }

    @Override
    public NutsExecutionContextBuilder addExecutorOptions(String[] executorOptions) {
        if (executorOptions != null) {
            this.executorOptions.addAll(Arrays.asList(executorOptions));
        }
        return this;
    }

    @Override
    public NutsExecutionContextBuilder addExecutorProperties(Map<String, String> executorProperties) {
        if (executorProperties != null) {
            this.executorProperties.putAll(executorProperties);
        }
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setExecutorProperties(Map<String, String> executorProperties) {
        this.executorProperties.clear();
        if (executorProperties != null) {
            this.executorProperties.putAll(executorProperties);
        }
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setArguments(String[] arguments) {
        this.arguments = NutsReservedCollectionUtils.nonNullList(Arrays.asList(arguments));
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setExecSession(NutsSession execSession) {
        this.execSession = execSession;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setExecutorDescriptor(NutsArtifactCall executorDescriptor) {
        this.executorDescriptor = executorDescriptor;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setCwd(String cwd) {
        this.cwd = cwd;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public NutsExecutionContextBuilder setRunAs(NutsRunAs runAs) {
        this.runAs = runAs == null ? NutsRunAs.currentUser() : runAs;
        return this;
    }

    public boolean isInheritSystemIO() {
        return inheritSystemIO;
    }

    public String getRedirectOutputFile() {
        return redirectOutputFile;
    }

    public String getRedirectInputFile() {
        return redirectInputFile;
    }

    public NutsExecutionContextBuilder setInheritSystemIO(boolean inheritSystemIO) {
        this.inheritSystemIO = inheritSystemIO;
        return this;
    }

    public NutsExecutionContextBuilder setRedirectOutputFile(String redirectOutputFile) {
        this.redirectOutputFile = redirectOutputFile;
        return this;
    }

    public NutsExecutionContextBuilder setRedirectInputFile(String redirectInputFile) {
        this.redirectInputFile = redirectInputFile;
        return this;
    }

    @Override
    public NutsExecutionContext build() {
        return new DefaultNutsExecutionContext(
                definition, arguments, executorOptions, workspaceOptions, env, executorProperties, cwd, session, execSession,
                workspace, failFast, temporary, executionType,
                commandName, sleepMillis, inheritSystemIO, redirectOutputFile, redirectInputFile
        );
    }

    public NutsExecutionContextBuilder setAll(NutsExecutionContext other) {
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
        return this;
    }
}
