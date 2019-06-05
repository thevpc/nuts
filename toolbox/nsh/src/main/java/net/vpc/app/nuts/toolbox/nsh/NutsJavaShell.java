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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.vpc.common.io.ByteArrayPrintStream;
import net.vpc.common.javashell.JShellCommand;

public class NutsJavaShell extends JShell {

    private static final Logger LOG = Logger.getLogger(NutsJavaShell.class.getName());
    private NutsWorkspace workspace;
    private NutsShellContext context;
//    private NutsShellContext javaShellContext;
    private NutsApplicationContext appContext;
    private File histFile = null;

    public NutsJavaShell(NutsApplicationContext appContext) {
        this(appContext, null, null);
    }

    public NutsJavaShell(NutsWorkspace workspace) {
        this(null, workspace, null);
    }

    public NutsJavaShell(NutsWorkspace workspace, NutsSession session) {
        this(null, workspace, session);
    }

    private NutsJavaShell(NutsApplicationContext appContext, NutsWorkspace workspace, NutsSession session) {
        setErrorHandler(new NutsErrorHandler());
        setExternalExecutor(new NutsExternalExecutor());
        setCommandTypeResolver(new NutsCommandTypeResolver());
        setNodeEvaluator(new NutsNodeEvaluator());
        //super.setCwd(workspace.getConfigManager().getCwd());
        if (appContext == null) {
            this.workspace = workspace;
            this.appContext = this.workspace.io().createApplicationContext(new String[]{}, Nsh.class, null, 0);
        } else if (workspace == null) {
            this.appContext = appContext;
            this.workspace = appContext.getWorkspace();
        } else {
            throw new IllegalArgumentException("Please specify either context or workspace");
        }
        if (session == null) {
            session = this.workspace.createSession();
        }
        context = createContext();
        context.setWorkspace(this.workspace);
        this.workspace.getUserProperties().put(NutsShellContext.class.getName(), context);
        context.setSession(session);
        //add default commands
        List<NshBuiltin> allCommand = new ArrayList<>();

        for (NshBuiltin command : this.workspace.extensions().
                createServiceLoader(NshBuiltin.class, NutsJavaShell.class, NshBuiltin.class.getClassLoader())
                .loadAll(this)) {
            NshBuiltin old = (NshBuiltin) context.builtins().find(command.getName());
            if (old != null && old.getSupportLevel(this) >= command.getSupportLevel(this)) {
                continue;
            }
            allCommand.add(command);
        }
        context.builtins().set(allCommand.toArray(new JShellCommand[0]));
        context.getUserProperties().put(JShellContext.class.getName(), context);
        try {
            histFile = this.workspace.config().getStoreLocation(this.workspace.resolveIdForClass(NutsJavaShell.class),
                    NutsStoreLocation.VAR).resolve("nsh.history").toFile();
            getHistory().setHistoryFile(histFile);
            if (histFile.exists()) {
                getHistory().load(histFile);
            }
        } catch (Exception ex) {
            //ignore
            LOG.log(Level.SEVERE, "Error resolving history file", ex);
        }
        this.workspace.getUserProperties().put(JShellHistory.class.getName(), getHistory());
    }

    public void execCommand(String[] command, StringBuilder in, StringBuilder out, StringBuilder err) {
        ByteArrayPrintStream oout = new ByteArrayPrintStream();
        ByteArrayPrintStream oerr = new ByteArrayPrintStream();
        final NutsShellContext cc = getGlobalContext().copy();
        NutsSessionTerminal tt = workspace.io().getTerminal().copy();
        tt.setIn(new ByteArrayInputStream(in == null ? new byte[0] : in.toString().getBytes()));
        tt.setOut(oout);
        tt.setErr(oerr);
        cc.setTerminal(tt);
        execCommand(command, cc);
        out.append(oout.toString());
        err.append(oerr.toString());
    }

    @Override
    public NutsShellContext createContext() {
        return createContext(null, null, null, null, null);
    }

