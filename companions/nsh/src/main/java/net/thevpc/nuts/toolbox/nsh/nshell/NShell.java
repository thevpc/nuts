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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.toolbox.nsh.nshell;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineHistory;
import net.thevpc.nuts.elem.NEDesc;

import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.spi.NDefaultSupportLevelContext;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.toolbox.nsh.autocomplete.NshAutoCompleter;
import net.thevpc.nuts.toolbox.nsh.cmdresolver.DefaultNShellCommandTypeResolver;
import net.thevpc.nuts.toolbox.nsh.cmdresolver.NCommandTypeResolver;
import net.thevpc.nuts.toolbox.nsh.cmdresolver.NShellCommandTypeResolver;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltin;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinCore;
import net.thevpc.nuts.toolbox.nsh.cmds.NShellBuiltinDefault;
import net.thevpc.nuts.toolbox.nsh.err.*;
import net.thevpc.nuts.toolbox.nsh.eval.*;
import net.thevpc.nuts.toolbox.nsh.history.DefaultNShellHistory;
import net.thevpc.nuts.toolbox.nsh.history.NShellHistory;
import net.thevpc.nuts.toolbox.nsh.nodes.*;
import net.thevpc.nuts.toolbox.nsh.options.DefaultNShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.options.NShellOptionsParser;
import net.thevpc.nuts.toolbox.nsh.parser.NShellParser;
import net.thevpc.nuts.toolbox.nsh.sys.NExternalExecutor;
import net.thevpc.nuts.toolbox.nsh.sys.NShellExternalExecutor;
import net.thevpc.nuts.toolbox.nsh.sys.NShellNoExternalExecutor;
import net.thevpc.nuts.toolbox.nsh.util.ByteArrayPrintStream;
import net.thevpc.nuts.toolbox.nsh.util.MemResult;
import net.thevpc.nuts.time.NClock;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Year;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class NShell {

    public static final String ENV_PATH = "PATH";
    public static final String ENV_HOME = "HOME";
    private static final Logger LOG = Logger.getLogger(NShell.class.getName());
    private final NShellOptions options;
    private final NShellHistory history;
    private final List<NShellVarListener> listeners = new ArrayList<>();
    protected NShellContext rootContext;
    private NClock bootStartMillis;
    private NShellEvaluator evaluator;
    private NShellErrorHandler errorHandler;
    private NShellExternalExecutor externalExecutor;
    private NShellCommandTypeResolver commandTypeResolver;
    private NSession session;
    private NId appId = null;
    private String serviceName = null;
    private Supplier<NMsg> headerMessageSupplier = null;

    public NShell() {
        this(new NShellConfiguration());
    }

    public NShell(NShellConfiguration configuration) {
        if (configuration == null) {
            configuration = new NShellConfiguration();
        }
        headerMessageSupplier = configuration.getHeaderMessageSupplier();
        serviceName = configuration.getServiceName();
        String[] args = configuration.getArgs();
        NShellOptionsParser shellOptionsParser = configuration.getShellOptionsParser();
        NShellEvaluator evaluator = configuration.getEvaluator();
        NShellCommandTypeResolver commandTypeResolver = configuration.getCommandTypeResolver();
        NShellErrorHandler errorHandler = configuration.getErrorHandler();
        NShellExternalExecutor externalExecutor = configuration.getExternalExecutor();
        NId appId = configuration.getAppId();
        NSession session = NSession.of();

        args = resolveArgs(session, args);
        this.appId = appId;
        this.bootStartMillis = NApp.of().getStartTime();
        this.session = session;
        //super.setCwd(workspace.getConfigManager().getCwd());
        if (this.appId == null) {
            this.appId = NApp.of().getId().orNull();
            if (this.appId == null) {
                this.appId = NId.getForClass(NShell.class).orNull();
            }
        }
        if (this.appId == null && session != null) {
            throw new IllegalArgumentException("unable to resolve application id");
        }
        if (this.appId != null && serviceName == null) {
            serviceName = this.appId.getArtifactId();
        }

        serviceName = resolveServiceName(session, serviceName, appId);
        if (commandTypeResolver == null) {
            if (session != null) {
                this.commandTypeResolver = new NCommandTypeResolver();
            } else {
                this.commandTypeResolver = new DefaultNShellCommandTypeResolver();
            }
        } else {
            this.commandTypeResolver = commandTypeResolver;
        }
        if (errorHandler == null) {
            this.errorHandler = new DefaultErrorHandler();
        } else {
            this.errorHandler = errorHandler;
        }
        if (evaluator == null) {
            if (session != null) {
                this.evaluator = new NshEvaluator();
            } else {
                this.evaluator = new DefaultNShellEvaluator();
            }
        } else {
            this.evaluator = evaluator;
        }
        NShellHistory history = configuration.getHistory();
        if (history == null) {
            this.history = new DefaultNShellHistory();
        } else {
            this.history = history;
        }
        if (shellOptionsParser == null) {
            shellOptionsParser = new DefaultNShellOptionsParser();
        }
        this.options = shellOptionsParser.parse(args);
        if (externalExecutor == null) {
            boolean includeExternalExecutor = configuration.getIncludeExternalExecutor() != null && configuration.getIncludeExternalExecutor();
            if (includeExternalExecutor) {
                if (session != null) {
                    this.externalExecutor = new NExternalExecutor();
                } else {
                    this.externalExecutor = new NShellNoExternalExecutor();
                }
            }
        } else {
            this.externalExecutor = externalExecutor;
        }
        if (options.getServiceName() == null) {
            options.setServiceName(serviceName == null ? "shell" : serviceName);
        }

        if (session != null) {
            NShellContext _rootContext = getRootContext();

            NWorkspace.of().setProperty(NShellContext.class.getName(), _rootContext);
            _rootContext.setSession(session);
            //add default commands
            List<NShellBuiltin> allCommand = new ArrayList<>();
            NSupportLevelContext constraints = new NDefaultSupportLevelContext(this);

            Predicate<NShellBuiltin> filter = new NShellBuiltinPredicate(configuration);
            for (NShellBuiltin command : NWorkspace.get().get().extensions()
                            .createServiceLoader(NShellBuiltin.class, NShell.class, NShellBuiltin.class.getClassLoader())
                    .loadAll(this)) {
                NShellBuiltin old = _rootContext.builtins().find(command.getName());
                if (old != null && old.getSupportLevel(constraints) >= command.getSupportLevel(constraints)) {
                    continue;
                }
                if (filter.test(command)) {
                    allCommand.add(command);
                }
            }
            _rootContext.builtins().set(allCommand.toArray(new NShellBuiltin[0]));
            _rootContext.getUserProperties().put(NShellContext.class.getName(), _rootContext);

            try {
                NPath histFile = this.history.getHistoryFile();
                if (histFile == null) {
                    histFile = NWorkspace.of().getStoreLocation(this.appId, NStoreType.VAR).resolve((serviceName == null ? "" : serviceName) + ".history");
                    this.history.setHistoryFile(histFile);
                    if (histFile.exists()) {
                        this.history.load(histFile);
                    }
                }
            } catch (Exception ex) {
                NLog.of(NShell.class)
                        .with().level(Level.SEVERE)
                        .error(ex)
                        .log(NMsg.ofC("error resolving history file %s", this.history.getHistoryFile()));
            }
            NWorkspace.of().setProperty(NShellHistory.class.getName(), this.history);
        }
    }

    private static String[] resolveArgs(NSession session, String[] args) {
        if (args != null) {
            return args;
        }
        return NApp.of().getArguments().toArray(new String[0]);
    }

    private static String resolveServiceName(NSession session, String serviceName, NId appId) {
        if ((serviceName == null || serviceName.trim().isEmpty())) {
            if (appId == null) {
                appId = NId.getForClass(NShell.class).get();
            }
            serviceName = appId.getArtifactId();
        }
        return serviceName;
    }

    public void addVarListener(NShellVarListener listener) {
        this.listeners.add(listener);
    }

    public void removeVarListener(NShellVarListener listener) {
        this.listeners.add(listener);
    }

    public NShellVarListener[] getVarListeners() {
        return listeners.toArray(new NShellVarListener[0]);
    }

    public NShellEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(NShellEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public NShellCommandTypeResolver getCommandTypeResolver() {
        return commandTypeResolver;
    }

    public void setCommandTypeResolver(NShellCommandTypeResolver whichResolver) {
        this.commandTypeResolver = whichResolver;
    }

    public NShellExternalExecutor getExternalExecutor() {
        return externalExecutor;
    }

    public void setExternalExecutor(NShellExternalExecutor externalExecutor) {
        this.externalExecutor = externalExecutor;
    }

    public NShellErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public void setErrorHandler(NShellErrorHandler errorHandler) {
        this.errorHandler = errorHandler;
    }

    public List<String> findFiles(final String namePattern, boolean exact, String parent, NSession session) {
        if (exact) {
            String[] all = NPath.of(parent).stream()
                    .filter(NPredicate.of((NPath x) -> namePattern.equals(x.getName())).withDesc(NEDesc.of("name='" + namePattern + "'")))
                    .map(NFunction.of(NPath::toString).withDesc(NEDesc.of("toString"))).toArray(String[]::new);
            return Arrays.asList(all);
        } else {
            final Pattern o = Pattern.compile(namePattern);
            String[] all = NPath.of(parent).stream()
                    .filter(NPredicate.of((NPath x) -> o.matcher(x.getName()).matches()).withDesc(NEDesc.of("name~~'" + namePattern + "'")))
                    .map(NFunction.of(NPath::toString).withDesc(NEDesc.of("toString"))).toArray(String[]::new);
            return Arrays.asList(all);
        }
    }

    //    protected NShellContext createRootContext() {
//        return new DefaultNShellContext(this);
//    }
    protected NShellContext createRootContext(String serviceName, String[] args) {
        return createContext(null, null, null, null, serviceName, args);
    }

    public NShellContext createNewContext(NShellContext parentContext) {
        return createNewContext(parentContext, parentContext.getServiceName(), parentContext.getArgsArray());
    }

    public NShellContext createNewContext(NShellContext ctx, String serviceName, String[] args) {
        return createContext(ctx, null, null, null, serviceName, args);
    }

    public NShellContext createInlineContext(NShellContext ctx, String serviceName, String[] args) {
        if (ctx == null) {
            ctx = getRootContext();
        }
        NShellContextForSource c = new NShellContextForSource(ctx);
        c.setServiceName(serviceName);
        c.setArgs(args);
        return c;
    }

    public NShellCommandNode createCommandNode(String[] args) {
        return NShellParser.createCommandNode(args);
    }

    public NShellContext getRootContext() {
        if (rootContext == null) {
            rootContext = createRootContext(options.getServiceName(), options.getCommandArgs().toArray(new String[0]));
        }
        return rootContext;
    }

    public void executeLine(String line, boolean storeResult, NShellContext context) {
        if (context == null) {
            context = getRootContext();
        }
        boolean success = false;
        if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
            try {
                getHistory().add(line);
                NShellCommandNode nn = parseScript(line);
                int i = context.getShell().evalNode(nn, context);
                success = i == 0;
            } catch (NShellQuitException e) {
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

    public int onResult(int r, NShellContext context) {
        context.setLastResult(new NShellResult(r, null, null));
        return r;
    }

    public int onResult(Throwable th, NShellContext context) {
        if (th == null) {
            context.setLastResult(new NShellResult(0, null, null));
            return 0;
        }
        if (th instanceof NShellQuitException) {
            throw (NShellQuitException) th;
        }
        if (getErrorHandler().isQuitException(th)) {
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            throw new NShellQuitException(th, 100);
        }

        if (th instanceof NShellException) {
            NShellException je = (NShellException) th;
            int errorCode = je.getExitCode();
            String lastErrorMessage = getErrorHandler().errorToMessage(th);
            context.setLastResult(new NShellResult(errorCode, lastErrorMessage, th));
            if (errorCode != NExecutionException.SUCCESS) {
                getErrorHandler().onError(lastErrorMessage, th, context);
            }
            return errorCode;
        }

        int errorCode = getErrorHandler().errorToCode(th);
        String lastErrorMessage = getErrorHandler().errorToMessage(th);
        context.setLastResult(new NShellResult(errorCode, lastErrorMessage, th));
        if (errorCode != 0) {
            getErrorHandler().onError(lastErrorMessage, th, context);
        }
        return errorCode;
    }

    public int onResult(int errorCode, Throwable th, NShellContext context) {
        if (errorCode != 0) {
            if (th == null) {
                th = new RuntimeException("error occurred. Error Code #" + errorCode);
            }
        } else {
            th = null;
        }
        String lastErrorMessage = th == null ? null : getErrorHandler().errorToMessage(th);
        context.setLastResult(new NShellResult(errorCode, lastErrorMessage, th));
        if (errorCode != 0) {
            getErrorHandler().onError(lastErrorMessage, th, context);
        }
        return errorCode;
    }

    public int executeCommand(String[] command, NShellContext context) {
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
                                      NShellContext context
    ) {
        context.getShell().traceExecution(() -> String.join(" ", command), context);
        String cmdToken = command[0];
        NPath cmdPath = NPath.of(cmdToken);
        if (!cmdPath.isName()) {
            if (isShellFile(cmdPath, session)) {
                return executeServiceFile(createNewContext(context, cmdPath.toString(), command), false);
            } else {
                final NShellExternalExecutor externalExec = getExternalExecutor();
                if (externalExec == null) {
                    throw new NShellException(NMsg.ofC("not found %s", cmdToken), 101);
                }
                return externalExec.execExternalCommand(command, context);
            }
        } else {
            List<String> cmds = new ArrayList<>(Arrays.asList(command));
            String a = considerAliases ? context.aliases().get(cmdToken) : null;
            if (a != null) {
                NShellNode node0 = null;
                try {
//                    NShellParser parser = new NShellParser();
//                    node0 = parser.parse(a);

                    node0 = NShellParser.fromString(a).parse();

                } catch (Exception ex) {
                    Logger.getLogger(NShell.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (node0 instanceof NShellCmdLineNode) {
                    NShellCmdLineNode nn = (NShellCmdLineNode) node0;
                    List<String> newCmd = new ArrayList<>();
                    for (NShellArgumentNode item : nn) {
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
            NShellBuiltin shellCommand = considerBuiltins ? context.builtins().find(a) : null;
            if (shellCommand != null && shellCommand.isEnabled()) {
                ArrayList<String> arg2 = new ArrayList<String>(cmds);
                arg2.remove(0);
                shellCommand.exec(arg2.toArray(new String[0]), context.createCommandContext(shellCommand));
            } else {
                if (considerExternal) {
                    final NShellExternalExecutor externalExec = getExternalExecutor();
                    if (externalExec == null) {
                        throw new NShellException(NMsg.ofC("not found %s", cmdToken), 101);
                    }
                    externalExec.execExternalCommand(cmds.toArray(new String[0]), context);
                } else {
                    throw new NShellException(NMsg.ofC("not found %s", cmdToken), 101);
                }
            }
        }
        return 0;
    }

    private boolean isShellFile(NPath cmdPath, NSession session) {
        if(cmdPath.getName().endsWith(".nsh")){
            return true;
        }
        if(cmdPath.getName().endsWith(".sh")){
            return true;
        }
        if(cmdPath.exists()){
            String firstLine = cmdPath.getLines().findFirst().orElse(null);
            if(firstLine!=null){
                if(firstLine.startsWith("#!/bin/sh")){
                    return true;
                }
                if(firstLine.startsWith("#!/bin/bash")){
                    return true;
                }
                if(firstLine.startsWith("#!/bin/nsh")){
                    return true;
                }
            }
        }
        return false;
    }

    public void run() {
        try {
            if (NApp.of().getAutoComplete() != null) {
                return;
            }
            NShellContext rootContext = getRootContext();
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
        } catch (NExecutionException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new NExecutionException(NMsg.ofC("%s", ex), ex, NExecutionException.ERROR_1);
        }
    }

    protected String readInteractiveLine(NShellContext context) {
        NTerminal terminal = context.getSession().getTerminal();
        return terminal.readLine(getPromptString(context));
    }

    protected void printHeader(NPrintStream out) {
        NMsg m = null;
        if (headerMessageSupplier != null) {
            m = headerMessageSupplier.get();
            if (m == null) {
                return;
            }
        }
        if (m == null) {
            NDescriptor resultDescriptor = null;
            if (appId != null) {
                try {
                    resultDescriptor = NFetchCmd.of(appId).setEffective(true).getResultDescriptor();
                } catch (Exception ex) {
                    //just ignore
                }
            }
            NDescriptorContributor contributor = null;
            if (resultDescriptor != null) {
                for (NDescriptorContributor c : resultDescriptor.getDevelopers()) {
                    contributor = c;
                    break;
                }
            }
            String copyRight = null;
            if (resultDescriptor != null && resultDescriptor.getLicenses() != null) {
                for (NDescriptorLicense license : resultDescriptor.getLicenses()) {
                    if (!NBlankable.isBlank(license.getDate())) {
                        copyRight = license.getDate();
                        break;
                    }
                }
            }
            if (resultDescriptor != null && resultDescriptor.getLicenses() != null) {
                for (NDescriptorLicense license : resultDescriptor.getLicenses()) {
                    if (!NBlankable.isBlank(license.getName())) {
                        copyRight = license.getName();
                        break;
                    }
                }
            }
            if (NBlankable.isBlank(copyRight)) {
                copyRight = String.valueOf(Year.now().getValue());
            }
            m = NMsg.ofC("%s v%s (c) %s",
                    NMsg.ofStyledPrimary1(NStringUtils.firstNonNull(serviceName, "app")),
                    (appId == null || appId.getVersion().isBlank()) ?
                            getRootContext().getWorkspace().getRuntimeId().getVersion() :
                            appId.getVersion()
                    , contributor == null ? "thevpc" : NStringUtils.firstNonBlank(
                            contributor.getName(),
                            contributor.getEmail(),
                            contributor.getId()
                    ),
                    copyRight
            );
        }
        out.resetLine().println(m);
    }

    protected void executeHelp(NShellContext context) {
        context.out().println("Syntax : shell [<FILE>]\n");
        context.out().println("    <FILE> : if present content will be processed as input\n");
    }

    protected void executeVersion(NShellContext context) {
        context.out().println(NMsg.ofC("v%s", NApp.of().getId().get().getVersion()));
    }

    protected void executeInteractive(NShellContext context) {
        NSystemTerminal.enableRichTerm();
        NPath appVarFolder = NApp.of().getVarFolder();
        if (appVarFolder == null) {
            appVarFolder = NWorkspace.of().getStoreLocation(
                    NId.get("net.thevpc.app.nuts.toolbox:nsh").get()
                    , NStoreType.VAR);
        }
        NIO.of().getSystemTerminal()
                .setCommandAutoCompleteResolver(new NshAutoCompleter(context.getWorkspace()))
                .setCommandHistory(
                        NCmdLineHistory.of()
                                .setPath(appVarFolder.resolve("nsh-history.hist"))
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
                } catch (NShellQuitException q) {
                    if (getOptions().isLogin()) {
                        executeLogoutScripts();
                    }
                    if (q.getExitCode() == NExecutionException.SUCCESS) {
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
        onQuit(new NShellQuitException(0));
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

    protected void onQuit(NShellQuitException quitException) {
        try {
            getHistory().save();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        if (quitException.getExitCode() == 0) {
            return;
        }
        throw new NExecutionException(NMsg.ofC("%s", quitException), quitException.getExitCode());
//        throw quitException;
    }

    public int executeServiceFile(NShellContext context, boolean ignoreIfNotFound) {
        String file = context.getServiceName();
        if (file != null) {
            file = NPath.of(file).toAbsolute(context.getDirectory()).toString();
        }
        if (file == null || !NPath.of(file).exists()) {
            if (ignoreIfNotFound) {
                return 0;
            }
            throw new NShellException(NMsg.ofC("shell file not found : %s", file), 1);
        }
        context.setServiceName(file);
        InputStream stream = null;
        try {
            stream = NPath.of(file).getInputStream();
            NShellCommandNode ii = parseScript(stream);
            if (ii == null) {
                return 0;
            }
            NShellContext c = context.setRootNode(ii);//.setParent(null);
            return context.getShell().evalNode(ii, c);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                throw new NShellException(ex, 1);
            }
        }
    }

    public int executeScript(String text, NShellContext context) {
        if (context == null) {
            context = getRootContext();
        }
        if (text == null || text.trim().isEmpty()) {
            return 0;
        }
        NShellCommandNode ii = parseScript(text);
        if (ii == null) {
            return 0;
        }
        NShellContext c = context.setRootNode(ii);//.setParent(null);
        return evalNode(ii, c);
    }

    public int evalNode(NShellCommandNode node, NShellContext context) {
        try {
            int r = node.eval(context);
            onResult(r, context);
            return r;
        } catch (NShellUniformException th) {
            if (th.isQuit()) {
                onResult(null, context);
                th.throwQuit();
                return 0;
            } else {
                onResult(th, context);
                throw th;
            }
        } catch (NShellQuitException th) {
            throw th;
        } catch (Exception th) {
            if (getErrorHandler().isQuitException(th)) {
                onResult(null, context);
                throw new NShellUniformException(getErrorHandler().errorToCode(th), true, th);
            }
            onResult(th, context);
            context.err().println(NMsg.ofC("error: %s", th));
            return getErrorHandler().errorToCode(th);
        }
    }

    public int safeEval(NShellCommandNode n, NShellContext context) {
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
    protected NMsg getPromptString(NShellContext context) {
        NSession session = context.getSession();
//        String wss = ws == null ? "" : new File(getRootContext().getAbsolutePath(ws.config().getWorkspaceLocation().toString())).getName();
        String login = null;
        if (session != null) {
            login = NWorkspaceSecurityManager.of().getCurrentUsername();
        }
        String prompt = ((login != null && login.length() > 0 && !"anonymous".equals(login)) ? (login + "@") : "");
        if (!NBlankable.isBlank(getRootContext().getServiceName())) {
            prompt = prompt + getRootContext().getServiceName();
        }
        prompt += "> ";
        return NMsg.ofPlain(prompt);
    }

    protected String getPromptString0(NShellContext context) {

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
                        s.append(context.getDirectory());
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

    //    public String evalAsString(String param, NShellContext context) {
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
//    public String[] findExecFilesInPath(String filePath, String[] classNames, NShellContext context) {
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
//    public String[] findClassesInPath(String filePath, String[] classNames, NShellContext context) {
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
    public void prepareContext(NShellContext context) {
//        try {
//            cwd = new File(".").getCanonicalPath();
//        } catch (IOException ex) {
//            cwd = new File(".").getAbsolutePath();
//        }
        context.vars().set(System.getenv());
        setUndefinedStartupEnv("USER", System.getProperty("user.name"), context);
        setUndefinedStartupEnv("LOGNAME", System.getProperty("user.name"), context);
        setUndefinedStartupEnv(NShell.ENV_PATH, ".", context);
        setUndefinedStartupEnv("PWD", System.getProperty("user.dir"), context);
        setUndefinedStartupEnv(NShell.ENV_HOME, System.getProperty("user.home"), context);
        setUndefinedStartupEnv("PS1", ">", context);
        setUndefinedStartupEnv("IFS", " \t\n", context);
    }

    private void setUndefinedStartupEnv(String name, String defaultValue, NShellContext context) {
        if (context.vars().get(name) == null) {
            context.vars().set(name, defaultValue);
        }
    }

    public NShellScript parseScript(InputStream stream) {
        NShellNode node0 = null;
        try {
            node0 = NShellParser.fromInputStream(stream).parse();
            if (node0 == null) {
                return null;
            }
        } catch (Exception ex) {
            Logger.getLogger(NShell.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node0 instanceof NShellCommandNode) {
            return new NShellScript((NShellCommandNode) node0);
        }
        throw new IllegalArgumentException("expected node " + node0);
    }

    public NShellScript parseScript(String scriptString) {
        NShellNode node0 = null;
        try {
            node0 = NShellParser.fromString(scriptString).parse();
            if (node0 == null) {
                return null;
            }
        } catch (Exception ex) {
            Logger.getLogger(NShell.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node0 instanceof NShellCommandNode) {
            return new NShellScript((NShellCommandNode) node0);
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

    public void traceExecution(Supplier<String> msg, NShellContext context) {
        if (getOptions().isXtrace()) {
            String txt = msg.get();
            context.err().println("+ " + txt);
        }
    }

    public NShellOptions getOptions() {
        return options;
    }

    public NShellHistory getHistory() {
        return history;
    }

    public String getVersion() {
        NId nutsId = NId.getForClass(getClass()).orNull();
        if (nutsId == null) {
            return "dev";
        }
        return nutsId.getVersion().getValue();
    }

    public NSession getSession() {
        return session;
    }

    public MemResult executeCommand(String[] command) {
        return executeCommand(command, (String) null);
    }

    public MemResult executeCommand(String[] command, String in) {
        StringBuilder out = new StringBuilder();
        StringBuilder err = new StringBuilder();
        ByteArrayPrintStream oout = new ByteArrayPrintStream();
        ByteArrayPrintStream oerr = new ByteArrayPrintStream();
        NShellContext newContext = createNewContext(getRootContext(), command[0], Arrays.copyOfRange(command, 1, command.length));
        newContext.setIn(new ByteArrayInputStream(in == null ? new byte[0] : in.getBytes()));
        newContext.setOut(oout);
        newContext.setErr(oerr);
        int r = executeCommand(command, newContext);
        out.append(oout);
        err.append(oerr);
        return new MemResult(out.toString(), err.toString(), r);
    }

    public NShellContext createContext(NShellContext ctx, NShellNode root, NShellNode parent, NShellVariables env, String serviceName, String[] args) {
        return new DefaultNShellContext(this, root, parent, ctx, session.getWorkspace(), session, env, serviceName, args);
    }

    private static class NShellBuiltinPredicate implements Predicate<NShellBuiltin> {
        private final NShellConfiguration configuration;

        boolean includeCoreBuiltins;
        boolean includeDefaultBuiltins;

        public NShellBuiltinPredicate(NShellConfiguration configuration) {
            this.configuration = configuration;
            includeCoreBuiltins = configuration.getIncludeCoreBuiltins() == null || configuration.getIncludeCoreBuiltins();
            includeDefaultBuiltins = configuration.getIncludeDefaultBuiltins() != null && configuration.getIncludeDefaultBuiltins();
        }

        @Override
        public boolean test(NShellBuiltin nShellBuiltin) {
            if (!includeCoreBuiltins) {
                if (nShellBuiltin instanceof NShellBuiltinCore) {
                    return false;
                }
            }
            if (!includeDefaultBuiltins) {
                if (nShellBuiltin instanceof NShellBuiltinDefault) {
                    return false;
                }
            }
            Predicate<NShellBuiltin> filter = configuration.getBuiltinFilter();
            if (filter != null) {
                if (!filter.test(nShellBuiltin)) {
                    return false;
                }
            }
            return true;
        }
    }
}
