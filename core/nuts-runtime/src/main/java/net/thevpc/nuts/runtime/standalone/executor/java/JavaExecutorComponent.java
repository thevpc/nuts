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
package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.executor.embedded.ClassloaderAwareRunnableImpl;
import net.thevpc.nuts.runtime.standalone.io.net.util.NetUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.StringKeyValueList;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsClassLoader;
import net.thevpc.nuts.runtime.standalone.util.CoreNumberUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsDebugString;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.spi.NutsComponentScope;
import net.thevpc.nuts.spi.NutsComponentScopeType;
import net.thevpc.nuts.spi.NutsExecutorComponent;
import net.thevpc.nuts.spi.NutsSupportLevelContext;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Filter;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by vpc on 1/7/17.
 */
@NutsComponentScope(NutsComponentScopeType.WORKSPACE)
public class JavaExecutorComponent implements NutsExecutorComponent {

    public static NutsId ID;
    NutsSession ws;

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
    public int getSupportLevel(NutsSupportLevelContext ctx) {
        this.ws = ctx.getSession();
        if (ID == null) {
            ID = NutsId.of("net.thevpc.nuts.exec:java", ws);
        }
        NutsDefinition def = ctx.getConstraints(NutsDefinition.class);
        if (def != null) {
            String shortName = def.getId().getShortName();
            //for executors
            if ("net.thevpc.nuts.exec:exec-java".equals(shortName)) {
                return DEFAULT_SUPPORT + 10;
            }
            if ("java".equals(shortName)) {
                return DEFAULT_SUPPORT + 10;
            }
            if ("jar".equals(def.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 10;
            }
        }
        return NO_SUPPORT;
    }

    //@Override
    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        NutsDefinition def = executionContext.getDefinition();
        Path contentFile = def.getFile();
        NutsSession session = executionContext.getSession();
        final JavaExecutorOptions joptions = new JavaExecutorOptions(
                def,
                executionContext.isTemporary(),
                executionContext.getArguments(),
                executionContext.getExecutorArguments(),
                NutsBlankable.isBlank(executionContext.getCwd()) ? System.getProperty("user.dir") : executionContext.getCwd(),
                session);
        final NutsSession execSession = executionContext.getExecSession();
        switch (executionContext.getExecutionType()) {
            case EMBEDDED: {
                return new EmbeddedProcessExecHelper(def, execSession, joptions, execSession.out());
            }
            case SPAWN:
            default: {
                StringKeyValueList runnerProps = new StringKeyValueList();
                if (executionContext.getExecutorDescriptor() != null) {
                    runnerProps.add(executionContext.getExecutorDescriptor().getProperties());
                }

                if (executionContext.getEnv() != null) {
                    runnerProps.add(executionContext.getEnv());
                }

                HashMap<String, String> osEnv = new HashMap<>();
                NutsWorkspaceOptionsBuilder options = ws.boot().getBootOptions().builder();

                //copy session parameters to the newly created workspace
                options.setDry(execSession.isDry());
                options.setGui(execSession.isGui());
                options.setOutLinePrefix(execSession.getOutLinePrefix());
                options.setErrLinePrefix(execSession.getErrLinePrefix());
                options.setDebug(execSession.getDebug());
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
                for (NutsId d : CoreNutsUtils.resolveNutsApiIds(executionContext.getDefinition(), session)) {
                    nutsDependencyVersion = d.getVersion();
                    if(nutsDependencyVersion!=null){
                        break;
                    }
                }
                if(nutsDependencyVersion==null) {
                    //what if nuts is added as raw classpath jar?!
                    for (String s : joptions.getClassPathNidStrings()) {
                        if (s.startsWith("net.thevpc.nuts:nuts#")) {
                            String v = s.substring("net.thevpc.nuts:nuts#".length());
                            nutsDependencyVersion = NutsVersion.of(v, session);
                        } else {
                            Pattern pp = Pattern.compile(".*[/\\\\]nuts-(?<v>[0-9.]+)[.]jar");
                            Matcher mm = pp.matcher(s);
                            if (mm.find()) {
                                String v = mm.group("v");
                                nutsDependencyVersion = NutsVersion.of(v, session);
                                break;
                            }
                        }
                    }
                }
                options.setExpireTime(execSession.getExpireTime());

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
                NutsWorkspaceOptionsBuilder options2 = options.copy();
                if(nutsDependencyVersion!=null){
                    options2.setApiVersion(nutsDependencyVersion.toString());
                    options2.setRuntimeId(null);
                }
                String bootArgumentsString = options2.formatter()
                        .setExported(true)
                        .setCompact(true)
                        .getBootCommandLine().formatter().setShellFamily(NutsShellFamily.SH).setNtf(false).toString();
                if (!NutsBlankable.isBlank(bootArgumentsString)) {
                    osEnv.put("NUTS_BOOT_ARGS", bootArgumentsString);
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
                    session.err().println("[[Process Stack Overflow Error]]");
                    session.err().println("it is very likely that you executed an infinite process creation recursion in your program.");
                    session.err().println("at least " + currentDepth + " (>=" + maxDepth + ") processes were created.");
                    session.err().println("are you aware of such misconception ?");
                    session.err().println("sorry but we need to end all of this disgracefully...");
                    System.exit(233);
                }

                List<NutsString> xargs = new ArrayList<>();
                List<String> args = new ArrayList<>();

                NutsTexts txt = NutsTexts.of(session);
                xargs.add(txt.ofPlain(joptions.getJavaCommand()));
                xargs.addAll(
                        joptions.getJvmArgs().stream()
                                .map(txt::ofPlain)
                                .collect(Collectors.toList())
                );

                args.add(joptions.getJavaCommand());
                args.addAll(joptions.getJvmArgs());

//                if (!NutsBlankable.isBlank(bootArgumentsString)) {
//                    String Dnuts_boot_args = "-Dnuts.boot.args=" + bootArgumentsString;
//                    xargs.add(Dnuts_boot_args);
//                    args.add(Dnuts_boot_args);
//                }
                NutsDebugString jdb = NutsDebugString.of(session.getDebug(), session);
                if (jdb.isEnabled()) {
                    int port = jdb.getPort();
                    if (port <= 0) {
                        port = 5005;
                    }
                    int maxPort = jdb.getMaxPort();
                    if (maxPort < port) {
                        maxPort = port + 1000;
                    }
                    port = NetUtils.detectRandomFreeTcpPort(port, maxPort + 1);
                    if (port < 0) {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unable to resolve valid debug port %d-%d", port, port + 1000));
                    }
                    String ds = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=" + (jdb.isSuspend() ? 'y' : 'n') + ",address=" + port;
                    xargs.add(txt.ofPlain(ds));
                    args.add(ds);
                }

                if (joptions.isJar()) {
                    xargs.add(txt.ofPlain("-jar"));
                    xargs.add(def.getId().formatter().format());

                    args.add("-jar");
                    args.add(contentFile.toString());
                } else {
                    xargs.add(txt.ofPlain("--nuts-path"));
                    xargs.add(
                            txt.builder().appendJoined(
                                    ";", joptions.getClassPathNidStrings()
                            ).immutable()
                    );
                    xargs.add(txt.ofPlain(
                                    joptions.getMainClass()
                            )
                    );

                    if(!joptions.getJ9_modulePath().isEmpty()){
                        args.add("--module-path");
                        args.add(joptions.getJ9_modulePath().stream().distinct().collect(Collectors.joining(File.pathSeparator)));
                    }
                    if(!joptions.getJ9_upgradeModulePath().isEmpty()){
                        args.add("--upgradable-module-path");
                        args.add(joptions.getJ9_upgradeModulePath().stream().distinct().collect(Collectors.joining(",")));
                    }
                    if(!joptions.getJ9_addModules().isEmpty()){
                        args.add("--add-modules");
                        args.add(joptions.getJ9_addModules().stream().distinct().collect(Collectors.joining(",")));
                    }
                    if(!NutsBlankable.isBlank(joptions.getSplash())){
                        args.add("-splash:"+NutsUtilStrings.trim(joptions.getSplash()));
                    }
                    List<String> classPathStrings = joptions.getClassPath();
                    if(!classPathStrings.isEmpty()) {
                        args.add("-classpath");
                        args.add(classPathStrings.stream().distinct().collect(Collectors.joining(File.pathSeparator)));
                    }
                    args.add(joptions.getMainClass());
                }
                xargs.addAll(
                        joptions.getAppArgs().stream()
                                .map(txt::ofPlain)
                                .collect(Collectors.toList())
                );
                args.addAll(joptions.getAppArgs());
                return new JavaProcessExecHelper(execSession, execSession, xargs, joptions, session, executionContext, def, args, osEnv);

            }

        }
    }