    @Override
    public JShellContext createContext(JShellContext ctx) {
        return createContext((NutsShellContext) ctx, null, null, null, null);
    }

    public NutsShellContext createContext(NutsShellContext ctx, Node root, Node parent, JShellVariables env, String[] args) {
        return new NutsJavaShellEvalContext(this, args, root, parent, ctx, workspace, appContext.getSession(), env);
    }

    public void runFile(String file, String[] args) {
        executeFile(file, createContext(context).setArgs(args), false);
    }

    public void setServiceName(String serviceName) {
        context.setServiceName(serviceName);
    }

    public void setWorkspace(NutsWorkspace workspace) {
        context.setWorkspace(workspace);
    }

    public void runLine(String line) {
        executeLine(line, context);
    }

    @Override
    public void run(String[] args) {
        NutsSessionTerminal terminal = context.getTerminal();
        PrintStream out = terminal.fout();
        PrintStream err = terminal.ferr();
        List<String> nonOptions = new ArrayList<>();
        boolean interactive = false;
        boolean perf = false;
        boolean command = false;
//        String command = null;
        long startMillis = appContext.getStartTimeMillis();
        NutsCommand cmd = null;
        cmd = appContext.getWorkspace().parser().parseCommand(args).setAutoComplete(appContext.getAutoComplete());
        NutsArgument a;
        while (cmd.hasNext()) {
            if (nonOptions.isEmpty()) {
                if ((a = cmd.next("--help")) != null) {
                    command = true;
                    nonOptions.add("help");
                } else if (appContext != null && appContext.configureFirst(cmd)) {
                    //ok
                } else if ((a = cmd.nextString("-c", "--command")) != null) {
                    command = true;
                    String cc = a.getValue().getString();
                    if (StringUtils.isBlank(cc)) {
                        cmd.required("missing command for -c");
                    }
                    nonOptions.add(cc);
                    nonOptions.addAll(Arrays.asList(cmd.toArray()));
                    cmd.skipAll();
                } else if ((a = cmd.nextBoolean("-i", "--interactive")) != null) {
                    interactive = a.getValue().getBoolean();
                } else if ((a = cmd.nextBoolean("--perf")) != null) {
                    perf = a.getValue().getBoolean();
                } else if ((a = cmd.nextBoolean("-x")) != null) {
                    getOptions().setXtrace(a.getValue().getBoolean());
                } else if (cmd.peek().isOption()) {
                    cmd.setCommandName("nsh").unexpectedArgument();
                } else {
                    nonOptions.add(cmd.next().getString());
                }
            } else {
                nonOptions.add(cmd.next().getString());
            }
        }
        if (nonOptions.isEmpty()) {
            interactive = true;
        }
        if (!cmd.isExecMode()) {
            return;
        }
        if (appContext != null) {
            context.setTerminalMode(appContext.getTerminalMode());
            context.setVerbose(appContext.isVerbose());
        }
        context.setSession(context.getSession());
        if (nonOptions.size() > 0) {
            String c = nonOptions.get(0);
            if (!command) {
                nonOptions.remove(0);
                context.setArgs(nonOptions.toArray(new String[0]));
                if (perf) {
                    terminal.fout().printf("**Nsh** loaded in [[%s]]\n",
                            Chronometer.formatPeriodMilli(System.currentTimeMillis() - startMillis)
                    );
                }
                executeFile(c, context, false);
                return;
            } else {
                if (perf) {
                    terminal.fout().printf("**Nsh** loaded in [[%s]]\n",
                            Chronometer.formatPeriodMilli(System.currentTimeMillis() - startMillis)
                    );
                }
                executeArguments(nonOptions.toArray(new String[0]), false, context);
                return;
            }
        }
        if (interactive) {
            if (perf) {
                terminal.fout().printf("**Nsh** loaded in [[%s]]\n",
                        Chronometer.formatPeriodMilli(System.currentTimeMillis() - startMillis)
                );
            }
            runInteractive(out);
        }
    }

