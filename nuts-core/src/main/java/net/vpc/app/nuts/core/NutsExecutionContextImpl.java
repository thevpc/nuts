/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;

import java.util.Properties;

/**
 * Created by vpc on 1/15/17.
 */
public class NutsExecutionContextImpl implements NutsExecutionContext {

    private NutsDefinition nutsDefinition;
    private Properties env;
    private String[] executorOptions;
    private Properties executorProperties;
    private String[] args;
    private NutsSession session;
    private NutsWorkspace workspace;
    private NutsExecutorDescriptor executorDescriptor;
    private String cwd;
    private String commandName;
    private boolean failFast;
    private boolean temporary;

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
    public NutsExecutionContextImpl(NutsDefinition nutsDefinition, String[] args, String[] executorArgs, Properties env, Properties executorProperties, String cwd, NutsSession session, NutsWorkspace workspace, boolean failFast, boolean temporary, String commandName) {
        if (args == null) {
            args = new String[0];
        }
        if (executorArgs == null) {
            executorArgs = new String[0];
        }
        if (executorProperties == null) {
            executorProperties = new Properties();
        }
        this.commandName = commandName;
        this.nutsDefinition = nutsDefinition;
        this.args = args;
        this.session = session;
        this.workspace = workspace;
        this.executorOptions = executorArgs;
        this.executorProperties = executorProperties;
        this.cwd = cwd;
        if (env == null) {
            env = new Properties();
        }
        this.env = env;
        this.failFast = failFast;
        this.temporary = temporary;
        this.executorDescriptor = nutsDefinition.getDescriptor().getExecutor();
    }

    public boolean isTemporary() {
        return temporary;
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
    public Properties getExecutorProperties() {
        return executorProperties;
    }

    @Override
    public NutsDefinition getDefinition() {
        return nutsDefinition;
    }

    @Override
    public String[] getArguments() {
        return args;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsExecutorDescriptor getExecutorDescriptor() {
        return executorDescriptor;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return session.getTerminal();
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public String getCwd() {
        return cwd;
    }

    public boolean isFailFast() {
        return failFast;
    }

}
