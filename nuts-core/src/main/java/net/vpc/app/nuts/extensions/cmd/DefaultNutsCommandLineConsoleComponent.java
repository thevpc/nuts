/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.workspaces.DefaultNutsCommandContext;
import net.vpc.apps.javashell.parser.Env;
import net.vpc.apps.javashell.parser.JavaShellEvalContext;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/24/17.
 */
public class DefaultNutsCommandLineConsoleComponent implements NutsCommandLineConsoleComponent {
    private static final Logger log = Logger.getLogger(DefaultNutsCommandLineConsoleComponent.class.getName());
    private Map<String, NutsCommand> commands = new HashMap<String, NutsCommand>();
    private NutsCommandContext context = new DefaultNutsCommandContext();
    private NutsJavaShell sh;
    private JavaShellEvalContext javaShellContext;
    public DefaultNutsCommandLineConsoleComponent() {

    }

    public NutsCommandContext getContext() {
        return context;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

    public void init(NutsWorkspace workspace, NutsSession session) throws IOException {
        context.setWorkspace(workspace);
        context.setSession(session);
        for (NutsCommand command : workspace.getFactory().createAllSupported(NutsCommand.class, this)) {
            NutsCommand old = findCommand(command.getName());
            if (old != null && old.getSupportLevel(this) > command.getSupportLevel(this)) {
                continue;
            }
            installCommand(command);
        }
        context.setCommandLine(this);
        sh=new NutsJavaShell(this,workspace);
        javaShellContext = sh.createContext(this.context, null, null, new Env(), new String[0]);
    }

    public void setServiceName(String serviceName) {
        context.setServiceName(serviceName);
    }

    public void setWorkspace(NutsWorkspace workspace) {
        context.setWorkspace(workspace);
    }

    @Override
    public void runFile(File file,String[] args) throws IOException {
        sh.executeFile(file.getPath(),sh.createContext(javaShellContext).setArgs(args),false);
    }

    @Override
    public void runLine(String line) {
        sh.executeLine(line,javaShellContext);
    }

    @Override
    public void run(String[] args) {
        sh.executeArguments(args, sh.createContext(javaShellContext).setArgs(args));
    }

    public void setCwd(String path){
        sh.setCwd(path);
    }

    public String getCwd(){
        return sh.getCwd();
    }


    @Override
    public NutsCommand[] getCommands() {
        return commands.values().toArray(new NutsCommand[commands.size()]);
    }

    @Override
    public NutsCommand getCommand(String cmd) {
        NutsCommand command = findCommand(cmd);
        if (command == null) {
            throw new RuntimeException("Command not found : " + cmd);
        }
        return command;
    }

    @Override
    public NutsCommand findCommand(String command) {
        return commands.get(command);
    }

    @Override
    public boolean installCommand(NutsCommand command) {
        boolean b = commands.put(command.getName(), command) == null;
        if (b) {
            log.log(Level.FINE, "Installing Command : " + command.getName());
        } else {
            log.log(Level.FINE, "Re-installing Command : " + command.getName());
        }
        return b;
    }

    @Override
    public boolean uninstallCommand(String command) {
        boolean b = commands.remove(command) != null;
        if (b) {
            log.log(Level.FINE, "Uninstalling Command : " + command);
        }
        return b;
    }

    @Override
    public Throwable getLastThrowable() {
        return sh.getLastThrowable();
    }

    @Override
    public String getLastErrorMessage() {
        return sh.getLastErrorMessage();
    }
}
