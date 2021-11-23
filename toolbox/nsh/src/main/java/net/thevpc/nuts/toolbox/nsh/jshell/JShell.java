/**
 * ====================================================================
 * Doovos (Distributed Object Oriented Operating System)
 * <p>
 * Doovos is a new Open Source Distributed Object Oriented Operating System
 * Design and implementation based on the Java Platform. Actually, it is a try
 * for designing a distributed operation system in top of existing
 * centralized/network OS. Designed OS will follow the object oriented
 * architecture for redefining all OS resources (memory,process,file
 * system,device,...etc.) in a highly distributed context. Doovos is also a
 * distributed Java virtual machine that implements JVM specification on top the
 * distributed resources context.
 * <p>
 * Doovos BIN is a standard implementation for Doovos boot sequence, shell and
 * common application tools. These applications are running onDoovos guest JVM
 * (distributed jvm).
 * <br>
 * <p>
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
package net.thevpc.nuts.toolbox.nsh.jshell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsDefaultSupportLevelContext;
import net.thevpc.nuts.spi.NutsSupportLevelContext;
import net.thevpc.nuts.toolbox.nsh.*;
import net.thevpc.nuts.toolbox.nsh.jshell.parser.JShellParser;
import net.thevpc.nuts.toolbox.nsh.jshell.util.ByteArrayPrintStream;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class JShell {

    public static final String APP_TITLE = "JavaShell";
    public static final String APP_VERSION = "0.4";
    public static final int NEXT_STATEMENT = -2;
    public static final String ENV_PATH = "PATH";
    public static final String ENV_HOME = "HOME";
    public static final String ENV_EXEC_PACKAGES = "EXEC_PKG";
    public static final String ENV_EXEC_EXTENSIONS = "EXEC_EXT";
    private static final Logger LOG = Logger.getLogger(JShell.class.getName());
    private final JShellOptions options;
    private final JShellHistory history;
    private final BufferedReader _in_reader = null;
    private final List<JShellVarListener> listeners = new ArrayList<>();
    public boolean fallBackToMain = false;
    public Object shellInterpreter = null;
    protected JShellContext rootContext;
    long boot_startMillis;
    private String version = "1.0.0";
    private JShellEvaluator evaluator;
    private JShellErrorHandler errorHandler;
    private JShellExternalExecutor externalExecutor;
    private JShellCommandTypeResolver commandTypeResolver;
    private NutsApplicationContext appContext;
    private NutsPath histFile = null;
    private NutsId appId = null;

    public JShell(NutsApplicationContext appContext, String[] args) {
        this(appContext, null, null, args);
    }

    public JShell(NutsSession session, String[] args) {
        this(NutsApplicationContext.of(new String[]{}, 0, Nsh.class, null, session), null, null, args);
    }

    public JShell(NutsSession session, NutsId appId, String[] args) {
        this(NutsApplicationContext.of(new String[]{}, 0, Nsh.class, null, session), appId, null, args);
    }

    public JShell(NutsSession session, NutsId appId, String serviceName, String[] args) {
        this(NutsApplicationContext.of(new String[]{}, 0, Nsh.class, null, session), appId, serviceName, args);
    }

    private JShell(NutsApplicationContext appContext, NutsId appId, String serviceName, String[] args) {
        this(resolveServiceName(appContext, serviceName, appId), resolveArgs(appContext, args), new DefaultJShellOptionsParser(appContext),
                new NshEvaluator(), new NutsCommandTypeResolver(), new NutsErrorHandler(), new NutsExternalExecutor(),
                null
        );
        boot_startMillis = appContext.getStartTimeMillis();
        this.appContext = appContext;
        this.appId = appId;
        //super.setCwd(workspace.getConfigManager().getCwd());
        if (this.appId == null) {
            this.appId = NutsIdResolver.of(appContext.getSession()).resolveId(JShell.class);
        }
        if (this.appId == null) {
            throw new IllegalArgumentException("unable to resolve application id");
        }
        JShellContext _rootContext = getRootContext();
        NutsSession ws = _rootContext.getSession();
        JShellHistory hist = getHistory();

        this.appContext.getSession().env().setProperty(JShellContext.class.getName(), _rootContext);
        _rootContext.setSession(appContext.getSession());
        //add default commands
        List<JShellBuiltin> allCommand = new ArrayList<>();
        NutsSupportLevelContext constraints = new NutsDefaultSupportLevelContext(appContext.getSession(), this);

        for (JShellBuiltin command : this.appContext.getSession().extensions().
                createServiceLoader(JShellBuiltin.class, JShell.class, JShellBuiltin.class.getClassLoader())
                .loadAll(this)) {
            JShellBuiltin old = _rootContext.builtins().find(command.getName());
            if (old != null && old.getSupportLevel(constraints) >= command.getSupportLevel(constraints)) {
                continue;
            }
            allCommand.add(command);
        }
        _rootContext.builtins().set(allCommand.toArray(new JShellBuiltin[0]));
        _rootContext.getUserProperties().put(JShellContext.class.getName(), _rootContext);
        try {
            histFile = ws.locations().getStoreLocation(this.appId,
                            NutsStoreLocation.VAR).resolve(serviceName + ".history");
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


    public JShell() {
        this(null, null, null, null, null, null, null, null);
    }

    public JShell(String serviceName, String[] args, JShellOptionsParser shellOptionsParser,
                  JShellEvaluator evaluator, JShellCommandTypeResolver commandTypeResolver,
                  JShellErrorHandler errorHandler,
                  JShellExternalExecutor externalExecutor,
                  JShellHistory history
    ) {
        if (commandTypeResolver == null) {
            this.commandTypeResolver = new DefaultJShellCommandTypeResolver();
        } else {
            this.commandTypeResolver = commandTypeResolver;
        }
        if (errorHandler == null) {
            this.errorHandler = new DefaultJShellErrorHandler();
        } else {
            this.errorHandler = errorHandler;
        }
        if (evaluator == null) {
            this.evaluator = new DefaultJShellEvaluator();
        } else {
            this.evaluator = evaluator;
        }
        if (history == null) {
            this.history = new DefaultJShellHistory();
        } else {
            this.history = history;
        }
        if (shellOptionsParser == null) {
            shellOptionsParser = new DefaultJShellOptionsParser(appContext);
        }
        options = shellOptionsParser.parse(args);
        this.externalExecutor = externalExecutor;
        if (options.getServiceName() == null) {
            options.setServiceName(serviceName == null ? "jshell" : serviceName);
        }
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
                appId = NutsIdResolver.of(appContext.getSession()).resolveId(JShell.class);
            }
            serviceName = appId.getArtifactId();
        }
        return serviceName;
    }

    public void addVarListener(JShellVarListener listener) {
        this.listeners.add(listener);
    }

    public void removeVarListener(JShellVarListener listener) {
        this.listeners.add(listener);
    }

    public JShellVarListener[] getVarListeners() {
        return listeners.toArray(new JShellVarListener[0]);
    }

    public JShellEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(JShellEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public JShellCommandTypeResolver getCommandTypeResolver() {
        return commandTypeResolver;
    }

    public void setCommandTypeResolver(JShellCommandTypeResolver whichResolver) {
        this.commandTypeResolver = whichResolver;
    }

    public JShellExternalExecutor getExternalExecutor() {
        return externalExecutor;
    }

    public void setExternalExecutor(JShellExternalExecutor externalExecutor) {
        this.externalExecutor = externalExecutor;
    }

    public JShellErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(JShellErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public List<String> findFiles(final String namePattern, boolean exact, String parent, NutsSession session) {
        if (exact) {
            String[] all = NutsPath.of(parent, session).list()
                    .filter(x -> namePattern.equals(x.getName()),"name='"+namePattern+"'")
                    .map(NutsPath::toString,"toString").toArray(String[]::new);
            return Arrays.asList(all);
        } else {
            final Pattern o = Pattern.compile(namePattern);
            String[] all = NutsPath.of(parent, session).list()
                    .filter(x -> o.matcher(x.getName()).matches(),"name~~'"+namePattern+"'")
                    .map(NutsPath::toString,"toString").toArray(String[]::new);
            return Arrays.asList(all);
        }
    }

    //    protected JShellContext createRootContext() {
//        return new DefaultJShellContext(this);
//    }
    protected JShellContext createRootContext(String serviceName, String[] args) {
        return createContext(null, null, null, null, serviceName, args);
    }

    public JShellContext createNewContext(JShellContext parentContext) {
        return createNewContext(parentContext, parentContext.getServiceName(), parentContext.getArgsArray());
    }

    public JShellContext createNewContext(JShellContext ctx, String serviceName, String[] args) {
        return createContext(ctx, null, null, null, serviceName, args);
    }

    public JShellContext createInlineContext(JShellContext ctx, String serviceName, String[] args) {
        if (ctx == null) {
            ctx = getRootContext();
        }
        JShellContextForSource c = new JShellContextForSource(ctx);
        c.setServiceName(serviceName);
        c.setArgs(args);
        return c;
    }

    public JShellCommandNode createCommandNode(String[] args) {
        return JShellParser.createCommandNode(args);
    }

    public JShellContext getRootContext() {
        if (rootContext == null) {
            rootContext = createRootContext(options.getServiceName(), options.getCommandArgs().toArray(new String[0]));
        }
        return rootContext;
    }

    public void executeLine(String line, boolean storeResult, JShellContext context) {
        if (context == null) {
            context = getRootContext();
        }
        boolean success = false;
        if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
            try {
                getHistory().add(line);
                JShellCommandNode nn = parseScript(line);
                context.getShell().evalNode(nn, context);
                success = true;
            } catch (JShellQuitException e) {
                throw e;
            } catch (Throwable e) {
                if (storeResult) {
                    onResult(e, context);
                } else {
                    if (e instanceof RuntimeException) {
                        throw e;
                    }
                    if (e instanceof Error) {
                        throw e;
                    }
                    throw new RuntimeException(e);
                }
            }
            if (storeResult) {
                if (success) {
                    onResult(null, context);
                    try {
                        history.save();
                    } catch (Exception e) {
                        //e.printStackTrace();
                    }
                }
            }
        }
    }

    public int onResult(int r, JShellContext context) {
        context.setLastResult(new JShellResult(r, null, null));
        return r;
    }

    public int onResult(Throwable th, JShellContext context) {
        if (th == null) {
            context.setLastResult(new JShellResult(0, null, null));
            return 0;
        }
        if (th instanceof JShellQuitException) {
            throw (JShellQuitException) th;
        }
        if (getErrorHandler().isQuitException(th)) {
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            throw new JShellQuitException(context.getSession(), th, 100);
        }

        if (th instanceof JShellException) {
            JShellException je = (JShellException) th;
            int errorCode = je.getExitCode();
            String lastErrorMessage = getErrorHandler().errorToMessage(th);
            context.setLastResult(new JShellResult(errorCode, lastErrorMessage, th));
            if (errorCode != 0) {
                getErrorHandler().onError(lastErrorMessage, th, context);
            }
            return errorCode;
        }

        int errorCode = getErrorHandler().errorToCode(th);
        String lastErrorMessage = getErrorHandler().errorToMessage(th);
        context.setLastResult(new JShellResult(errorCode, lastErrorMessage, th));
        if (errorCode != 0) {
            getErrorHandler().onError(lastErrorMessage, th, context);
        }
        return errorCode;
    }

    public int onResult(int errorCode, Throwable th, JShellContext context) {
        if (errorCode != 0) {
            if (th == null) {
                th = new RuntimeException("error occurred. Error Code #" + errorCode);
            }
        } else {
            th = null;
        }
        String lastErrorMessage = th == null ? null : getErrorHandler().errorToMessage(th);
        context.setLastResult(new JShellResult(errorCode, lastErrorMessage, th));
        if (errorCode != 0) {
            getErrorHandler().onError(lastErrorMessage, th, context);
        }
        return errorCode;
    }

    public int executeCommand(String[] command, JShellContext context) {
        context.setServiceName(command[0]);
        context.setArgs(Arrays.copyOfRange(command, 1, command.length));
        return context.getShell().evalNode(createCommandNode(command), context);
    }

    public void addToHistory(String[] command) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < command.length; i++) {
            String arg = command[i];
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
    }

    public int executePreparedCommand(String[] command,
                                      boolean considerAliases, boolean considerBuiltins, boolean considerExternal,
                                      JShellContext context
    ) {
        context.getShell().traceExecution(() -> String.join(" ", command), context);
        String cmdToken = command[0];
        NutsPath cmdPath = NutsPath.of(cmdToken, context.getSession());
        if (!cmdPath.isName()) {
            final JShellExternalExecutor externalExec = getExternalExecutor();
            if (externalExec == null) {
                throw new JShellException(context.getSession(), NutsMessage.cstyle("not found %s", cmdToken), 101);
            }
            return externalExec.execExternalCommand(command, context);
            //this is a path!
        } else {
            List<String> cmds = new ArrayList<>(Arrays.asList(command));
            String a = considerAliases ? context.aliases().get(cmdToken) : null;
            if (a != null) {
                JShellNode node0 = null;
                try {
//                    JShellParser parser = new JShellParser();
//                    node0 = parser.parse(a);

                    node0 = JShellParser.fromString(a).parse();

                } catch (Exception ex) {
                    Logger.getLogger(JShell.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (node0 instanceof JShellCommandLineNode) {
                    JShellCommandLineNode nn = (JShellCommandLineNode) node0;
                    List<String> newCmd = new ArrayList<>();
                    for (JShellArgumentNode item : nn) {
                        newCmd.addAll(Arrays.asList(item.evalString(context)));
                    }
                    for (int i = 1; i < cmds.size(); i++) {
                        newCmd.add(cmds.get(i));
                    }
                    cmds.clear();
                    cmds.addAll(newCmd);
                } else {
                    throw new IllegalArgumentException("invalid  alias " + a);
                }
            } else {
                a = cmdToken;
            }
            JShellBuiltin shellCommand = considerBuiltins ? context.builtins().find(a) : null;
            if (shellCommand != null && shellCommand.isEnabled()) {
                ArrayList<String> arg2 = new ArrayList<String>(cmds);
                arg2.remove(0);
                shellCommand.exec(arg2.toArray(new String[0]), context.createCommandContext(shellCommand));
            } else {
                if (considerExternal) {
                    final JShellExternalExecutor externalExec = getExternalExecutor();
                    if (externalExec == null) {
                        throw new JShellException(context.getSession(), NutsMessage.cstyle("not found %s", cmdToken), 101);
                    }
                    externalExec.execExternalCommand(cmds.toArray(new String[0]), context);
                } else {
                    throw new JShellException(context.getSession(), NutsMessage.cstyle("not found %s", cmdToken), 101);
                }
            }
        }
        return 0;
    }

//    protected String readInteractiveLine(JShellFileContext context) {
//        if (_in_reader == null) {
//            _in_reader = new BufferedReader(new InputStreamReader(System.in));
//        }
//        try {
//            return _in_reader.readLine();
//        } catch (IOException ex) {
//            throw new UncheckedIOException(ex);
//        }
//    }

    public void run() {
        try {
            if (appContext.getAutoComplete() != null) {
                return;
            }
            JShellContext rootContext = getRootContext();
            if (getOptions().isHelp()) {
                executeHelp(rootContext);
                return;
            }
            if (getOptions().isVersion()) {
                executeVersion(rootContext);
                return;
            }
            if (getOptions().isStdInAndPos()) {
                if (getOptions().getCommandArgs().isEmpty()) {
                    //ok
                    executeInteractive(rootContext);
                } else {
                    rootContext.err().println("-s option not supported yet. ignored");
                    executeInteractive(rootContext);
                }
                if (getOptions().isInteractive()) {
                    executeInteractive(rootContext);
                }
                return;
            }

            if (getOptions().isCommand()) {
                executeCommand(getOptions().getCommandArgs().toArray(new String[0]), rootContext);
                if (getOptions().isInteractive()) {
                    executeInteractive(rootContext);
                }
                return;
            }

            if (!getOptions().getFiles().isEmpty()) {
                for (String file : getOptions().getFiles()) {
                    executeServiceFile(createNewContext(rootContext, file, getOptions().getCommandArgs().toArray(new String[0])), false);
                }
                if (getOptions().isInteractive()) {
                    executeInteractive(rootContext);
                }
                return;
            }
            executeInteractive(rootContext);
        } catch (NutsExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NutsExecutionException(appContext.getSession(), NutsMessage.cstyle("%s", ex), ex, 100);
        }
    }

    protected String readInteractiveLine(JShellContext context) {
        NutsSessionTerminal terminal = context.getSession().getTerminal();
        return terminal.readLine(getPromptString(context));
    }

    protected void printHeader(NutsPrintStream out) {
        out.resetLine().println(NutsTexts.of(appContext.getSession()).builder()
                .appendCode("sh", "nuts")
                .append(" shell ")
                .append("v" + getRootContext().getWorkspace().getRuntimeId().getVersion().toString(), NutsTextStyle.version())
                .append(" (c) thevpc 2020"));
    }

    protected void executeHelp(JShellContext context) {
        context.out().println("Syntax : shell [<FILE>]\n");
        context.out().println("    <FILE> : if present content will be processed as input\n");
    }

    protected void executeVersion(JShellContext context) {
        context.out().printf("v%s\n", APP_VERSION);
    }

    protected void executeInteractive(JShellContext context) {
        NutsSession session = appContext.getSession();
        NutsSystemTerminal.enableRichTerm(session);
        session.config().getSystemTerminal()
                .setCommandAutoCompleteResolver(new NshAutoCompleter())
                .setCommandHistory(
                        NutsCommandHistory.of(session)
                                .setPath(appContext.getVarFolder().resolve("nsh-history.hist"))
                );
        prepareContext(getRootContext());
        printHeader(context.out());
        if (getOptions().isLogin()) {
            executeLoginScripts();
        }

        while (true) {
            String line = null;
            try {
                line = readInteractiveLine(context);
            } catch (Exception ex) {
                onResult(ex, context);
                break;
            }
            if (line == null) {
                break;
            }
            if (line.trim().length() > 0) {
                try {
                    executeLine(line, true, context);
                } catch (JShellQuitException q) {
                    if (getOptions().isLogin()) {
                        executeLogoutScripts();
                    }
                    if (q.getExitCode() == 0) {
                        return;
                    }
                    onQuit(q);
                    return;
                }
            }
        }
        if (getOptions().isLogin()) {
            executeLogoutScripts();
        }
        onQuit(new JShellQuitException(session, 1));
    }

    private void executeLoginScripts() {
        if (!getOptions().isNoProfile()) {
            for (String profileFile : new String[]{
                    "/etc/profile",
                    (getOptions().isPosix()) ? null : "~/.bash_profile",
                    (getOptions().isPosix()) ? null : "~/.bash_login",
                    "~/.profile",
                    getOptions().isBash() || getOptions().isPosix() ? null : getOptions().getStartupScript()
            }) {
                if (profileFile != null) {
                    if (profileFile.startsWith("~/") || profileFile.startsWith("~\\")) {
                        profileFile = System.getProperty("user.home") + profileFile.substring(1);
                    }
                    executeServiceFile(createNewContext(getRootContext(), profileFile, new String[0]), true);
                }
            }
        }
    }

    private void executeLogoutScripts() {
        if (!getOptions().isNoProfile()) {
            for (String profileFile : new String[]{
                    (getOptions().isPosix()) ? null : "~/.bash_logout",
                    (getOptions().isBash() || getOptions().isPosix()) ? null : getOptions().getStartupScript()
            }) {
                if (profileFile != null) {
                    if (profileFile.startsWith("~/") || profileFile.startsWith("~\\")) {
                        profileFile = System.getProperty("user.home") + profileFile.substring(1);
                    }
                    executeServiceFile(createNewContext(getRootContext(), profileFile, new String[0]), true);
                }
            }
        }
    }

    protected void onQuit(JShellQuitException quitException) {
        try {
            getHistory().save();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        throw new NutsExecutionException(getRootContext().getSession(), NutsMessage.cstyle("%s", quitException), quitException.getExitCode());
//        throw quitException;
    }

    public int executeServiceFile(JShellContext context, boolean ignoreIfNotFound) {
        NutsSession session = appContext.getSession();
        String file = context.getServiceName();
        if (file != null) {
            file = NutsPath.of(file, session).toAbsolute(context.getCwd()).toString();
        }
        if (file == null || !NutsPath.of(file, session).exists()) {
            if (ignoreIfNotFound) {
                return 0;
            }
            throw new JShellException(session, NutsMessage.cstyle("shell file not found : %s", file), 1);
        }
        context.setServiceName(file);
        FileInputStream stream = null;
        try {
            try {
                stream = new FileInputStream(file);
                JShellCommandNode ii = parseScript(stream);
                if (ii == null) {
                    return 0;
                }
                JShellContext c = context.setRootNode(ii);//.setParent(null);
                return context.getShell().evalNode(ii, c);
            } catch (IOException ex) {
                throw new JShellException(session, ex, 1);
            }
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                throw new JShellException(session, ex, 1);
            }
        }
    }

    public int executeScript(String text, JShellContext context) {
        if (context == null) {
            context = getRootContext();
        }
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        JShellCommandNode ii = parseScript(text);
        if (ii == null) {
            return 0;
        }
        JShellContext c = context.setRootNode(ii);//.setParent(null);
        return evalNode(ii, c);
    }

    public int evalNode(JShellCommandNode node, JShellContext context) {
        try {
            int r = node.eval(context);
            onResult(r, context);
            return r;
        } catch (JShellUniformException th) {
            if (th.isQuit()) {
                onResult(null, context);
                th.throwQuit();
                return 0;
            } else {
                onResult(th, context);
                throw th;
            }
        } catch (JShellQuitException th) {
            throw th;
        } catch (Exception th) {
            if (getErrorHandler().isQuitException(th)) {
                onResult(null, context);
                throw new JShellUniformException(context.getSession(), getErrorHandler().errorToCode(th), true, th);
            }
            onResult(th, context);
            context.err().printf("error: %s%n", th);
            return getErrorHandler().errorToCode(th);
        }
    }

    public int safeEval(JShellCommandNode n, JShellContext context) {
        boolean success = false;
        try {
            n.eval(context);
            success = true;
        } catch (Exception ex2) {
            return onResult(ex2, context);
        }
        if (success) {
            return onResult(null, context);
        }
        throw new IllegalArgumentException("Unexpected behaviour");
    }

    //    public String getPromptString() {
//        return getPromptString(getRootContext());
//    }
    protected String getPromptString(JShellContext context) {
        NutsSession ws = context.getSession();
//        String wss = ws == null ? "" : new File(getRootContext().getAbsolutePath(ws.config().getWorkspaceLocation().toString())).getName();
        String login = null;
        if (ws != null) {
            login = ws.security().getCurrentUsername();
        }
        String prompt = ((login != null && login.length() > 0 && !"anonymous".equals(login)) ? (login + "@") : "");//+ wss;
        if (!NutsBlankable.isBlank(getRootContext().getServiceName())) {
            prompt = prompt + "@" + getRootContext().getServiceName();
        }
        prompt += "> ";
        return prompt;
    }

    protected String getPromptString0(JShellContext context) {

        String promptValue = context.vars().getAll().getProperty("PS1");
        if (promptValue == null) {
            promptValue = "\\u> ";
        }
        char[] promptChars = promptValue.toCharArray();
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < promptChars.length; i++) {
            char c = promptChars[i];
            if (c == '\\' && i < (promptChars.length - 1)) {
                i++;
                c = promptChars[i];
                switch (c) {
                    case 'W': {
                        s.append(context.getCwd());
                        break;
                    }
                    case 'u': {
                        s.append(context.vars().getAll().getProperty("USER", "anonymous"));
                        break;
                    }
                    case 'h': {
                        String h = context.vars().getAll().getProperty("HOST", "nowhere");
                        if (h.contains(".")) {
                            h = h.substring(0, h.indexOf('.'));
                        }
                        s.append(h);
                        break;
                    }
                    case 'H': {
                        s.append(context.vars().getAll().getProperty("HOST", "nowhere"));
                        break;
                    }
                    default: {
                        s.append('\\').append(c);
                        break;
                    }
                }
            } else {
                s.append(c);
            }
        }
        return s.toString();

    }


//    public String evalAsString(String param, JShellContext context) {
//        Properties envs = new Properties();
//        Properties processEnvs = context.vars().getAll();
//        for (Entry<Object, Object> entry : processEnvs.entrySet()) {
//            envs.put(entry.getKey(), entry.getValue());
//        }
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < param.length(); i++) {
//            char c = param.charAt(i);
//            if (c == '$') {
//                StringBuilder var = new StringBuilder();
//                i++;
//                if (i < param.length()) {
//                    if (param.charAt(i) != '{') {
//                        while (i < param.length()
//                                && ((param.charAt(i) >= 'a' && param.charAt(i) <= 'z')
//                                || (param.charAt(i) >= 'A' && param.charAt(i) <= 'Z')
//                                || (param.charAt(i) >= 'O' && param.charAt(i) <= '9')
//                                || (param.charAt(i) == '_'))) {
//                            var.append(param.charAt(i++));
//                        }
//                        i--;
//                    } else {
//                        i++;//ignore '{'
//                        while (i < param.length() && (param.charAt(i) != '}')) {
//                            var.append(param.charAt(i++));
//                        }
//                    }
//                } else {
//                    var.append('$');
//                }
//                Object obj = envs.get(var.toString());
//                sb.append(obj == null ? "" : String.valueOf(obj));
//            } else {
//                sb.append(c);
//            }
//        }
//        return sb.toString();
//    }
//
//    public String[] findExecFilesInPath(String filePath, String[] classNames, JShellContext context) {
//        ArrayList<String> found = new ArrayList<String>();
//        File f = new File(filePath);
//        if (!f.exists()) {
//            return new String[0];
//        }
//        if (f.isDirectory()) {
//            for (String ff : classNames) {
//                File f2 = new File(f, ff);
//                if (f2.exists()) {
//                    found.add(f2.getPath());
//                }
//            }
//        }
//        return found.toArray(new String[found.size()]);
//    }
//
//    public String[] findClassesInPath(String filePath, String[] classNames, JShellContext context) {
//        System.out.printf("findClassesInPath : path=%s should contain? %s\n", filePath, Arrays.asList(classNames).toString());
//        ArrayList<String> found = new ArrayList<String>();
//        String[] expanded = context.expandPaths(filePath/*, null*/);
//        System.out.printf("path=%s expanded to %s\n", filePath, Arrays.asList(expanded));
//        for (String fp : expanded) {
//            System.out.printf("\tfindClassesInPath : path=%s should contain? %s\n", fp, Arrays.asList(classNames));
//            File f = new File(fp);
//            if (f.exists()) {
//                String[] fileCls = new String[classNames.length];
//                for (int i = 0; i < fileCls.length; i++) {
//                    fileCls[i] = classNames[i].replace('.', '/') + ".class";
//
//                }
//                List<String> clsNames = Arrays.asList(fileCls);
//                if (f.isDirectory()) {
//                    for (String ff : fileCls) {
//                        if (new File(f, ff).exists()) {
//                            found.add(ff);
//                        }
//                    }
//                } else {
//                    ZipFile zipFile = null;
//                    boolean fileFound = false;
//                    try {
//                        System.out.printf("lookup into %s for %s\n", fp, clsNames);
//                        // open a zip file for reading
//                        zipFile = new ZipFile(fp);
//                        // get an enumeration of the ZIP file entries
//                        Enumeration<? extends ZipEntry> e = zipFile.entries();
//                        while (e.hasMoreElements()) {
//                            ZipEntry entry = e.nextElement();
//                            String entryName = entry.getName();
//                            for (String ff : fileCls) {
//                                if (entryName.equals(ff)) {
//                                    found.add(ff);
//                                    break;
//                                }
//                            }
//                            if (found.size() == classNames.length) {
//                                break;
//                            }
//                        }
//
//                    } catch (IOException ioe) {
//                        //return found;
//                    } finally {
//                        try {
//                            if (zipFile != null) {
//                                zipFile.close();
//                            }
//                        } catch (IOException ioe) {
//                            System.err.printf("Error while closing zip file %s\n", ioe);
//                        }
//                    }
//                }
//            }
//        }
//        return found.toArray(new String[found.size()]);
//    }

    public void prepareContext(JShellContext context) {
//        try {
//            cwd = new File(".").getCanonicalPath();
//        } catch (IOException ex) {
//            cwd = new File(".").getAbsolutePath();
//        }
        context.vars().set(System.getenv());
        setUndefinedStartupEnv("USER", System.getProperty("user.name"), context);
        setUndefinedStartupEnv("LOGNAME", System.getProperty("user.name"), context);
        setUndefinedStartupEnv(JShell.ENV_PATH, ".", context);
        setUndefinedStartupEnv("PWD", System.getProperty("user.dir"), context);
        setUndefinedStartupEnv(JShell.ENV_HOME, System.getProperty("user.home"), context);
        setUndefinedStartupEnv("PS1", ">", context);
        setUndefinedStartupEnv("IFS", " \t\n", context);
    }

    private void setUndefinedStartupEnv(String name, String defaultValue, JShellContext context) {
        if (context.vars().get(name) == null) {
            context.vars().set(name, defaultValue);
        }
    }

    public JShellScript parseScript(InputStream stream) {
        JShellNode node0 = null;
        try {
            node0 = JShellParser.fromInputStream(stream).parse();
            if (node0 == null) {
                return null;
            }
        } catch (Exception ex) {
            Logger.getLogger(JShell.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node0 instanceof JShellCommandNode) {
            return new JShellScript((JShellCommandNode) node0);
        }
        throw new IllegalArgumentException("expected node " + node0);
    }

    public JShellScript parseScript(String scriptString) {
        JShellNode node0 = null;
        try {
            node0 = JShellParser.fromString(scriptString).parse();
            if (node0 == null) {
                return null;
            }
        } catch (Exception ex) {
            Logger.getLogger(JShell.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node0 instanceof JShellCommandNode) {
            return new JShellScript((JShellCommandNode) node0);
        }
        throw new IllegalArgumentException("expected node " + scriptString);
    }
//    public String escapeStringForDoubleQuotes(String s) {
//        StringBuilder sb=new StringBuilder();
//        for (char c: s.toCharArray()) {
//            switch (c){
//                case '\\':
//                case '(':
//                case ')':
//                case '&':
//                case '|':
//                    {
//                    sb.append('\\');
//                    sb.append(c);
//                    break;
//                }
//                default:{
//                    sb.append(c);
//                }
//            }
//        }
//        return sb.toString();
//    }

    public String escapeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '\\':
                case '&':
                case '!':
                case '$':
                case '`':
                case '?':
                case '*':
                case '[':
                case ']': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public String escapePath(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            switch (c) {
                case '?':
                case '*':
                case '[':
                case ']': {
                    sb.append('\\');
                    sb.append(c);
                    break;
                }
                default: {
                    sb.append(c);
                }
            }
        }
        return sb.toString();
    }

    public void traceExecution(Supplier<String> msg, JShellContext context) {
        if (getOptions().isXtrace()) {
            String txt = msg.get();
            context.err().println("+ " + txt);
        }
    }

    public JShellOptions getOptions() {
        return options;
    }

    public JShellHistory getHistory() {
        return history;
    }

    public String getVersion() {
        NutsId nutsId = NutsIdResolver.of(appContext.getSession()).resolveId(getClass());
        if (nutsId == null) {
            return "dev";
        }
        return nutsId.getVersion().getValue();
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    public NutsApplicationContext getAppContext() {
        return appContext;
    }

    public MemResult executeCommand(String[] command) {
        return executeCommand(command, (String) null);
    }

    public MemResult executeCommand(String[] command, String in) {
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        ByteArrayPrintStream oout = new ByteArrayPrintStream();
        ByteArrayPrintStream oerr = new ByteArrayPrintStream();
        JShellContext newContext = createNewContext(getRootContext(), command[0], Arrays.copyOfRange(command, 1, command.length));
        newContext.setIn(new ByteArrayInputStream(in == null ? new byte[0] : in.getBytes()));
        newContext.setOut(oout);
        newContext.setErr(oerr);
        int r = executeCommand(command, newContext);
        out.append(oout);
        err.append(oerr);
        return new MemResult(out.toString(), err.toString(), r);
    }

    public JShellContext createContext(JShellContext ctx, JShellNode root, JShellNode parent, JShellVariables env, String serviceName, String[] args) {
        return new DefaultJShellContext(this, root, parent, ctx, appContext.getSession().getWorkspace(), appContext.getSession(), env, serviceName, args);
    }

}
