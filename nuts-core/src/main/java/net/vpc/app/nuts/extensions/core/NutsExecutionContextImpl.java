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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;

import java.util.Properties;

/**
 * Created by vpc on 1/15/17.
 */
public class NutsExecutionContextImpl implements NutsExecutionContext {

    private NutsFile nutsFile;
    private Properties env;
    private String[] execArgs;
    private Properties execProperties;
    private String[] args;
    private NutsSession session;
    private NutsWorkspace workspace;
    private NutsExecutorDescriptor executorDescriptor;

    public NutsExecutionContextImpl(NutsFile nutsFile, NutsSession session, NutsWorkspace workspace) {
        this.nutsFile = nutsFile;
        this.session = session;
        if (nutsFile != null && nutsFile.getDescriptor() != null && nutsFile.getDescriptor().getInstaller() != null) {
            NutsExecutorDescriptor ii = nutsFile.getDescriptor().getInstaller();
            execArgs = ii.getArgs();
            execProperties = ii.getProperties();
        }
        this.workspace = workspace;
        if (args == null) {
            args = new String[0];
        }
        if (execArgs == null) {
            execArgs = new String[0];
        }
        if (execProperties == null) {
            execProperties = new Properties();
        }
    }

    public NutsExecutionContextImpl(NutsFile nutsFile, String[] appArgs, String[] executorArgs, Properties env, Properties execProperties, NutsSession session, NutsWorkspace workspace) {
        if (appArgs == null) {
            appArgs = new String[0];
        }
        if (executorArgs == null) {
            executorArgs = new String[0];
        }
        if (execProperties == null) {
            execProperties = new Properties();
        }
        this.nutsFile = nutsFile;
        this.args = appArgs;
        this.execArgs = executorArgs;
        this.execProperties = execProperties;
        this.workspace = workspace;
        this.session = session;
        if (env == null) {
            env = new Properties();
        }
        this.env = env;
    }

    public NutsExecutionContextImpl(NutsFile nutsFile, String[] args, String[] execArgs, Properties env, Properties execProperties, NutsSession session, NutsWorkspace workspace, NutsExecutorDescriptor executorDescriptor) {
        if (args == null) {
            args = new String[0];
        }
        if (execArgs == null) {
            execArgs = new String[0];
        }
        if (execProperties == null) {
            execProperties = new Properties();
        }
        this.nutsFile = nutsFile;
        this.args = args;
        this.session = session;
        this.workspace = workspace;
        this.executorDescriptor = executorDescriptor;
        this.execArgs = execArgs;
        this.execProperties = execProperties;
        if (env == null) {
            env = new Properties();
        }
        this.env = env;
    }

    @Override
    public String[] getExecArgs() {
        return execArgs;
    }

    @Override
    public Properties getExecProperties() {
        return execProperties;
    }

    @Override
    public NutsFile getNutsFile() {
        return nutsFile;
    }

    @Override
    public String[] getArgs() {
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
    public NutsTerminal getTerminal() {
        return session.getTerminal();
    }

    @Override
    public Properties getEnv() {
        return env;
    }
}