    static class EmbeddedProcessExecHelper extends AbstractSyncIProcessExecHelper {

        private final NutsDefinition def;
        private final JavaExecutorOptions joptions;
        private final NutsPrintStream out;

        public EmbeddedProcessExecHelper(NutsDefinition def, NutsSession session, JavaExecutorOptions joptions, NutsPrintStream out) {
            super(session);
            this.def = def;
            this.joptions = joptions;
            this.out = out;
        }

        @Override
        public void dryExec() {
            NutsSession session = getSession();
            NutsTexts text = NutsTexts.of(session);
            List<String> cmdLine = new ArrayList<>();
            cmdLine.add("embedded-java");
            cmdLine.add("-cp");
            cmdLine.add(joptions.getClassPathNodes().stream().map(NutsClassLoaderNode::getId).collect(Collectors.joining(":")));
            cmdLine.add(joptions.getMainClass());
            cmdLine.addAll(joptions.getAppArgs());

            session.out().printf("[dry] %s%n",
                    text.builder()
                            .append("exec", NutsTextStyle.pale())
                            .append(" ")
                            .append(NutsCommandLine.of(cmdLine, session))
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
                classLoader = ((DefaultNutsWorkspaceExtensionManager) session.extensions()).getModel().getNutsURLClassLoader(
                        def.getId().toString(),
                        null//getSession().getWorkspace().config().getBootClassLoader()
                        , session
                );
                for (NutsClassLoaderNode n : joptions.getClassPathNodes()) {
                    classLoader.add(n);
                }
                Class<?> cls = Class.forName(joptions.getMainClass(), true, classLoader);
                new ClassloaderAwareRunnableImpl(def.getId(), classLoader, cls, session, joptions).runAndWaitFor();
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
                    throw new NutsExecutionException(session, NutsMessage.cstyle("error executing %s : %s", def == null ? null : def.getId(), th), th);
                }
            }
            return 0;
        }
    }

}