    protected void runInteractive(PrintStream out) {
        NutsSessionTerminal terminal = null;
        printHeader(out);

        while (true) {

            terminal = context.getTerminal();
            NutsWorkspace ws = context.getWorkspace();
            String wss = ws == null ? "" : new File(context.getAbsolutePath(ws.config().getWorkspaceLocation().toString())).getName();
            String login = null;
            if (ws != null) {
                login = ws.security().getCurrentLogin();
            }
            String prompt = ((login != null && login.length() > 0 && !"anonymous".equals(login)) ? (login + "@") : "");//+ wss;
            if (!StringUtils.isBlank(context.getServiceName())) {
                prompt = prompt + "@" + context.getServiceName();
            }
            prompt += "> ";

            String line = null;
            try {
                line = terminal.readLine(prompt);
            } catch (JShellInterruptException ex) {
                terminal.ferr().printf("@@Exit Shell@@: ==%s==\n", ex.getMessage());
                break;
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() > 0) {
                try {
                    runLine(line);
                } catch (JShellQuitException q) {
                    try {
                        if (histFile != null) {
                            getHistory().save(histFile);
                        }
                    } catch (IOException e) {
                        //e.printStackTrace();
                    }
                    if (q.getResult() == 0) {
                        return;
                    }
                    throw new NutsExecutionException(context.getWorkspace(), q.getMessage(), q.getResult());
                }
            }
        }
        throw new NutsExecutionException(workspace, "EXIT", 1);
    }

    protected PrintStream printHeader(PrintStream out) {
        return out.printf("##Nuts## shell (**Network Updatable Things Services**) [[v%s]] (c) vpc 2018\n",
                context.getWorkspace().config().getContext(NutsBootContextType.RUNTIME).getRuntimeId().getVersion().toString());
    }

    //    @Override
    public void runCommand(String[] args) {
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
        executeArguments(args, true, createContext(context).setArgs(args));
    }

    @Override
    public String getVersion() {
        return PomIdResolver.resolvePomId(getClass(), new PomId("", "", "dev")).getVersion();
    }

    public NutsShellContext getGlobalContext() {
        return context;
    }

    public static class NutsNodeEvaluator extends DefaultJShellNodeEvaluator implements JShellNodeEvaluator {

        @Override
        public void evalBinaryPipeOperation(InstructionNode left, InstructionNode right, JShellContext context) {
            final PrintStream nout;
            final PipedOutputStream out;
            final PipedInputStream in;
            final JavaShellNonBlockingInputStream in2;
            NutsShellContext ncontext = (NutsShellContext) context;
            try {
                out = new PipedOutputStream();
                nout = ncontext.getWorkspace().io().createPrintStream(out, NutsTerminalMode.FORMATTED);
                in = new PipedInputStream(out, 1024);
                in2 = (in instanceof JavaShellNonBlockingInputStream) ? (JavaShellNonBlockingInputStream) in : new JavaShellNonBlockingInputStreamAdapter("jpipe-" + right.toString(), in);
            } catch (IOException ex) {
                throw new JShellException(1, ex);
            }
            final JShellContext leftContext = context.getShell().createContext(context).setOut(nout);
            final JShellUniformException[] a = new JShellUniformException[2];
            Thread j1 = new Thread() {
                @Override
                public void run() {
                    try {
                        context.getShell().uniformException(new NodeEvalUnsafeRunnable(left, leftContext));
                    } catch (JShellUniformException e) {
                        if (e.isQuit()) {
                            e.throwQuit();
                            return;
                        }
                        a[0] = e;
                    }
                    in2.noMoreBytes();
                }

            };
            j1.start();
            JShellContext rightContext = context.getShell().createContext(context).setIn((InputStream) in2);
            try {
                context.getShell().uniformException(new NodeEvalUnsafeRunnable(right, rightContext));
            } catch (JShellUniformException e) {
                if (e.isQuit()) {
                    e.throwQuit();
                    return;
                }
                a[1] = e;
            }
            try {
                j1.join();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            if (a[1] != null) {
                a[1].throwAny();
            }
        }
    }

