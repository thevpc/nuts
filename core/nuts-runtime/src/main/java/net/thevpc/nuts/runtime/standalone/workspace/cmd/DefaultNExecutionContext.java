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
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.collections.NCollections;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNExecutionContext implements NExecutionContext {

    private final NDefinition definition;
    private final Map<String, String> env;
    private final List<String> executorOptions;
    private final List<String> workspaceOptions;
    private final List<String> arguments;
    private NSession session;
    private final NArtifactCall executorDescriptor;
    private final NPath cwd;
    private final String commandName;
    private final boolean failFast;
    private final boolean temporary;
    private final NDuration sleepDuration;
    private final NExecutionType executionType;
    private NRunAs runAs;
    private NExecInput in;
    private NExecOutput out;
    private NExecOutput err;
    private boolean dry;
    private final boolean bot;
    private final NDefinition runner;

    public DefaultNExecutionContext(NDefinition definition,
                                    List<String> arguments, List<String> executorArgs, List<String> workspaceOptions, Map<String, String> env,
                                    NPath cwd, boolean failFast,
                                    boolean temporary,
                                    NExecutionType executionType,
                                    String commandName,
                                    NDuration sleepDuration,
                                    NExecInput in,
                                    NExecOutput out,
                                    NExecOutput err,
                                    boolean dry,
                                    boolean bot,
                                    NDefinition runner,
                                    NRunAs runAs,NSession session
    ) {
        this.commandName = commandName;
        this.definition = definition;
        this.arguments = NCollections.unmodifiableList(arguments);
        this.executorOptions = NCollections.unmodifiableList(executorArgs);
        this.workspaceOptions = NCollections.unmodifiableList(workspaceOptions);
        this.sleepDuration = sleepDuration;
        this.cwd = cwd;
        if (env == null) {
            env = new LinkedHashMap<>();
        }
        this.env = env;
        this.failFast = failFast;
        this.temporary = temporary;
        this.executionType = executionType;
        this.executorDescriptor = definition.descriptor().executor();
        this.in = in;
        this.out = out;
        this.err = err;
        this.dry = dry;
        this.bot = bot;
        this.runner = runner;
        this.runAs = runAs;
        this.session = session;
    }

    public DefaultNExecutionContext(NExecutionContext other) {
        this.commandName = other.commandName();
        this.definition = other.definition();
        this.arguments = other.arguments();
        this.session = other.session();
        this.executorOptions = other.executorOptions();
        this.workspaceOptions = other.workspaceOptions();
        this.cwd = other.directory();
        this.env = other.env();
        this.failFast = other.isFailFast();
        this.temporary = other.isTemporary();
        this.executionType = other.executionType();
        this.executorDescriptor = other.executorDescriptor();
        this.sleepDuration = other.sleepDuration();
        this.in = other.in();
        this.out = other.out();
        this.err = other.err();
        this.dry = other.isDry();
        this.bot = other.isBot();
        this.runner = other.runner();
        this.runAs = other.runAs();
    }

    public boolean isDry() {
        return dry;
    }

    public NExecutionContext dry(boolean dry) {
        this.dry = dry;
        return this;
    }

    @Override
    public boolean isBot() {
        return bot;
    }

    public NExecInput in() {
        return in;
    }

    public DefaultNExecutionContext in(NExecInput in) {
        this.in = in;
        return this;
    }

    public NExecOutput out() {
        return out;
    }

    public DefaultNExecutionContext out(NExecOutput out) {
        this.out = out;
        return this;
    }

    public NExecOutput err() {
        return err;
    }

    public DefaultNExecutionContext err(NExecOutput err) {
        this.err = err;
        return this;
    }


    public NDuration sleepDuration() {
        return sleepDuration;
    }

    @Override
    public String commandName() {
        return commandName;
    }

    @Override
    public List<String> executorOptions() {
        return executorOptions;
    }

    @Override
    public List<String> workspaceOptions() {
        return workspaceOptions;
    }

    @Override
    public NDefinition definition() {
        return definition;
    }

    @Override
    public NDefinition runner() {
        return runner;
    }

    @Override
    public List<String> arguments() {
        return arguments;
    }

    @Override
    public NArtifactCall executorDescriptor() {
        return executorDescriptor;
    }

    @Override
    public NSession session() {
        return session;
    }

    @Override
    public Map<String, String> env() {
        return env;
    }

    @Override
    public NPath directory() {
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
    public NExecutionType executionType() {
        return executionType;
    }


    @Override
    public NRunAs runAs() {
        return runAs;
    }

    public NExecutionContextBuilder builder() {
        return new DefaultNExecutionContextBuilder(this);
    }
}
