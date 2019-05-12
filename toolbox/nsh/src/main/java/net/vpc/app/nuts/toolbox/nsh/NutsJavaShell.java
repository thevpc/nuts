/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.app.nuts.*;
import net.vpc.common.javashell.*;
import net.vpc.common.javashell.cmds.*;
import net.vpc.common.javashell.parser.nodes.BinoOp;
import net.vpc.common.javashell.parser.nodes.InstructionNode;
import net.vpc.common.javashell.parser.nodes.Node;
import net.vpc.common.javashell.util.JavaShellNonBlockingInputStream;
import net.vpc.common.javashell.util.JavaShellNonBlockingInputStreamAdapter;
import net.vpc.common.mvn.PomId;
import net.vpc.common.mvn.PomIdResolver;
import net.vpc.common.strings.StringUtils;
import net.vpc.common.util.Chronometer;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsJavaShell extends JavaShell {

    private static final Logger LOG = Logger.getLogger(NutsJavaShell.class.getName());
    private NutsWorkspace workspace;
    private Map<String, NutsCommand> commands = new HashMap<>();
    private NutsConsoleContext context;
    private NutsConsoleContext javaShellContext;
    private NutsApplicationContext appContext;
    private File histFile = null;

    //    public NutsJavaShell(NutsApplicationContext appContext) {
//        this.appContext = appContext;
//        init(appContext.getWorkspace(), appContext.getSession());
//    }
//
//    public NutsJavaShell(NutsWorkspace workspace) {
//        init(workspace, workspace.createSession());
//    }
//
//    public NutsJavaShell(NutsWorkspace workspace, NutsSession session) {
//        init(workspace, session);
//    }
//
    public NutsJavaShell(NutsApplicationContext appContext) {
        this(appContext.getWorkspace(), appContext.getSession());
        this.appContext = appContext;
    }

    public NutsJavaShell(NutsWorkspace workspace, NutsSession session) {
        //super.setCwd(workspace.getConfigManager().getCwd());
        context = new NutsJavaShellEvalContext(this, new String[0], null, null, null, workspace, session, new Env());
        this.workspace = workspace;
        context.setWorkspace(workspace);
        workspace.getUserProperties().put(NutsConsoleContext.class.getName(), context);
        context.setSession(session);
        //add default commands
        List<Command> allCommand = new ArrayList<>();
        allCommand.add(new ExitCmd());

        for (NutsCommand command : workspace.extensions().
                createServiceLoader(NutsCommand.class, NutsJavaShell.class, NutsCommand.class.getClassLoader())
                .loadAll(this)) {
            NutsCommand old = findCommand(command.getName());
            if (old != null && old.getSupportLevel(this) >= command.getSupportLevel(this)) {
                continue;
            }
            allCommand.add(command);
        }
        declareCommands(allCommand.toArray(new Command[0]));
        context.setShell(this);
        javaShellContext = createContext(this.context, null, null, new Env(), new String[0]);
        context.getUserProperties().put(ConsoleContext.class.getName(), javaShellContext);
        try {
            histFile = workspace.config().getStoreLocation(workspace.resolveIdForClass(NutsJavaShell.class),
                    NutsStoreLocation.VAR).resolve("nsh.history").toFile();
            getHistory().setHistoryFile(histFile);
            if (histFile.exists()) {
                getHistory().load(histFile);
            }
        } catch (Exception ex) {
            //ignore
            LOG.log(Level.SEVERE, "Error resolving history file", ex);
        }
        workspace.getUserProperties().put(ShellHistory.class.getName(), getHistory());
    }

    @Override
    public boolean containsCommand(String cmd) {
        return findCommand(cmd) != null;
    }

    @Override
    public void declareCommand(Command command) {
        if (!(command instanceof NutsCommand)) {
            command = new ShellToNutsCommand(command);
        }
        boolean b = commands.put(command.getName(), (NutsCommand) command) == null;
        if (LOG.isLoggable(Level.FINE)) {
            if (b) {
                LOG.log(Level.FINE, "Installing Command : " + command.getName());
            } else {
                LOG.log(Level.FINE, "Re-installing Command : " + command.getName());
            }
        }
    }

    @Override
    public void declareCommands(Command... cmds) {
        StringBuilder installed = new StringBuilder();
        StringBuilder reinstalled = new StringBuilder();
        int installedCount = 0;
        int reinstalledCount = 0;
        boolean loggable = LOG.isLoggable(Level.FINE);
        for (Command command : cmds) {
            if (!(command instanceof NutsCommand)) {
                command = new ShellToNutsCommand(command);
            }
            boolean b = commands.put(command.getName(), (NutsCommand) command) == null;
            if (loggable) {
                if (b) {
                    if (installed.length() > 0) {
                        installed.append(", ");
                    }
                    installed.append(command.getName());
                    installedCount++;
                } else {
                    if (reinstalled.length() > 0) {
                        reinstalled.append(", ");
                    }
                    reinstalled.append(command.getName());
                    reinstalledCount++;
                }
            }
        }
        if (loggable) {
            if (installed.length() > 0) {
                installed.insert(0, "Installing " + installedCount + " Command" + (installedCount > 1 ? "s" : "") + " : ");
            }
            if (reinstalled.length() > 0) {
                installed.append(" ; Re-installing ").append(reinstalledCount).append(" Command").append(reinstalledCount > 1 ? "s" : "").append(" : ");
                installed.append(reinstalled);
            }
            LOG.log(Level.FINE, installed.toString());
        }
    }

    @Override
    public boolean undeclareCommand(String command) {
        boolean b = commands.remove(command) != null;
        if (LOG.isLoggable(Level.FINE)) {
            if (b) {
                LOG.log(Level.FINE, "Uninstalling Command : " + command);
            }
        }
        return b;
    }

    @Override
    public int execExternalCommand(String[] command, ConsoleContext context) {
        try {
            Command exec = getCommand("exec");
            return exec.exec(command, context.createCommandContext(exec));
        } catch (Exception ex) {
            return onResult(1, ex);
        }
    }

    @Override
    public ConsoleContext createContext(Node root, Node parent, Env env, String[] args) {
        return createContext(context, root, parent, env, args);
    }

    @Override
    public ConsoleContext createContext(ConsoleContext parentContext) {
        return new NutsJavaShellEvalContext(parentContext);
    }

    public NutsConsoleContext createContext(NutsConsoleContext commandContext, Node root, Node parent, Env env, String[] args) {
        return new NutsJavaShellEvalContext(this, args, root, parent, commandContext, workspace, workspace.createSession(), env);
    }

    @Override
    public String errorToMessage(Throwable th) {
        return StringUtils.exceptionToString(th);
    }

    @Override
    public void onErrorImpl(String message, Throwable th) {
        context.getTerminal().ferr().printf("@@@%s@@@\n", message);
    }

    @Override
    public String which(String path0, ConsoleContext context) {
        if (!path0.startsWith("/")) {
            return getCwd() + "/" + path0;
        }
        return path0;
    }

    @Override
    public void setCwd(String cwd) {
        super.setCwd(cwd);
    }

    protected int evalBinaryPipeOperation(InstructionNode left, InstructionNode right, ConsoleContext context) {
        final PrintStream nout;
        final PipedOutputStream out;
        final PipedInputStream in;
        final JavaShellNonBlockingInputStream in2;
        try {
            out = new PipedOutputStream();
            nout = workspace.io().createPrintStream(out, NutsTerminalMode.FORMATTED);
            in = new PipedInputStream(out, 1024);
            in2 = (in instanceof JavaShellNonBlockingInputStream) ? (JavaShellNonBlockingInputStream) in : new JavaShellNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
        } catch (IOException ex) {
            Logger.getLogger(BinoOp.class.getName()).log(Level.SEVERE, null, ex);
            return 1;
        }
        final Integer[] a = new Integer[2];
        Thread j1 = new Thread() {
            @Override
            public void run() {
                a[0] = left.eval(context.getShell().createContext(context).setOut(nout));
                in2.noMoreBytes();
            }

        };
        j1.start();
        ConsoleContext rightContext = context.getShell().createContext(context).setIn((InputStream) in2);
        a[1] = right.eval(rightContext);
        try {
            j1.join();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return a[1];
    }

    public NutsCommand[] getCommands() {
        return commands.values().toArray(new NutsCommand[0]);
    }

    public int runFile(String file, String[] args) {
        return executeFile(file, createContext(javaShellContext).setArgs(args), false);
    }

    public void setServiceName(String serviceName) {
        context.setServiceName(serviceName);
    }

    public void setWorkspace(NutsWorkspace workspace) {
        context.setWorkspace(workspace);
    }

    public int runLine(String line) {
        return executeLine(line, javaShellContext);
    }

    @Override
    public int run(String[] args) {
        NutsSessionTerminal terminal = context.getTerminal();
        PrintStream out = terminal.fout();
        PrintStream err = terminal.ferr();
        List<String> nonOptions = new ArrayList<>();
        boolean interactive = false;
        boolean perf = false;
        boolean command = false;
//        String command = null;
        long startMillis = appContext == null ? System.currentTimeMillis() : appContext.getStartTimeMillis();
        NutsCommandLine cmd = null;
        if(appContext==null){
            cmd=new NutsDefaultCommandLine(args);
        }else{
            cmd=appContext.getWorkspace().parser().parseCommandLine(args).setAutoComplete(appContext.getAutoComplete());
        }
        NutsArgument a;
        while (cmd.hasNext()) {
            if (nonOptions.isEmpty()) {
                if ((a = cmd.readOption("--help")) != null) {
                    command = true;
                    nonOptions.add("help");
                } else if (appContext != null && appContext.configure(cmd)) {
                    //ok
                } else if ((a = cmd.readStringOption("-c", "--command")) != null) {
                    command = true;
                    nonOptions.add(a.getValue().getString());
                } else if ((a = cmd.readBooleanOption("-i", "--interactive")) != null) {
                    interactive = a.getBooleanValue();
                } else if ((a = cmd.readBooleanOption("--perf")) != null) {
                    perf = a.getBooleanValue();
                } else if ((a = cmd.readBooleanOption("-x")) != null) {
                    getOptions().setXtrace(a.getBooleanValue());
                } else if ((a = cmd.readBooleanOption("-c")) != null) {
                    nonOptions.add(cmd.read().getString());
                } else if (cmd.get().isOption()) {
                    cmd.setCommandName("nsh").unexpectedArgument();
                } else {
                    nonOptions.add(cmd.read().getString());
                }
            } else {
                nonOptions.add(cmd.read().getString());
            }
        }
        int ret = 0;
        if (nonOptions.isEmpty()) {
            interactive = true;
        }
        if (!cmd.isExecMode()) {
            return 0;
        }
        if (appContext != null) {
            javaShellContext.setTerminalMode(appContext.getTerminalMode());
            javaShellContext.setVerbose(appContext.isVerbose());
        }
        if (nonOptions.size() > 0) {
            String c = nonOptions.get(0);
            if (!command) {
                nonOptions.remove(0);
                javaShellContext.setArgs(nonOptions.toArray(new String[0]));
                if (perf) {
                    terminal.fout().printf("**Nsh** loaded in [[%s]]\n",
                            Chronometer.formatPeriodMilli(System.currentTimeMillis() - startMillis)
                    );
                }
                ret = executeFile(c, javaShellContext, false);
            } else {
                if (perf) {
                    terminal.fout().printf("**Nsh** loaded in [[%s]]\n",
                            Chronometer.formatPeriodMilli(System.currentTimeMillis() - startMillis)
                    );
                }
                ret = executeArguments(nonOptions.toArray(new String[0]), false, javaShellContext);
            }
        }
        if (interactive) {
            if (perf) {
                terminal.fout().printf("**Nsh** loaded in [[%s]]\n",
                        Chronometer.formatPeriodMilli(System.currentTimeMillis() - startMillis)
                );
            }
            ret = runInteractive(out);
        }
        return ret;
    }

    protected int runInteractive(PrintStream out) {
        NutsSessionTerminal terminal = null;
        printHeader(out);

        while (true) {

            terminal = context.getTerminal();
            NutsWorkspace ws = context.getWorkspace();
            String wss = ws == null ? "" : new File(context.getShell().getAbsolutePath(ws.config().getWorkspaceLocation().toString())).getName();
            String login = null;
            if (ws != null) {
                login = ws.security().getCurrentLogin();
            }
            String prompt = ((login != null && login.length() > 0 && !"anonymous".equals(login)) ? (login + "@") : "");//+ wss;
            if (!StringUtils.isEmpty(context.getServiceName())) {
                prompt = prompt + "@" + context.getServiceName();
            }
            prompt += "> ";

            String line = null;
            try {
                line = terminal.readLine(prompt);
            } catch (InterruptShellException ex) {
                terminal.ferr().printf("@@Exit Shell@@: ==%s==\n", ex.getMessage());
                break;
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() > 0) {
                try {
                    runLine(line);
                } catch (QuitShellException q) {
                    try {
                        if (histFile != null) {
                            getHistory().save(histFile);
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                    return 0;
                }
            }
        }
        return 1;
    }

    protected PrintStream printHeader(PrintStream out) {
        return out.printf("##Nuts## shell (**Network Updatable Things Services**) [[v%s]] (c) vpc 2018\n",
                context.getWorkspace().config().getContext(NutsBootContextType.RUNTIME).getRuntimeId().getVersion().toString());
    }

    //    @Override
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
        getHistory().add(sb.toString());
        return executeArguments(args, true, createContext(javaShellContext).setArgs(args));
    }

    public String getVersion() {
        return PomIdResolver.resolvePomId(getClass(), new PomId("", "", "dev")).getVersion();
    }

    public NutsCommand findCommand(String command) {
        return commands.get(command);
    }

}
