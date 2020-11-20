/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.nuts.*;
import net.thevpc.jshell.*;
import net.thevpc.jshell.parser.nodes.Node;
import net.thevpc.common.mvn.PomId;
import net.thevpc.common.mvn.PomIdResolver;
import net.thevpc.common.strings.StringUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.thevpc.common.io.ByteArrayPrintStream;
import net.thevpc.jshell.JShellBuiltin;

public class NutsJavaShell extends JShell {

    private static final Logger LOG = Logger.getLogger(NutsJavaShell.class.getName());
    private NutsApplicationContext appContext;
    private File histFile = null;
    private NutsId appId = null;

    public NutsJavaShell(NutsApplicationContext appContext) {
        this(appContext, null, null, null, null);
    }

    public NutsJavaShell(NutsWorkspace workspace) {
        this(null, workspace, null, null, null);
    }

    public NutsJavaShell(NutsWorkspace workspace, NutsSession session, NutsId appId) {
        this(null, workspace, session, appId, null);
    }

    public NutsJavaShell(NutsWorkspace workspace, NutsSession session, NutsId appId, String serviceName) {
        this(null, workspace, session, appId, serviceName);
    }

    private NutsJavaShell(NutsApplicationContext appContext, NutsWorkspace workspace, NutsSession session, NutsId appId, String serviceName) {
        this.appId = appId;
        setErrorHandler(new NutsErrorHandler());
        setExternalExecutor(new NutsExternalExecutor());
        setCommandTypeResolver(new NutsCommandTypeResolver());
        setNodeEvaluator(new NutsNodeEvaluator());
        //super.setCwd(workspace.getConfigManager().getCwd());
        if (appContext == null) {
            this.appContext = workspace.apps().createApplicationContext(new String[]{}, Nsh.class, null, 0);
        } else if (workspace == null) {
            this.appContext = appContext;
        } else {
            throw new IllegalArgumentException("Please specify either context or workspace");
        }
        if (session == null) {
            session = this.appContext.getWorkspace().createSession();
        }
        if (this.appId == null) {
            this.appId = getWorkspace().id().resolveId(NutsJavaShell.class);
        }
        if (this.appId == null) {
            throw new IllegalArgumentException("Unable to resolve application id");
        }
        if ((serviceName == null || serviceName.trim().isEmpty())) {
            serviceName = this.appId.getArtifactId();
        }
        NutsShellContext _nrootContext = getRootNutsShellContext();
        JShellContext _rootContext = getRootContext();
        _rootContext.setServiceName(serviceName);
        NutsWorkspace ws = this.getWorkspace();
        JShellHistory hist = getHistory();

        _nrootContext.setWorkspace(this.appContext.getWorkspace());
        this.appContext.getWorkspace().userProperties().put(NutsShellContext.class.getName(), _rootContext);
        _nrootContext.setSession(session);
        //add default commands
        List<NshBuiltin> allCommand = new ArrayList<>();
        NutsSupportLevelContext<NutsJavaShell> constraints = new NutsDefaultSupportLevelContext<>(ws,this);

        for (NshBuiltin command : this.appContext.getWorkspace().extensions().
                createServiceLoader(NshBuiltin.class, NutsJavaShell.class, NshBuiltin.class.getClassLoader(), session)
                .loadAll(this)) {
            NshBuiltin old = (NshBuiltin) _rootContext.builtins().find(command.getName());
            if (old != null && old.getSupportLevel(constraints) >= command.getSupportLevel(constraints)) {
                continue;
            }
            allCommand.add(command);
        }
        _rootContext.builtins().set(allCommand.toArray(new JShellBuiltin[0]));
        _rootContext.getUserProperties().put(JShellContext.class.getName(), _rootContext);
        try {
            histFile = ws.locations().getStoreLocation(this.appId,
                    NutsStoreLocation.VAR).resolve(serviceName + ".history").toFile();
            hist.setHistoryFile(histFile);
            if (histFile.exists()) {
                hist.load(histFile);
            }
        } catch (Exception ex) {
            //ignore
            LOG.log(Level.SEVERE, "Error resolving history file", ex);
        }
        ws.userProperties().put(JShellHistory.class.getName(), hist);
    }

    public NutsWorkspace getWorkspace() {
        return this.appContext.getWorkspace();
    }

    public void executeCommand(String[] command, StringBuilder in, StringBuilder out, StringBuilder err) {
        ByteArrayPrintStream oout = new ByteArrayPrintStream();
        ByteArrayPrintStream oerr = new ByteArrayPrintStream();
        final NutsShellContext cc = (NutsShellContext) getRootContext().copy();
        NutsSessionTerminal tt = getWorkspace().io().term().getTerminal().copy();
        tt.setIn(new ByteArrayInputStream(in == null ? new byte[0] : in.toString().getBytes()));
        tt.setOut(oout);
        tt.setErr(oerr);
        cc.getSession().setTerminal(tt);
        executeCommand(command, true, true, true, cc);
        out.append(oout.toString());
        err.append(oerr.toString());
    }

    @Override
    public JShellContext createContext(JShellContext ctx) {
        return createContext((NutsShellContext) ctx, null, null, null, null);
    }

