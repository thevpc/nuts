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
package net.vpc.app.nuts;

/**
 * Created by vpc on 1/13/17.
 */
public class NutsCommandContext {

    private String serviceName;
    private NutsWorkspace workspace;
    private NutsCommandLineConsoleComponent commandLine;
    private Throwable lastError;
    private Throwable latestError;
    private NutsSession session;

    public NutsCommandContext() {
    }

    public NutsCommandContext copy() {
        NutsCommandContext c = new NutsCommandContext();
        c.serviceName = serviceName;
        c.workspace = workspace;
        c.commandLine = commandLine;
        c.lastError = lastError;
        c.latestError = latestError;
        c.session = session.copy();
        return c;
    }

    public String getServiceName() {
        return serviceName;
    }

    public NutsCommandContext setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsCommandContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsTerminal getTerminal() {
        return session.getTerminal();
    }

    public Throwable getLatestError() {
        return latestError;
    }

    public void setLatestError(Throwable latestError) {
        this.latestError = latestError;
    }

    public Throwable getLastError() {
        return lastError;
    }

    public void setLastError(Throwable lastError) {
        this.lastError = lastError;
    }

    public NutsCommandLineConsoleComponent getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(NutsCommandLineConsoleComponent commandLine) {
        this.commandLine = commandLine;
    }

    public NutsWorkspace getValidWorkspace() {
        if (workspace == null) {
            throw new IllegalArgumentException("No valid Workspace openWorkspace");
        }
        return workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
    }
}
