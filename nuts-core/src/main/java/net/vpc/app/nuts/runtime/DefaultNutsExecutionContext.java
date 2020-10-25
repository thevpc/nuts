/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2020 thevpc
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.*;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by vpc on 1/15/17.
 */
public class DefaultNutsExecutionContext implements NutsExecutionContext {

    private NutsDefinition definition;
    private Map<String, String> env;
    private String[] executorOptions;
    private Map<String, String> executorProperties;
    private String[] arguments;
    private NutsSession execSession;
    private NutsSession traceSession;
    private NutsWorkspace workspace;
    private NutsArtifactCall executorDescriptor;
    private String cwd;
    private String commandName;
    private boolean failFast;
    private boolean temporary;
    private NutsExecutionType executionType;

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
    public DefaultNutsExecutionContext(NutsDefinition definition,
                                       String[] arguments, String[] executorArgs, Map<String, String> env, Map<String, String> executorProperties,
                                       String cwd, NutsSession traceSession, NutsSession execSession, NutsWorkspace workspace, boolean failFast,
                                       boolean temporary,
                                       NutsExecutionType executionType,
                                       String commandName) {
        if (arguments == null) {
            arguments = new String[0];
        }
        if (executorArgs == null) {
            executorArgs = new String[0];
        }
        if (executorProperties == null) {
            executorProperties = new LinkedHashMap<>();
        }
        this.commandName = commandName;
        this.definition = definition;
        this.arguments = arguments;
        this.execSession = execSession;
        this.traceSession = traceSession;
        this.workspace = workspace;
        this.executorOptions = executorArgs;
        this.executorProperties = executorProperties;
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

    public DefaultNutsExecutionContext(NutsExecutionContext other) {
        this.commandName = other.getCommandName();
        this.definition = other.getDefinition();
        this.arguments = other.getArguments();
        this.execSession = other.getExecSession();
        this.traceSession = other.getTraceSession();
        this.workspace = other.getWorkspace();
        this.executorOptions = other.getExecutorOptions();
        this.executorProperties = other.getExecutorProperties();
        this.cwd = other.getCwd();
        this.env = other.getEnv();
        this.failFast = other.isFailFast();
        this.temporary = other.isTemporary();
        this.executionType = other.getExecutionType();
        this.executorDescriptor = other.getExecutorDescriptor();
    }

    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public String[] getExecutorOptions() {
        return executorOptions;
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
    public String[] getArguments() {
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
    public NutsSession getTraceSession() {
        return traceSession;
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

    public DefaultNutsExecutionContext setDefinition(NutsDefinition definition) {
        this.definition = definition;
        return this;
    }

    public DefaultNutsExecutionContext setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public DefaultNutsExecutionContext setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    public DefaultNutsExecutionContext setExecutorProperties(Map<String, String> executorProperties) {
        this.executorProperties = executorProperties;
        return this;
    }

    public DefaultNutsExecutionContext setArguments(String[] arguments) {
        this.arguments = arguments;
        return this;
    }

    public DefaultNutsExecutionContext setExecSession(NutsSession execSession) {
        this.execSession = execSession;
        return this;
    }

    public DefaultNutsExecutionContext setTraceSession(NutsSession traceSession) {
        this.traceSession = traceSession;
        return this;
    }

    public DefaultNutsExecutionContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public DefaultNutsExecutionContext setExecutorDescriptor(NutsArtifactCall executorDescriptor) {
        this.executorDescriptor = executorDescriptor;
        return this;
    }

    public DefaultNutsExecutionContext setCwd(String cwd) {
        this.cwd = cwd;
        return this;
    }

    public DefaultNutsExecutionContext setCommandName(String commandName) {
        this.commandName = commandName;
        return this;
    }

    public DefaultNutsExecutionContext setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    public DefaultNutsExecutionContext setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    public DefaultNutsExecutionContext setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }
}
