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
package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import net.thevpc.nuts.toolbox.nsh.bundles.jshell.parser.JShellParser;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.ByteArrayPrintStream;
import net.thevpc.nuts.toolbox.nsh.bundles.jshell.util.ShellUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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

    public boolean fallBackToMain = false;

    public Object shellInterpreter = null;
    protected JShellFileContext rootContext;
    private String version = "1.0.0";
    private JShellOptions options;
    private JShellEvaluator evaluator;
    private JShellHistory history;
    private JShellErrorHandler errorHandler;
    private JShellExternalExecutor externalExecutor;
    private JShellCommandTypeResolver commandTypeResolver;
    private BufferedReader _in_reader = null;
    private List<JShellVarListener> listeners = new ArrayList<>();

    public void addVarListener(JShellVarListener listener) {
        this.listeners.add(listener);
    }

    public void removeVarListener(JShellVarListener listener) {
        this.listeners.add(listener);
    }

    public JShellVarListener[] getVarListeners() {
        return listeners.toArray(new JShellVarListener[0]);
    }

    public JShell(){
        this(null,null,null,null,null,null,null,null);
    }

    public JShell(String serviceName, String[] args,JShellOptionsParser shellOptionsParser,
                  JShellEvaluator evaluator,JShellCommandTypeResolver commandTypeResolver,
                  JShellErrorHandler errorHandler,
                  JShellExternalExecutor externalExecutor,
                  JShellHistory history
    ) {
        if(commandTypeResolver==null) {
            this.commandTypeResolver = new DefaultJShellCommandTypeResolver();
        }else{
            this.commandTypeResolver=commandTypeResolver;
        }
        if(errorHandler==null) {
            this.errorHandler = new DefaultJShellErrorHandler();
        }else{
            this.errorHandler=errorHandler;
        }
        if(evaluator==null) {
            this.evaluator = new DefaultJShellEvaluator();
        }else{
            this.evaluator=evaluator;
        }
        if(history==null) {
            this.history = new DefaultJShellHistory();
        }else{
            this.history=history;
        }
        if(shellOptionsParser==null){
            shellOptionsParser=new DefaultJShellOptionsParser();
        }
        options=shellOptionsParser.parse(args);
        this.externalExecutor=externalExecutor;
        if(options.getServiceName()==null){
            options.setServiceName(serviceName==null?"jshell":serviceName);
        }
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

    public List<String> findFiles(final String namePattern, boolean exact, String parent) {
        if (exact) {
            String[] all = new File(parent).list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return namePattern.equals(name);
                }
            });
            if (all == null) {
                all = new String[0];
            }
            return Arrays.asList(all);
        } else {
            final Pattern o = Pattern.compile(namePattern);
            String[] all = new File(parent).list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return o.matcher(name).matches();
                }
            });
            if (all == null) {
                all = new String[0];
            }
            return Arrays.asList(all);
        }
    }

    public void setServiceName(String serviceName) {
        getRootContext().setServiceName(serviceName);
    }


    protected JShellContext createRootContext() {
        return new DefaultJShellContext(this);
    }

    public JShellFileContext createFileContext(JShellContext rootContext, String serviceName, String[] args) {
        return new DefaultJShellFileContext(
                rootContext,
                serviceName, args
        );
    }

    public JShellFileContext createSourceFileContext(JShellFileContext parentContext, String serviceName, String[] args) {
        return createFileContext(
                parentContext.getShellContext(),
                serviceName, args
        );
    }

    public JShellFileContext createNewContext(JShellFileContext parentContext, String serviceName, String[] args) {
        return createFileContext(
                createContext(parentContext.getShellContext()),
                serviceName, args
        );
    }

    public JShellFileContext createNewContext(JShellFileContext parentContext) {
        return createFileContext(
                createContext(parentContext.getShellContext()),
                parentContext.getServiceName(), parentContext.getArgsArray()
        );
    }

    public JShellContext createContext(JShellContext parentContext) {
        return new DefaultJShellContext(parentContext);
    }

    public JShellCommandNode createCommandNode(String[] args) {
        return JShellParser.createCommandNode(args);
    }


    public JShellFileContext getRootContext() {
        if(rootContext==null){
            rootContext = createFileContext(createRootContext(), options.getServiceName(), options.getCommandArgs().toArray(new String[0]));
        }
        return rootContext;
    }

    public void executeLine(String line, boolean storeResult, JShellFileContext context) {
        if (context == null) {
            context = getRootContext();
        }
        boolean success = false;
        if (line.trim().length() > 0 && !line.trim().startsWith("#")) {
            try {
                getHistory().add(line);
                JShellCommandNode nn = parseCommandLine(line);
                nn.eval(context);
                success = true;
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

    public int onResult(Throwable th, JShellFileContext context) {
        if (th == null) {
            context.setLastResult(new JShellResult(0, null, null));
            return 0;
        }
        if (th instanceof JShellQuitException) {
            throw (JShellQuitException) th;
        }
        if (getErrorHandler().isRequireExit(th)) {
            if (th instanceof RuntimeException) {
                throw (RuntimeException) th;
            }
            throw new JShellQuitException(100, th);
        }

        if (th instanceof JShellException) {
            JShellException je = (JShellException) th;
            int errorCode = je.getResult();
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

    public int onResult(int errorCode, Throwable th, JShellFileContext context) {
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

    public int executeCommand(String[] command, JShellFileContext context) {
        context.setServiceName(command[0]);
        context.setArgs(Arrays.copyOfRange(command, 1, command.length));
        return createCommandNode(command).eval(context);
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
                                       JShellFileContext context
    ) {
        String cmdToken = command[0];
        if (cmdToken.indexOf('/') >= 0 || cmdToken.indexOf('\\') >= 0) {
            final JShellExternalExecutor externalExec = getExternalExecutor();
            if (externalExec == null) {
                throw new JShellException(101, "not found " + cmdToken);
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
                        throw new JShellException(101, "not found " + cmdToken);
                    }
                    externalExec.execExternalCommand(cmds.toArray(new String[0]), context);
                } else {
                    throw new JShellException(101, "not found " + cmdToken);
                }
            }
        }
        return 0;
    }


    public void run() {
        if (getOptions().isHelp()) {
            executeHelp(getRootContext());
            return;
        }
        if (getOptions().isVersion()) {
            executeVersion(getRootContext());
            return;
        }
        if (getOptions().isStdInAndPos()) {
            if (getOptions().getCommandArgs().isEmpty()) {
                //ok
                executeInteractive(getRootContext());
            } else {
                getRootContext().err().println("-s option not supported yet. ignored");
                executeInteractive(getRootContext());
            }
            if (getOptions().isInteractive()) {
                executeInteractive(getRootContext());
            }
            return;
        }

        if (getOptions().isCommand()) {
            executeCommand(getOptions().getCommandArgs().toArray(new String[0]),getRootContext());
            if (getOptions().isInteractive()) {
                executeInteractive(getRootContext());
            }
            return;
        }

        if (!getOptions().getFiles().isEmpty()) {
            for (String file : getOptions().getFiles()) {
                executeFile(createSourceFileContext(getRootContext(), file, getOptions().getCommandArgs().toArray(new String[0])), false);
            }
            if (getOptions().isInteractive()) {
                executeInteractive(getRootContext());
            }
            return;
        }
        executeInteractive(getRootContext());
    }

    protected String readInteractiveLine(JShellFileContext context) {
        if (_in_reader == null) {
            _in_reader = new BufferedReader(new InputStreamReader(System.in));
        }
        try {
            return _in_reader.readLine();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    protected void printHeader(PrintStream out) {
        //
    }

    protected void executeHelp(JShellFileContext context) {
        context.out().println("Syntax : shell [<FILE>]\n");
        context.out().println("    <FILE> : if present content will be processed as input\n");
    }

    protected void executeVersion(JShellFileContext context) {
        context.out().printf("v%s\n", APP_VERSION);
    }

    protected void executeInteractive(JShellFileContext context) {
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
                    if (q.getResult() == 0) {
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
        onQuit(new JShellQuitException(1, null));
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
                    executeFile(createSourceFileContext(getRootContext(), profileFile, new String[0]), true);
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
                    executeFile(createSourceFileContext(getRootContext(), profileFile, new String[0]), true);
                }
            }
        }
    }

    protected void onQuit(JShellQuitException quitExcepion) {
        try {
            getHistory().save();
        } catch (IOException e) {
            //e.printStackTrace();
        }
        throw quitExcepion;
    }

    public void executeFile(JShellFileContext context, boolean ignoreIfNotFound) {
        String file = context.getServiceName();
        if (file != null) {
            file = ShellUtils.getAbsolutePath(new File(context.getCwd()), file);
        }
        if (file == null || !new File(file).isFile()) {
            if (ignoreIfNotFound) {
                return;
            }
            throw new JShellException(1, "shell file not found : " + file);
        }
        context.setServiceName(file);
        FileInputStream stream = null;
        try {
            try {
                stream = new FileInputStream(file);
                JShellCommandNode ii = parseCommand(stream);
                if (ii == null) {
                    return;
                }
                JShellFileContext c = context.setRoot(ii);//.setParent(null);
                ii.eval(c);
            } catch (IOException ex) {
                throw new JShellException(1, ex);
            }
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                throw new JShellException(1, ex);
            }
        }
    }

    public void executeString(String text, JShellFileContext context) {
        if (context == null) {
            context = getRootContext();
        }
        if (text == null || text.trim().isEmpty()) {
            return;
        }
        try (InputStream stream = new ByteArrayInputStream(text.getBytes())) {
            JShellCommandNode ii = parseCommand(stream);
            if (ii == null) {
                return;
            }
            JShellFileContext c = context.setRoot(ii);//.setParent(null);
            ii.eval(c);
        } catch (IOException ex) {
            throw new JShellException(1, ex);
        }
    }

    public int uniformException(UnsafeRunnable r) throws JShellUniformException {
        try {
            return r.run();
        } catch (JShellUniformException th) {
            throw th;
        } catch (Exception th) {
            if (getErrorHandler().isRequireExit(th)) {
                throw new JShellUniformException(getErrorHandler().errorToCode(th), true, th);
            }
            throw new JShellUniformException(getErrorHandler().errorToCode(th), true, th);
        }
    }

    public int safeEval(JShellCommandNode n, JShellFileContext context) {
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
    protected String getPromptString(JShellFileContext context) {

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

    public void prepareContext(JShellFileContext context) {
//        try {
//            cwd = new File(".").getCanonicalPath();
//        } catch (IOException ex) {
//            cwd = new File(".").getAbsolutePath();
//        }
        context.vars().set(System.getenv());
        setUndefinedStartupEnv("USER", System.getProperty("user.name"),context);
        setUndefinedStartupEnv("LOGNAME", System.getProperty("user.name"),context);
        setUndefinedStartupEnv(JShell.ENV_PATH, ".",context);
        setUndefinedStartupEnv("PWD", System.getProperty("user.dir"),context);
        setUndefinedStartupEnv(JShell.ENV_HOME, System.getProperty("user.home"),context);
        setUndefinedStartupEnv("PS1", ">",context);
        setUndefinedStartupEnv("IFS", " \t\n",context);
    }

    private void setUndefinedStartupEnv(String name, String defaultValue,JShellFileContext context) {
        if (context.vars().get(name) == null) {
            context.vars().set(name, defaultValue);
        }
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

    public JShellCommandNode parseCommand(InputStream stream) {
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
            return (JShellCommandNode) node0;
        }
        throw new IllegalArgumentException("expected node " + node0);
    }

    public JShellCommandNode parseCommandLine(String line) {
        JShellNode node0 = null;
        try {
            node0 = JShellParser.fromString(line).parse();
            if (node0 == null) {
                return null;
            }
        } catch (Exception ex) {
            Logger.getLogger(JShell.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (node0 instanceof JShellCommandNode) {
            return (JShellCommandNode) node0;
        }
        throw new IllegalArgumentException("expected node " + line);
    }

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

    public void traceExecution(String msg, JShellFileContext context) {
        if (getOptions().isXtrace()) {
            context.out().println("+ " + msg);
        }
    }

    public JShellOptions getOptions() {
        return options;
    }

    public JShellHistory getHistory() {
        return history;
    }

    public String getVersion() {
        return version;
    }

    protected void setVersion(String version) {
        this.version = version;
    }

    public MemResult executeCommand(String[] command) {
        return executeCommand(command,(String)null);
    }

    public MemResult executeCommand(String[] command, String in) {
        StringBuilder out=new StringBuilder();
        StringBuilder err=new StringBuilder();
        ByteArrayPrintStream oout = new ByteArrayPrintStream();
        ByteArrayPrintStream oerr = new ByteArrayPrintStream();
        JShellFileContext newContext = createNewContext(getRootContext(), command[0], Arrays.copyOfRange(command, 1, command.length));
        newContext.setIn(new ByteArrayInputStream(in == null ? new byte[0] : in.toString().getBytes()));
        newContext.setOut(oout);
        newContext.setErr(oerr);
        int r=executeCommand(command, newContext);
        out.append(oout.toString());
        err.append(oerr.toString());
        return new MemResult(out.toString(),err.toString(),r);
    }

    public static class MemResult{
        private String out;
        private String err;
        private int exitCode;

        public MemResult(String out, String err, int exitCode) {
            this.out = out;
            this.err = err;
            this.exitCode = exitCode;
        }

        public String out() {
            return out;
        }

        public String err() {
            return err;
        }

        public int exitCode() {
            return exitCode;
        }
    }
}
