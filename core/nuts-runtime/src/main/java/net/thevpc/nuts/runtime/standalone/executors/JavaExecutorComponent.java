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
package net.thevpc.nuts.runtime.standalone.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.collections.StringKeyValueList;
import net.thevpc.nuts.runtime.bundles.io.IProcessExecHelper;
import net.thevpc.nuts.runtime.core.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.core.util.CoreBooleanUtils;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JavaExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsWorkspace ws;

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        execHelper(executionContext).exec();
    }

    @Override
    public void dryExec(NutsExecutionContext executionContext) throws NutsExecutionException {
        execHelper(executionContext).dryExec();
    }

    @Override
    public int getSupportLevel(NutsSupportLevelContext<NutsDefinition> nutsDefinition) {
        this.ws = nutsDefinition.getWorkspace();
        if (ID == null) {
            ID = ws.id().parser().parse("net.thevpc.nuts.exec:java");
        }
        String shortName = nutsDefinition.getConstraints().getId().getShortName();
        //for executors
        if ("net.thevpc.nuts.exec:exec-java".equals(shortName)) {
            return DEFAULT_SUPPORT + 1;
        }
        if ("java".equals(shortName)) {
            return DEFAULT_SUPPORT + 1;
        }
        if ("jar".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())) {
            return DEFAULT_SUPPORT + 1;
        }
        return NO_SUPPORT;
    }

    //@Override
    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        NutsDefinition def = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
