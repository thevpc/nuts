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
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.common.collections.CoreCollectionUtils;

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
    private List<String> arguments;
    private NSession session;
    private NWorkspace workspace;
    private NArtifactCall executorDescriptor;
    private NPath cwd;
    private String commandName;
    private boolean failFast;
    private boolean temporary;
    private long sleepMillis;
    private NExecutionType executionType;
    private NRunAs runAs;
    private NExecInput in;
    private NExecOutput out;
    private NExecOutput err;

    public DefaultNExecutionContext(NDefinition definition,
                                    List<String> arguments, List<String> executorArgs, List<String> workspaceOptions, Map<String, String> env,
                                    NPath cwd, NSession session, NWorkspace workspace, boolean failFast,
                                    boolean temporary,
                                    NExecutionType executionType,
                                    String commandName,
                                    long sleepMillis,
                                    NExecInput in,
                                    NExecOutput out,
                                    NExecOutput err
    ) {
        this.commandName = commandName;
        this.definition = definition;
        this.arguments = CoreCollectionUtils.unmodifiableList(arguments);
        this.session = session;
        this.workspace = workspace;
        this.executorOptions = CoreCollectionUtils.unmodifiableList(executorArgs);
        this.workspaceOptions = CoreCollectionUtils.unmodifiableList(workspaceOptions);
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
        this.in = in;
        this.out = out;
        this.err = err;
    }

    public DefaultNExecutionContext(NExecutionContext other) {
        this.commandName = other.getCommandName();
        this.definition = other.getDefinition();
        this.arguments = other.getArguments();
        this.session = other.getSession();
        this.workspace = other.getWorkspace();
        this.executorOptions = other.getExecutorOptions();
        this.workspaceOptions = other.getWorkspaceOptions();
        this.cwd = other.getDirectory();
        this.env = other.getEnv();
        this.failFast = other.isFailFast();
        this.temporary = other.isTemporary();
        this.executionType = other.getExecutionType();
        this.executorDescriptor = other.getExecutorDescriptor();
        this.sleepMillis = other.getSleepMillis();
        this.in = other.getIn();
        this.out = other.getOut();
        this.err = other.getErr();
    }

    public NExecInput getIn() {
        return in;
    }

    public DefaultNExecutionContext setIn(NExecInput in) {
        this.in = in;
        return this;
    }

    public NExecOutput getOut() {
        return out;
    }

    public DefaultNExecutionContext setOut(NExecOutput out) {
        this.out = out;
        return this;
    }

    public NExecOutput getErr() {
        return err;
    }

    public DefaultNExecutionContext setErr(NExecOutput err) {
        this.err = err;
        return this;
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

    public DefaultNExecutionContext setArguments(String[] arguments) {
        this.arguments = CoreCollectionUtils.unmodifiableList(Arrays.asList(arguments));
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

    public DefaultNExecutionContext setCwd(NPath cwd) {
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
