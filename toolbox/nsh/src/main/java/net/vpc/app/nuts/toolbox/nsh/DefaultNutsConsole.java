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
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.common.javashell.*;
import net.vpc.common.javashell.cmds.*;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/24/17.
 */
public class DefaultNutsConsole implements NutsConsole {

    private static final Logger log = Logger.getLogger(DefaultNutsConsole.class.getName());
    private Map<String, NutsCommand> commands = new HashMap<String, NutsCommand>();
    private NutsCommandContext context = new DefaultNutsCommandContext();
    private NutsJavaShell sh;
    private JavaShellEvalContext javaShellContext;
    private HistoryElementList history = new HistoryElementList();

    public DefaultNutsConsole(NutsWorkspace workspace) {
        init(workspace,workspace.createSession());
    }

    public DefaultNutsConsole(NutsWorkspace workspace, NutsSession session) {
        init(workspace,session);
    }
    public DefaultNutsConsole() {

    }

    public NutsCommandContext getContext() {
        return context;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

    @Override
    public void init(NutsWorkspace workspace, NutsSession session) {
        context.setWorkspace(workspace);
        context.setSession(session);
        //add default commands
        installCommand(new ShellToNutsCommand(new EnvCmd()));
        installCommand(new ShellToNutsCommand(new AliasCmd()));
        installCommand(new ShellToNutsCommand(new ExitCmd()));
//        installCommand(new ShellToNutsCommand(new PropsCmd()));
        installCommand(new ShellToNutsCommand(new SetCmd()));
//        installCommand(new ShellToNutsCommand(new SetPropCmd()));
        installCommand(new ShellToNutsCommand(new ShowerrCmd()));
        installCommand(new ShellToNutsCommand(new UnsetCmd()));
        installCommand(new ShellToNutsCommand(new UnaliasCmd()));
        installCommand(new ShellToNutsCommand(new UnexportCmd()));
        installCommand(new ShellToNutsCommand(new TypeCmd()));
        installCommand(new ShellToNutsCommand(new SourceCmd()));
        installCommand(new ShellToNutsCommand(new PwdCmd()));

        for (NutsCommand command : workspace.getExtensionManager().createAllSupported(NutsCommand.class, this)) {
            NutsCommand old = findCommand(command.getName());
            if (old != null && old.getSupportLevel(this) >= command.getSupportLevel(this)) {
                continue;
            }
            installCommand(command);
        }
        context.setConsole(this);
        sh = new NutsJavaShell(this, workspace);
        javaShellContext = sh.createContext(this.context, null, null, new Env(), new String[0]);
        context.getUserProperties().put(JavaShellEvalContext.class.getName(), javaShellContext);
        try {
            history.load();
        } catch (Exception ex) {
            //ignore
        }
    }

    public void setServiceName(String serviceName) {
        context.setServiceName(serviceName);
    }

    public void setWorkspace(NutsWorkspace workspace) {
        context.setWorkspace(workspace);
    }

    @Override
    public int runFile(String file, String[] args) {
        return sh.executeFile(file, sh.createContext(javaShellContext).setArgs(args), false);
    }

    @Override
    public int runLine(String line) {
        history.add(line);
        return sh.executeLine(line, javaShellContext);
    }

    @Override
    public int run(String[] args) {
        NutsTerminal terminal = context.getTerminal();
        NutsPrintStream out = terminal.getFormattedOut();
        NutsPrintStream err = terminal.getFormattedErr();
        List<String> nonOptions=new ArrayList<>();
        boolean interactive=false;
        String command=null;
        for (int i = 0; i < args.length; i++) {
            String a=args[i];
            if(nonOptions.isEmpty() && a.startsWith("-")){
                switch (a){
                    case "-c":{
                        i++;
                        command=args[i];
                        break;
                    }
                    case "-i":{
                        interactive=true;
                        break;
                    }
                    case "--version":{
                        String wsVersion = context.getValidWorkspace().getConfigManager().getWorkspaceRuntimeId().getVersion().toString();
                        out.printf("**Nuts** console (**Network Updatable Things Services**) **v%s** (c) vpc 2017\n",wsVersion);
                        return 0;
                    }
                    default: {
                        err.printf("Unknown option %s\n",a);
                        return 1;
                    }
                }
            }else{
                nonOptions.add(a);
            }
        }
        int ret=0;
        if(command==null && nonOptions.isEmpty()){
            interactive=true;
        }
        if(command!=null){
            javaShellContext.setArgs(nonOptions.toArray(new String[nonOptions.size()]));
            ret=sh.executeLine(command, javaShellContext);
        }else if(nonOptions.size()>0){
            String c=nonOptions.get(0);
            if(c.contains("/") || c.contains("\\")){
                nonOptions.remove(0);
                javaShellContext.setArgs(nonOptions.toArray(new String[nonOptions.size()]));
                ret=sh.executeFile(c,javaShellContext,false);
            }else {
                ret = sh.executeArguments(nonOptions.toArray(new String[nonOptions.size()]), javaShellContext);
            }
        }
        if(interactive) {
            ret=runInteractive(out);
        }
        return ret;
    }

    protected int runInteractive(NutsPrintStream out){
        NutsTerminal terminal = null;
        out.printf("**Nuts** console (**Network Updatable Things Services**) **v%s** (c) vpc 2017\n",
                context.getValidWorkspace().getConfigManager().getWorkspaceRuntimeId().getVersion().toString());

        while (true) {

            terminal = context.getTerminal();
            NutsWorkspace ws = context.getWorkspace();
            String wss = ws == null ? "" :new File(context.getAbsolutePath(ws.getConfigManager().getWorkspaceLocation())).getName();
            String login = null;
            if (ws != null) {
                login = ws.getSecurityManager().getCurrentLogin();
            }
            String prompt = login + "@" + wss;
            if (!StringUtils.isEmpty(context.getServiceName())) {
                prompt = prompt + "@" + context.getServiceName();
            }
            prompt += "> ";

            String line = null;
            try {
                line = terminal.readLine(prompt);
            } catch (InterrupShellException ex) {
                terminal.getFormattedErr().printf("==%s==\n", ex.getMessage());
                continue;
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() > 0) {
                try {
                    runLine(line);
                } catch (QuitShellException q) {
                    return 0;
                }
            }
        }
        return 1;
    }

    @Override
    public int runCommand(String[] args) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (i > 0) {
                sb.append(" ");
            }
            if (arg.contains(" ")) {
                sb.append("\"").append(arg).append("\"");
            } else {
                sb.append(arg);
            }
        }
        history.add(sb.toString());
        return sh.executeArguments(args, sh.createContext(javaShellContext).setArgs(args));
    }


    public int getLastResult() {
        return sh.getLastResult();
    }

    public String getCwd() {
        return sh.getCwd();
    }

    public void setCwd(String path) {
        sh.setCwd(path);
    }

    @Override
    public NutsCommand[] getCommands() {
        return commands.values().toArray(new NutsCommand[commands.size()]);
    }

    @Override
    public NutsCommand getCommand(String cmd) {
        NutsCommand command = findCommand(cmd);
        if (command == null) {
            throw new NutsIllegalArgumentException("Command not found : " + cmd);
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
        if(log.isLoggable(Level.FINE)) {
            if (b) {
                log.log(Level.FINE, "Installing Command : " + command.getName());
            } else {
                log.log(Level.FINE, "Re-installing Command : " + command.getName());
            }
        }
        return b;
    }

    @Override
    public boolean uninstallCommand(String command) {
        boolean b = commands.remove(command) != null;
        if(log.isLoggable(Level.FINE)) {
            if (b) {
                log.log(Level.FINE, "Uninstalling Command : " + command);
            }
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

    @Override
    public List<HistoryElement> getHistory(int maxElements) {
        return history.getElements(maxElements);
    }
}
