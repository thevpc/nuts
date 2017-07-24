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
package net.vpc.app.nuts.extensions.cmd;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/24/17.
 */
public class DefaultNutsCommandLineConsoleComponent implements NutsCommandLineConsoleComponent {
    private static final Logger log = Logger.getLogger(DefaultNutsCommandLineConsoleComponent.class.getName());
    private Map<String, NutsCommand> commands = new HashMap<String, NutsCommand>();
    private NutsCommandContext context = new NutsCommandContext();

    public DefaultNutsCommandLineConsoleComponent() {

    }

    @Override
    public int getSupportLevel(Object criteria) {
        return CORE_SUPPORT;
    }

    public void init(NutsWorkspace workspace, NutsSession session) throws IOException {
        context.setSession(context.getSession());
        context.setWorkspace(workspace);
        context.setSession(session);
        for (NutsCommand command : workspace.getFactory().createAllSupported(NutsCommand.class,this)) {
            NutsCommand old = findCommand(command.getName());
            if (old != null && old.getSupportLevel(this) > command.getSupportLevel(this)) {
                continue;
            }
            installCommand(command);
        }
        context.setCommandLine(this);
    }

    public void setServiceName(String serviceName) {
        context.setServiceName(serviceName);
    }

    public void setWorkspace(NutsWorkspace workspace) {
        context.setWorkspace(workspace);
    }

    @Override
    public void run(String[] args) {
        try {
            List<String> options = new ArrayList<String>();
            boolean noexec = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (args[i].equals("-v") || args[i].equals("--version")) {
                        System.out.println("version " + context.getValidWorkspace().getWorkspaceVersion());
                        return;
                    }else if(args[i].equals("--noexec")){
                        noexec=true;
                    }else if (args[i].equals("-?") || args[i].equals("--help")) {
                        getCommand("help").run(new String[0], context);
                        return;
                    } else {
                        options.add(args[i]);
                    }
                } else {
                    context.getValidWorkspace();
                    String cmd = args[i];

                    if (cmd.equals("!")) {
                        cmd = "exec";
                    } else if (cmd.startsWith("!")) {
                        String[] cmdArgs = new String[args.length - i + 1];
                        if (i + 1 < args.length) {
                            System.arraycopy(args, i + 1, cmdArgs, 2, cmdArgs.length - 2);
                        }
                        String oldCmd = cmd;
                        cmd = "exec";
                        cmdArgs[i] = cmd;
                        cmdArgs[i + 1] = oldCmd.substring(1);
                        args = cmdArgs;
                        i = 0;
                    }

                    NutsCommand c = findCommand(cmd);
                    if (c == null) {
                        if (noexec) {
                            context.getTerminal().getErr().println("Command not found " + cmd);
                        } else {
                            try {
                                NutsId.parseOrError(args[i]);
                            }catch (net.vpc.app.nuts.NutsIdInvalidFormatException invalid){
                                context.getTerminal().getErr().println("Command not found " + cmd);
                                return;
                            }
                            String[] cmdArgs = new String[args.length - i];
                            System.arraycopy(args, i, cmdArgs, 0, cmdArgs.length);
                            try {
                                initInvoke();
                                context.getValidWorkspace().exec(args, context.getSession());
                            } catch (Throwable ex) {
                                finalizeInvoke(ex);
                            }
                        }
                    } else {
                        String[] cmdArgs = new String[args.length - 1 - i];
                        System.arraycopy(args, i + 1, cmdArgs, 0, cmdArgs.length);

                        try {
                            initInvoke();
                            c.run(cmdArgs, context);
                        } catch (Throwable ex) {
                            finalizeInvoke(ex);
                        }
                    }
                    return;
                }
            }
            throw new RuntimeException("No Action found");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void initInvoke(){
        context.setLatestError(context.getLastError());
        context.setLastError(null);
    }
    private void finalizeInvoke(Throwable ex){
        String message = StringUtils.exceptionToString(ex);
        if (StringUtils.isEmpty(message)) {
            message = ex.toString();
        }
        if (StringUtils.isEmpty(message)) {
            message = ex.getClass().getName();
        }
        context.getTerminal().getErr().println(message);

        context.setLastError(ex);
    }

    @Override
    public NutsCommand[] getCommands() {
        return commands.values().toArray(new NutsCommand[commands.size()]);
    }

    @Override
    public NutsCommand getCommand(String cmd) {
        NutsCommand command = findCommand(cmd);
        if (command == null) {
            throw new RuntimeException("Command not found " + cmd);
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
            log.log(Level.FINE, "Installing Command " + command.getName());
        } else {
            log.log(Level.FINE, "Re-installing Command " + command.getName());
        }
        return b;
    }

    @Override
    public boolean uninstallCommand(String command) {
        boolean b = commands.remove(command) != null;
        if (b) {
            log.log(Level.FINE, "Uninstalling Command " + command);
        }
        return b;
    }
}
