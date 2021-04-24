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
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.common.mvn.PomId;
import net.thevpc.common.mvn.PomIdResolver;
import net.thevpc.common.strings.StringUtils;
import net.thevpc.jshell.*;
import net.thevpc.nuts.*;

import java.io.File;
import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NutsJavaShell extends JShell {

    private static final Logger LOG = Logger.getLogger(NutsJavaShell.class.getName());
    //        String forCommand = null;
    long boot_startMillis;
    private NutsApplicationContext appContext;
    private File histFile = null;
    private NutsId appId = null;
    private NutsWorkspace workspace = null;

    public NutsJavaShell(NutsApplicationContext appContext, String[] args) {
        this(appContext, null, appContext.getSession(), null, null, args);
    }

    public NutsJavaShell(NutsWorkspace workspace, String[] args) {
        this(workspace.apps().createApplicationContext(new String[]{}, Nsh.class, null, 0, null), workspace, null, null, null, args);
    }

    public NutsJavaShell(NutsWorkspace workspace, NutsSession session, NutsId appId, String[] args) {
        this(workspace.apps().createApplicationContext(new String[]{}, Nsh.class, null, 0, session), workspace, session, appId, null, args);
    }

    public NutsJavaShell(NutsWorkspace workspace, NutsSession session, NutsId appId, String serviceName, String[] args) {
        this(workspace.apps().createApplicationContext(new String[]{}, Nsh.class, null, 0, session), workspace, session, appId, serviceName, args);
    }

    private NutsJavaShell(NutsApplicationContext appContext, NutsWorkspace workspace, NutsSession session, NutsId appId, String serviceName, String[] args) {
        super(resolveServiceName(appContext, serviceName, appId), resolveArgs(appContext, args), new NshOptionsParser(appContext),
                new NshEvaluator(), new NutsCommandTypeResolver(), new NutsErrorHandler(), new NutsExternalExecutor(),
                null
        );
        boot_startMillis = appContext.getStartTimeMillis();
        this.appContext = appContext;
        this.appId = appId;
        //super.setCwd(workspace.getConfigManager().getCwd());
        if (session == null) {
            session = this.appContext.getWorkspace().createSession();
        }
        if (workspace != null) {
            this.workspace = workspace;
        } else {
            this.workspace = appContext.getWorkspace();
        }

        if (this.appId == null) {
            this.appId = getWorkspace().id().setSession(session).resolveId(NutsJavaShell.class);
        }
        if (this.appId == null) {
            throw new IllegalArgumentException("unable to resolve application id");
        }
        NutsShellContext _nrootContext = getRootNutsShellContext();
        JShellFileContext _rootContext = getRootContext();
        NutsWorkspace ws = this.getWorkspace();
        JShellHistory hist = getHistory();

        this.appContext.getWorkspace().env().setProperty(JShellFileContext.class.getName(), _rootContext
        );
        _nrootContext.setSession(session);
        //add default commands
        List<NshBuiltin> allCommand = new ArrayList<>();
        NutsSupportLevelContext<NutsJavaShell> constraints = new NutsDefaultSupportLevelContext<>(session, this);

        for (NshBuiltin command : this.appContext.getWorkspace().extensions().
                createServiceLoader(NshBuiltin.class, NutsJavaShell.class, NshBuiltin.class.getClassLoader())
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
            histFile = Paths.get(ws.locations().getStoreLocation(this.appId,
                    NutsStoreLocation.VAR)).resolve(serviceName + ".history").toFile();
            hist.setHistoryFile(histFile);
            if (histFile.exists()) {
                hist.load(histFile);
            }
        } catch (Exception ex) {
            //ignore
            LOG.log(Level.SEVERE, "error resolving history file", ex);
        }
        ws.env().setProperty(JShellHistory.class.getName(), hist);
    }

    private static String[] resolveArgs(NutsApplicationContext appContext, String[] args) {
        if (args != null) {
            return args;
        }
        return appContext.getArguments();
    }

    private static String resolveServiceName(NutsApplicationContext appContext, String serviceName, NutsId appId) {
        if ((serviceName == null || serviceName.trim().isEmpty())) {
            if (appId == null) {
                appId = appContext.getWorkspace().id().setSession(appContext.getSession()).resolveId(NutsJavaShell.class);
            }
            serviceName = appId.getArtifactId();
        }
        return serviceName;
    }

    public NutsShellContext getNutsShellContext() {
        JShellFileContext f = (JShellFileContext) workspace.env().getProperty(JShellFileContext.class.getName());
        return (NutsShellContext) f.getShellContext();

    }

    public NutsSession getSession() {
        return getNutsShellContext().getSession();
    }

    public void setSession(NutsSession session) {
        getNutsShellContext().setSession(session);
    }

    public NutsWorkspace getWorkspace() {
        return this.workspace;
    }

    public void setWorkspace(NutsWorkspace workspace) {
        getRootNutsShellContext().setWorkspace(workspace);
    }

    public NutsShellContext createContext(NutsShellContext ctx, JShellNode root, JShellNode parent, JShellVariables env) {
        return new DefaultNutsShellContext(this, root, parent, ctx, getWorkspace(), appContext.getSession(), env);
    }

    @Override
    protected JShellContext createRootContext() {
        return createContext(null, null, null, null);
    }

    @Override
    public JShellContext createContext(JShellContext ctx) {
        return createContext((NutsShellContext) ctx, null, null, null);
    }

    @Override
    public void run() {
        try {
            if (appContext.getAutoComplete() != null) {
                return;
            }
            super.run();
        } catch (NutsExecutionException ex) {
            throw ex;
        } catch (JShellException ex) {
            throw new NutsExecutionException(appContext.getSession(), ex.getMessage(), ex, ex.getResult());
        } catch (Exception ex) {
            throw new NutsExecutionException(appContext.getSession(), ex.getMessage(), ex, 100);
        }
    }

    @Override
    protected String readInteractiveLine(JShellFileContext context) {
        NutsSessionTerminal terminal = null;
        terminal = getRootNutsShellContext().getSession().getTerminal();
        return terminal.readLine(getPromptString(context));
    }

    @Override
    protected void printHeader(PrintStream out) {
        out.println(appContext.getWorkspace().formats().text().builder()
                .appendCode("sh", "nuts")
                .append(" shell ")
                .append("v" + getWorkspace().getRuntimeId().getVersion().toString(), NutsTextStyle.version())
                .append(" (c) thevpc 2020"));
    }

    @Override
    protected void executeInteractive(JShellFileContext context) {
        appContext.getWorkspace().term().enableRichTerm();
        appContext.getWorkspace().term().getSystemTerminal()
                .setCommandAutoCompleteResolver(new NshAutoCompleter())
                .setCommandHistory(
                        appContext.getWorkspace().commandLine().createHistory()
                                .setPath(Paths.get(appContext.getVarFolder()).resolve("nsh-history.hist"))
                                .build()
                )
                .setCommandReadHighlighter(new NutsCommandReadHighlighter() {
                    @Override
                    public NutsText highlight(String buffer, NutsSession session) {
                        return session.getWorkspace().formats().text().forCode("sh", buffer).parse(session);
                    }
                });
        super.executeInteractive(context);
    }

    @Override
    protected void onQuit(JShellQuitException q) {
        throw new NutsExecutionException(getSession(), q.getMessage(), q.getResult());
    }

    @Override
    protected String getPromptString(JShellFileContext context) {
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

    public NshOptions getOptions() {
        return (NshOptions) super.getOptions();
    }

    //    @Override
    @Override
    public String getVersion() {
        return PomIdResolver.resolvePomId(getClass(), new PomId("", "", "dev")).getVersion();
    }

    public NutsShellContext getRootNutsShellContext() {
        return (NutsShellContext) super.getRootContext().getShellContext();
    }

    private static class NshAutoCompleter implements NutsCommandAutoCompleteResolver {

        @Override
        public List<NutsArgumentCandidate> resolveCandidates(NutsCommandLine commandline, int wordIndex, NutsSession session) {
            NutsWorkspace workspace = session.getWorkspace();
            List<NutsArgumentCandidate> candidates = new ArrayList<>();
            JShellFileContext fileContext = (JShellFileContext) workspace.env().getProperty(JShellFileContext.class.getName());
            NutsShellContext nutsConsoleContext = (NutsShellContext) fileContext.getShellContext();

            if (wordIndex == 0) {
                for (JShellBuiltin command : nutsConsoleContext.builtins().getAll()) {
                    candidates.add(workspace.commandLine().createCandidate(command.getName()).build());
                }
            } else {
                List<String> autoCompleteWords = new ArrayList<>(Arrays.asList(commandline.toStringArray()));
                int x = commandline.getCommandName().length();

                List<JShellAutoCompleteCandidate> autoCompleteCandidates
                        = nutsConsoleContext.resolveAutoCompleteCandidates(commandline.getCommandName(), autoCompleteWords, wordIndex, commandline.toString(), fileContext);
                for (Object cmdCandidate0 : autoCompleteCandidates) {
                    JShellAutoCompleteCandidate cmdCandidate = (JShellAutoCompleteCandidate) cmdCandidate0;
                    if (cmdCandidate != null) {
                        String value = cmdCandidate.getValue();
                        if (!StringUtils.isBlank(value)) {
                            String display = cmdCandidate.getDisplay();
                            if (StringUtils.isBlank(display)) {
                                display = value;
                            }
                            candidates.add(workspace.commandLine().createCandidate(value).setDisplay(display).build());
                        }
                    }
                }
            }
            return candidates;
        }
    }

}
