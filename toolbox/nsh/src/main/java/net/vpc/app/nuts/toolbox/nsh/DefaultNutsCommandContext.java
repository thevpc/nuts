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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.common.commandline.CommandAutoComplete;
import net.vpc.common.io.FileUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultNutsCommandContext implements NutsCommandContext {

    private String serviceName;
    private NutsWorkspace workspace;
    private NutsConsole console;
    private NutsSession session;
    private Map<String, Object> userProperties = new HashMap<>();
    private Properties env = new Properties();
    private CommandAutoComplete autoComplete;

    public DefaultNutsCommandContext() {
    }

    public DefaultNutsCommandContext(NutsWorkspace ws) {
        setWorkspace(ws);
        setSession(ws==null?null:ws.createSession());
    }

    public NutsCommandContext copy() {
        DefaultNutsCommandContext c = new DefaultNutsCommandContext();
        c.serviceName = serviceName;
        c.workspace = workspace;
        c.console = console;
        c.session = session.copy();
        c.env = new Properties();
        c.env.putAll(env);
        c.userProperties = new HashMap<>();
        c.userProperties.putAll(userProperties);
        return c;
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public NutsCommandContext setEnv(Properties env) {
        this.env = new Properties();
        if (env != null) {
            this.env.putAll(env);
        }
        return this;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    @Override
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public NutsCommandContext setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsCommandContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsTerminal getTerminal() {
        return session.getTerminal();
    }

    @Override
    public NutsConsole getConsole() {
        return console;
    }

    @Override
    public String[] expandPath(String path) {
        return FileUtils.expandPath(path,new File(getCwd()));
    }

    @Override
    public String getCwd() {
        return console.getCwd();
    }

    @Override
    public void setCwd(String path) {
        console.setCwd(path);
    }

    @Override
    public String getAbsolutePath(String path) {
        return FileUtils.getAbsolutePath(new File(getCwd()), path);
    }

    @Override
    public void setConsole(NutsConsole console) {
        this.console = console;
    }

    @Override
    public NutsWorkspace getValidWorkspace() {
        if (workspace == null) {
            throw new NutsIllegalArgumentException("No valid Workspace openWorkspace");
        }
        return workspace;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
    }

    @Override
    public CommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public void setAutoComplete(CommandAutoComplete autoComplete) {
        this.autoComplete = autoComplete;
    }

    @Override
    public PrintStream getFormattedOut() {
        return getTerminal().getFormattedOut();
    }

    @Override
    public PrintStream getFormattedErr() {
        return getTerminal().getFormattedErr();
    }
    @Override
    public PrintStream getOut() {
        return getTerminal().getOut();
    }

    @Override
    public PrintStream getErr() {
        return getTerminal().getErr();
    }
}