    public NutsShellContext createContext(NutsShellContext ctx, Node root, Node parent, JShellVariables env, String[] args) {
        return new NutsJavaShellEvalContext(this, args, root, parent, ctx, getWorkspace(), appContext.getSession(), env);
    }

    public void setWorkspace(NutsWorkspace workspace) {
        getRootNutsShellContext().setWorkspace(workspace);
    }

    List<String> boot_nonOptions = new ArrayList<>();
    boolean boot_interactive = false;
    boolean boot_command = false;
//        String command = null;
    long boot_startMillis;

    @Override
    protected void prepareExecuteShell(String[] args) {
        boot_startMillis = appContext.getStartTimeMillis();
        NutsSession session = getRootNutsShellContext().getSession();
        PrintStream out = session.out();
        PrintStream err = session.err();
        NutsCommandLine cmd = null;
        cmd = getWorkspace().commandLine().create(args).setAutoComplete(appContext.getAutoComplete());
        NutsArgument a;
        while (cmd.hasNext()) {
            if (boot_nonOptions.isEmpty()) {
                if ((a = cmd.next("--help")) != null) {
                    boot_command = true;
                    boot_nonOptions.add("help");
                } else if (appContext != null && appContext.configureFirst(cmd)) {
                    //ok
                } else if ((a = cmd.nextString("-c", "--command")) != null) {
                    boot_command = true;
                    String cc = a.getStringValue();
                    if (StringUtils.isBlank(cc)) {
                        cmd.required("missing command for -c");
                    }
                    boot_nonOptions.add(cc);
                    boot_nonOptions.addAll(Arrays.asList(cmd.toStringArray()));
                    cmd.skipAll();
                } else if ((a = cmd.nextBoolean("-i", "--interactive")) != null) {
                    boot_interactive = a.getBooleanValue();
                } else if ((a = cmd.nextBoolean("-x")) != null) {
                    getOptions().setXtrace(a.getBooleanValue());
                } else if (cmd.peek().isOption()) {
                    cmd.setCommandName("nsh").unexpectedArgument();
                } else {
                    boot_nonOptions.add(cmd.next().getString());
                }
            } else {
                boot_nonOptions.add(cmd.next().getString());
            }
        }
        if (boot_nonOptions.isEmpty()) {
            boot_interactive = true;
        }
        if (!cmd.isExecMode()) {
            return;
        }
        if (appContext != null) {
            getRootNutsShellContext().getSession().getTerminal().setOutMode(appContext.getSession().getTerminal().getOutMode());
            getRootNutsShellContext().getSession().getTerminal().setErrMode(appContext.getSession().getTerminal().getErrMode());
        }
        getRootNutsShellContext().setSession(getRootNutsShellContext().getSession());
    }

    @Override
    public void executeShell(String[] args) {
        prepareExecuteShell(args);
        if (!(getWorkspace().commandLine().create(args).setAutoComplete(appContext.getAutoComplete())).isExecMode()) {
            return;
        }
        executeFile(getStartupScript(), getRootContext(), true);
        if (boot_nonOptions.size() > 0) {
            String c = boot_nonOptions.get(0);
            if (!boot_command) {
                boot_nonOptions.remove(0);
                getRootContext().setArgs(boot_nonOptions.toArray(new String[0]));
                executeFile(c, getRootContext(), false);
            } else {
                executeCommand(boot_nonOptions.toArray(new String[0]));
            }
            return;
        }
        if (boot_interactive) {
            try {
                executeInteractive(getRootContext().out());
            } finally {
                executeFile(getShutdownScript(), getRootContext(), true);
            }
        }
    }

    @Override
    protected String getPromptString(JShellContext context) {
        NutsWorkspace ws = getWorkspace();
//        String wss = ws == null ? "" : new File(getRootContext().getAbsolutePath(ws.config().getWorkspaceLocation().toString())).getName();
        String login = null;
        if (ws != null) {
            login = ws.security().getCurrentUsername();
        }
        String prompt = ((login != null && login.length() > 0 && !"anonymous".equals(login)) ? (login + "@") : "");//+ wss;
        if (!StringUtils.isBlank(getRootContext().getServiceName())) {
            prompt = prompt + "@" + getRootContext().getServiceName();
        }
        prompt += "> ";
        return prompt;
    }

    @Override
    protected String readInteractiveLine(JShellContext context) {
        NutsSessionTerminal terminal = null;
        terminal = getRootNutsShellContext().getSession().getTerminal();
        return terminal.readLine(getPromptString(context));
    }

    @Override
    protected void onQuit(JShellQuitException q) {
        throw new NutsExecutionException(getWorkspace(), q.getMessage(), q.getResult());
    }

    @Override
    protected void printHeader(PrintStream out) {
        out.printf("==nuts== shell (**Network Updatable Things Services**) [[v%s]] (c) vpc 2018\n",
                getWorkspace().getRuntimeId().getVersion().toString());
    }

    //    @Override
    @Override
    public String getVersion() {
        return PomIdResolver.resolvePomId(getClass(), new PomId("", "", "dev")).getVersion();
    }

    public NutsShellContext getRootNutsShellContext() {
        return (NutsShellContext) super.getRootContext();
    }

    @Override
    protected JShellContext createRootContext() {
        return new NutsJavaShellEvalContext(this, null, null, null, null, getWorkspace(), appContext.getSession(), null);
    }

}