    private class NutsCommandTypeResolver implements JShellCommandTypeResolver {

        @Override
        public JShellCommandType type(String item, JShellContext context) {
            String a = context.aliases().get(item);
            if (a != null) {
                return new JShellCommandType(item, "shell alias", a, item + " is aliased to " + a);
            }
            String path = item;
            if (!item.startsWith("/")) {
                path = context.getCwd() + "/" + item;
            }
            final NutsExecutableInfo w = workspace.exec().command(item).which();
            if (w != null) {
                return new JShellCommandType(item, "nuts " + w.getType().toString().toLowerCase(), w.getValue(), w.getDescription());
            }
            if (new File(path).isFile()) {
                return new JShellCommandType(item, "path", path, item + " is " + path);
            }
            return null;
        }

    }

    public static class NutsExternalExecutor implements JShellExternalExecutor {

        @Override
        public void execExternalCommand(String[] command, JShellContext context) {
            NutsShellContext jc = (NutsShellContext) context;
            jc.getWorkspace().exec()
                    //                .session(context.get)
                    .command(command).failFast().run();
        }

    }

    public static class NutsErrorHandler implements JShellErrorHandler {

        @Override
        public boolean isRequireExit(Throwable th) {
            return th instanceof JShellQuitException;
        }

        @Override
        public int errorToCode(Throwable th) {
            if (th instanceof NutsExecutionException) {
                return ((NutsExecutionException) th).getExitCode();
            }
            if (th instanceof JShellException) {
                return ((NutsExecutionException) th).getExitCode();
            }
            return 1;
        }

        @Override
        public String errorToMessage(Throwable th) {
            return StringUtils.exceptionToString(th);
        }

        @Override
        public void onErrorImpl(String message, Throwable th, JShellContext context) {
            ((NutsShellContext) context).getTerminal().ferr().printf("@@%s@@\n", message);
        }

    }

    public static class NutsBuiltinManager implements JShellBuiltinManager {

        private Map<String, NshBuiltin> commands = new HashMap<>();

        @Override
        public boolean contains(String cmd) {
            return find(cmd) != null;
        }

        @Override
        public NshBuiltin find(String command) {
            return commands.get(command);
        }

        public JShellCommand get(String cmd) {
            JShellCommand command = find(cmd);
            if (command == null) {
                throw new NoSuchElementException("Command not found : " + cmd);
            }
            if (!command.isEnabled()) {
                throw new NoSuchElementException("Command disabled : " + cmd);
            }
            return command;
        }

        @Override
        public void set(JShellCommand command) {
            if (!(command instanceof NshBuiltin)) {
                command = new ShellToNshCommand(command);
            }
            boolean b = commands.put(command.getName(), (NshBuiltin) command) == null;
            if (LOG.isLoggable(Level.FINE)) {
                if (b) {
                    LOG.log(Level.FINE, "Installing Command : " + command.getName());
                } else {
                    LOG.log(Level.FINE, "Re-installing Command : " + command.getName());
                }
            }
        }

        @Override
        public void set(JShellCommand... cmds) {
            StringBuilder installed = new StringBuilder();
            StringBuilder reinstalled = new StringBuilder();
            int installedCount = 0;
            int reinstalledCount = 0;
            boolean loggable = LOG.isLoggable(Level.FINE);
            for (JShellCommand command : cmds) {
                if (!(command instanceof NshBuiltin)) {
                    command = new ShellToNshCommand(command);
                }
                boolean b = commands.put(command.getName(), (NshBuiltin) command) == null;
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
        public boolean unset(String command) {
            boolean b = commands.remove(command) != null;
            if (LOG.isLoggable(Level.FINE)) {
                if (b) {
                    LOG.log(Level.FINE, "Uninstalling Command : " + command);
                }
            }
            return b;
        }

        @Override
        public NshBuiltin[] getAll() {
            return commands.values().toArray(new NshBuiltin[0]);
        }

    }

}