//        boolean inheritSystemIO=CoreCommonUtils.parseBoolean(String.valueOf(executionContext.getExecutorProperties().get("inheritSystemIO")),false);
        final NutsWorkspace ws = executionContext.getWorkspace();
        Path contentFile = def.getPath();
        final JavaExecutorOptions joptions = new JavaExecutorOptions(
                def,
                executionContext.isTemporary(),
                executionContext.getArguments(),
                executionContext.getExecutorArguments(),
                CoreStringUtils.isBlank(executionContext.getCwd()) ? System.getProperty("user.dir") : executionContext.getCwd(),
                executionContext.getTraceSession()/*.copy().setProgressOptions("none")*/);
        final NutsSession execSession = executionContext.getExecSession();
        switch (executionContext.getExecutionType()) {
            case EMBEDDED: {
                return new EmbeddedProcessExecHelper(def, execSession, joptions, execSession.out());
            }
            case SPAWN:
            default: {
                StringKeyValueList runnerProps = new StringKeyValueList();
                if (executionContext.getExecutorDescriptor() != null) {
                    runnerProps.add((Map) executionContext.getExecutorDescriptor().getProperties());
                }

                if (executionContext.getEnv() != null) {
                    runnerProps.add((Map) executionContext.getEnv());
                }

                HashMap<String, String> osEnv = new HashMap<>();
                NutsWorkspaceOptionsBuilder options = ws.config().options().copy();

                //copy session parameters to the newly created workspace
                options.setDry(execSession.isDry());
                options.setGui(execSession.isGui());
                options.setOutLinePrefix(execSession.getOutLinePrefix());
                options.setErrLinePrefix(execSession.getErrLinePrefix());
                options.setDebug(execSession.isDebug());
                options.setTrace(execSession.isTrace());
                options.setBot(execSession.isBot());
                options.setCached(execSession.isCached());
                options.setIndexed(execSession.isIndexed());
                options.setConfirm(execSession.getConfirm());
                options.setTransitive(execSession.isTransitive());
                options.setOutputFormat(execSession.getOutputFormat());
                if (null == options.getTerminalMode()) {
                    options.setTerminalMode(execSession.getTerminal().out().mode());
                } else {
                    switch (options.getTerminalMode()) {
                        //retain filtered
                        case FILTERED:
                            break;
                        //retain inherited
                        case INHERITED:
                            break;
                        default:
                            options.setTerminalMode(execSession.getTerminal().out().mode());
                            break;
                    }
                }
                NutsVersion nutsDependencyVersion = null;
                for (String s : joptions.getClassPathNidStrings()) {
                    if (s.startsWith("net.thevpc.nuts:nuts#")) {
                        String v = s.substring("net.thevpc.nuts:nuts#".length());
                        nutsDependencyVersion = executionContext.getWorkspace().version().parser().parse(v);
                    } else {
                        Pattern pp = Pattern.compile(".*[/\\\\]nuts-(?<v>[0-9.]+)[.]jar");
                        Matcher mm = pp.matcher(s);
                        if (mm.find()) {
                            String v = mm.group("v");
                            nutsDependencyVersion = executionContext.getWorkspace().version().parser().parse(v);
                            break;
                        }
                    }
                }
//                List<String> validBootCommand = new ArrayList<>();
//                options.setTrace(execSession.isTrace());
                options.setExpireTime(execSession.getExpireTime());
//                options.setOutputFormat(execSession.getOutputFormat());
//                options.setConfirm(execSession.getConfirm());

                Filter logFileFilter = execSession.getLogFileFilter();
                Filter logTermFilter = execSession.getLogTermFilter();
                Level logTermLevel = execSession.getLogTermLevel();
                Level logFileLevel = execSession.getLogFileLevel();
                if (logFileFilter != null || logTermFilter != null || logTermLevel != null || logFileLevel != null) {
                    NutsLogConfig lc = options.getLogConfig();
                    if (lc == null) {
                        lc = new NutsLogConfig();
                    } else {
                        lc = lc.copy();
                    }
                    if (logTermLevel != null) {
                        lc.setLogTermLevel(logTermLevel);
                    }
                    if (logFileLevel != null) {
                        lc.setLogFileLevel(logFileLevel);
                    }
                    if (logTermFilter != null) {
                        lc.setLogTermFilter(logTermFilter);
                    }
                    if (logFileFilter != null) {
                        lc.setLogFileFilter(logFileFilter);
                    }
                }
//                options.setConfirm(execSession.getConfirm());

                String[] bootCommand = options.format().exported().setApiVersion(nutsDependencyVersion == null ? null : nutsDependencyVersion.toString())
                        .compact().getBootCommand();
//                if(nutsDependencyVersion!=null && nutsDependencyVersion.compareTo(executionContext.getWorkspace().getApiVersion())<0){
//                    if(nutsDependencyVersion.compareTo("0.8.0")<0){
//                        for (String s : bootCommand) {
//                            if(s.startsWith("-N=") || s.startsWith("--expire=")){
//                                //ignore..
//                            }else{
//                                validBootCommand.add(s);
//                            }
//                        }
//                    }else{
//                validBootCommand.addAll(Arrays.asList(bootCommand));
//                    }
//                }
                String bootArgumentsString = executionContext.getWorkspace().commandLine().create(bootCommand)
                        .toString();
                if (!CoreStringUtils.isBlank(bootArgumentsString)) {
                    osEnv.put("nuts_boot_args", bootArgumentsString);
                    joptions.getJvmArgs().add("-Dnuts.boot.args=" + bootArgumentsString);
                }
                //nuts.export properties should be propagated!!
                Properties sysProperties = System.getProperties();
                for (Object k : sysProperties.keySet()) {
                    String sk = (String) k;
                    if (sk.startsWith("nuts.export.")) {
                        joptions.getJvmArgs().add("-D" + sk + "=" + sysProperties.getProperty(sk));
                    }
                }
                // fix infinite recursion
                int maxDepth = Math.abs(CoreNumberUtils.convertToInteger(sysProperties.getProperty("nuts.export.watchdog.max-depth"), 24));
                if (maxDepth > 64) {
                    maxDepth = 64;
                }
                int currentDepth = CoreNumberUtils.convertToInteger(sysProperties.getProperty("nuts.export.watchdog.depth"), -1);
                currentDepth++;
                if (currentDepth > maxDepth) {
                    System.err.println("[[Process Stack Overflow Error]]");
                    System.err.println("it is very likely that you executed an infinite process creation recursion in your program.");
                    System.err.println("at least " + currentDepth + " (>=" + maxDepth + ") processes were created.");
                    System.err.println("are you aware of such misconception ?");
                    System.err.println("sorry but we need to end all of this disgracefully...");
                    System.exit(233);
                }

                List<NutsString> xargs = new ArrayList<>();
                List<String> args = new ArrayList<>();

                NutsTextManager txt = execSession.getWorkspace().text();
                xargs.add(txt.forPlain(joptions.getJavaHome()));
                xargs.addAll(
                        joptions.getJvmArgs().stream()
                                .map(txt::forPlain)
                                .collect(Collectors.toList())
                );

                args.add(joptions.getJavaHome());
                args.addAll(joptions.getJvmArgs());

//                if (!CoreStringUtils.isBlank(bootArgumentsString)) {
//                    String Dnuts_boot_args = "-Dnuts.boot.args=" + bootArgumentsString;
//                    xargs.add(Dnuts_boot_args);
//                    args.add(Dnuts_boot_args);
//                }
                String jdb = (String) ws.env().getOption("jdb", null);
                if (jdb != null) {
                    boolean suspend = true;
                    int port = 5005;
                    for (String arg : jdb.split("[ ,]")) {
                        arg = arg.trim();
                        if (arg.length() > 0) {
                            if (arg.matches("[0-9]+")) {
                                port = Integer.parseInt(arg);
                            } else {
                                switch (arg) {
                                    case "suspend": {
                                        suspend = true;
                                        break;
                                    }
                                    case "no-suspend": {
                                        suspend = false;
                                        break;
                                    }
                                    case "!suspend": {
                                        suspend = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    String ds = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=" + (suspend ? 'y' : 'n') + ",address=" + port;
                    xargs.add(txt.forPlain(ds));
                    args.add(ds);
                }

                if (joptions.isJar()) {
                    xargs.add(txt.forPlain("-jar"));
                    xargs.add(ws.id().formatter(def.getId()).format());

                    args.add("-jar");
                    args.add(contentFile.toString());
                } else {
                    xargs.add(txt.forPlain("--nuts-path"));
                    xargs.add(
                            txt.builder().appendJoined(
                                    ";", joptions.getClassPathNidStrings()
                            ).immutable()
                    );
                    xargs.add(txt.forPlain(
                                    joptions.getMainClass()
                            )
                    );

                    args.add("-classpath");
                    args.add(String.join(File.pathSeparator, joptions.getClassPathStrings()));
                    args.add(joptions.getMainClass());
                }
                xargs.addAll(
                        joptions.getApp().stream()
                                .map(txt::forPlain)
                                .collect(Collectors.toList())
                );
                args.addAll(joptions.getApp());
                return new JavaProcessExecHelper(execSession, execSession, xargs, joptions, ws, executionContext, def, args, osEnv);

            }

        }
    }

    static class EmbeddedProcessExecHelper extends AbstractSyncIProcessExecHelper {

        private NutsDefinition def;
        private JavaExecutorOptions joptions;
        private NutsPrintStream out;

        public EmbeddedProcessExecHelper(NutsDefinition def, NutsSession session, JavaExecutorOptions joptions, NutsPrintStream out) {
            super(session);
            this.def = def;
            this.joptions = joptions;
            this.out = out;
        }

        @Override
        public void dryExec() {
            NutsTextManager text = getSession().getWorkspace().text();
            List<String> cmdLine = new ArrayList<>();
            cmdLine.add("embedded-java");
            cmdLine.add("-cp");
            cmdLine.add(String.join(":", joptions.getClassPathStrings()));
            cmdLine.add(joptions.getMainClass());
            cmdLine.addAll(joptions.getApp());

            getSession().out().printf("[dry] %s%n",
                    text.builder()
                            .append("exec", NutsTextStyle.pale())
                            .append(" ")
                            .append(getSession().getWorkspace().commandLine().create(cmdLine))
            );
        }

        @Override
        public int exec() {
            NutsSession session = getSession();
            //we must make a copy not to alter caller session
            session = session.copy();

            if (session.out() != null) {
                session.out().resetLine();
            }
            DefaultNutsClassLoader classLoader = null;
            Throwable th = null;
            try {
                classLoader = ((DefaultNutsWorkspaceExtensionManager) session.getWorkspace().extensions()).getModel().getNutsURLClassLoader(
                        def.getId().toString(),
                        null//getSession().getWorkspace().config().getBootClassLoader()
                        , session
                );
                for (NutsClassLoaderNode n : joptions.getClassPath()) {
                    classLoader.add(n);
                }
                Class<?> cls = Class.forName(joptions.getMainClass(), true, classLoader);
                new ClassloaderAwareRunnableImpl2(def.getId(), classLoader, cls, session, joptions).runAndWaitFor();
                return 0;
            } catch (InvocationTargetException e) {
                th = e.getTargetException();
            } catch (MalformedURLException | NoSuchMethodException | SecurityException
                    | IllegalAccessException | IllegalArgumentException
                    | ClassNotFoundException e) {
                th = e;
            } catch (Throwable ex) {
                th = ex;
            }
            if (th != null) {
                if (!(th instanceof NutsExecutionException)) {
                    throw new NutsExecutionException(session,
                            NutsMessage.cstyle("error executing %s : %s", def.getId(), th)
                            , th);
                }
                NutsExecutionException nex = (NutsExecutionException) th;
                if (nex.getExitCode() != 0) {
                    throw new NutsExecutionException(session, NutsMessage.cstyle("error executing %s : %s", def.getId(), th), th);
                }
            }
            return 0;
        }
    }

    static class ClassloaderAwareRunnableImpl2 extends ClassloaderAwareRunnable {

        private final Class<?> cls;
        private final JavaExecutorOptions joptions;
        private final NutsId id;

        public ClassloaderAwareRunnableImpl2(NutsId id, ClassLoader classLoader, Class<?> cls, NutsSession session, JavaExecutorOptions joptions) {
            super(session.copy(), classLoader);
            this.id = id;
            this.cls = cls;
            this.joptions = joptions;
        }

        @Override
        public Object runWithContext() throws Throwable {
            Method mainMethod = null;
            if (cls.getName().equals("net.thevpc.nuts.Nuts")) {
                mainMethod = cls.getMethod("run", NutsSession.class, String[].class);
                mainMethod.setAccessible(true);
                NutsApplications.getSharedMap().put("nuts.embedded.application.id", id);
                mainMethod.invoke(null, new Object[]{getSession(), joptions.getApp().toArray(new String[0])});
                return null;
            }
            boolean isNutsApp = false;
            Object nutsApp = null;
            try {
                mainMethod = cls.getMethod("run", NutsSession.class, String[].class);
                mainMethod.setAccessible(true);
                for (Class<?> pi : cls.getInterfaces()) {
                    //this is the old nuts apps (version >= 0.8.1)
                    if (pi.getName().equals("net.thevpc.nuts.NutsApplication")) {
                        isNutsApp = true;
                        break;
                    }
                }
                Class p = cls.getSuperclass();
                while (!isNutsApp && p != null) {
                    if (p.getName().equals("net.thevpc.nuts.NutsApplication")
                            //this is the old nuts apps (version < 0.8.0)
                            || p.getName().equals("net.vpc.app.nuts.NutsApplication")) {
                        isNutsApp = true;
                        break;
                    }
                    for (Class<?> pi : cls.getInterfaces()) {
                        //this is the old nuts apps (version >= 0.8.1)
                        if (pi.getName().equals("net.thevpc.nuts.NutsApplication")) {
                            isNutsApp = true;
                            break;
                        }
                    }
                    p = p.getSuperclass();
                }
                if (isNutsApp) {
                    isNutsApp = false;
                    nutsApp = cls.getConstructor().newInstance();
                    isNutsApp = true;
                }
            } catch (Exception rr) {
                //ignore

            }
            if (isNutsApp) {
                //NutsWorkspace
                NutsApplications.getSharedMap().put("nuts.embedded.application.id", id);
                mainMethod.invoke(nutsApp, new Object[]{getSession().copy(), joptions.getApp().toArray(new String[0])});
            } else {
                //NutsWorkspace
                System.setProperty("nuts.boot.args", getSession().getWorkspace().config().options()
                        .format().exported().compact().getBootCommandLine()
                );
                mainMethod = cls.getMethod("main", String[].class);
//                List<String> nargs = new ArrayList<>();
//                nargs.addAll(joptions.getApp());
                mainMethod.invoke(null, new Object[]{joptions.getApp().toArray(new String[0])});
            }
            return null;
        }

    }

    private static class JavaProcessExecHelper extends AbstractSyncIProcessExecHelper {

        private final NutsSession execSession;
        private final List<NutsString> xargs;
        private final JavaExecutorOptions joptions;
        private final NutsWorkspace ws;
        private final NutsExecutionContext executionContext;
        private final NutsDefinition def;
        private final List<String> args;
        private final HashMap<String, String> osEnv;

        public JavaProcessExecHelper(NutsSession ns, NutsSession execSession, List<NutsString> xargs, JavaExecutorOptions joptions, NutsWorkspace ws, NutsExecutionContext executionContext, NutsDefinition def, List<String> args, HashMap<String, String> osEnv) {
            super(ns);
            this.execSession = execSession;
            this.xargs = xargs;
            this.joptions = joptions;
            this.ws = ws;
            this.executionContext = executionContext;
            this.def = def;
            this.args = args;
            this.osEnv = osEnv;
        }

        @Override
        public void dryExec() {
            NutsPrintStream out = execSession.out();
            out.println("[dry] ==[nuts-exec]== ");
            for (int i = 0; i < xargs.size(); i++) {
                NutsString xarg = xargs.get(i);
//                if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
//                    for (String s : xarg.split(";")) {
//                        out.println("\t\t\t " + s);
//                    }
//                } else {
                out.println("\t\t " + xarg);
//                }
            }
            String directory = CoreStringUtils.isBlank(joptions.getDir()) ? null : ws.io().expandPath(joptions.getDir());
            NutsWorkspaceUtils.of(executionContext.getTraceSession()).execAndWait(def,
                    executionContext.getTraceSession(),
                    execSession,
                    executionContext.getExecutorProperties(),
                    args.toArray(new String[0]),
                    osEnv, directory, joptions.isShowCommand(), true,
                    executionContext.getSleepMillis(),
                    executionContext.isInheritSystemIO(), false,
                    CoreStringUtils.isBlank(executionContext.getRedirectOuputFile()) ? null : new File(executionContext.getRedirectOuputFile()),
                    CoreStringUtils.isBlank(executionContext.getRedirectInpuFile()) ? null : new File(executionContext.getRedirectInpuFile())
            ).dryExec();
        }

        @Override
        public int exec() {
            return preExec().exec();
        }

        private CoreIOUtils.ProcessExecHelper preExec() {
            if (joptions.isShowCommand() || CoreBooleanUtils.getSysBoolNutsProperty("show-command", false)) {
                NutsPrintStream out = execSession.out();
                out.printf("%s %n", ws.text().forStyled("nuts-exec", NutsTextStyle.primary1()));
                for (int i = 0; i < xargs.size(); i++) {
                    NutsString xarg = xargs.get(i);
//                    if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
//                        for (String s : xarg.split(";")) {
//                            out.println("\t\t\t " + s);
//                        }
//                    } else {
                    out.println("\t\t " + xarg);
//                    }
                }
            }
            String directory = CoreStringUtils.isBlank(joptions.getDir()) ? null : ws.io().expandPath(joptions.getDir());
            return NutsWorkspaceUtils.of(executionContext.getTraceSession()).execAndWait(def,
                    executionContext.getTraceSession(),
                    execSession,
                    executionContext.getExecutorProperties(),
                    args.toArray(new String[0]),
                    osEnv, directory, joptions.isShowCommand(), true,
                    executionContext.getSleepMillis(),
                    executionContext.isInheritSystemIO(), false,
                    CoreStringUtils.isBlank(executionContext.getRedirectOuputFile()) ? null : new File(executionContext.getRedirectOuputFile()),
                    CoreStringUtils.isBlank(executionContext.getRedirectInpuFile()) ? null : new File(executionContext.getRedirectInpuFile())
            );
        }

        @Override
        public Future<Integer> execAsync() {
            return preExec().execAsync();
        }
    }

}
