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
package net.thevpc.nuts.runtime.standalone.executor.java;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NClassLoaderNode;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineFormat;
import net.thevpc.nuts.cmdline.NWorkspaceCmdLineParser;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;
import net.thevpc.nuts.io.NTerminalMode;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.executor.embedded.ClassloaderAwareRunnableImpl;
import net.thevpc.nuts.runtime.standalone.io.net.util.NetUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.runtime.standalone.io.util.IProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNClassLoader;
import net.thevpc.nuts.runtime.standalone.util.NDebugString;
import net.thevpc.nuts.runtime.standalone.extension.DefaultNExtensions;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.NRecommendationPhase;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.recom.RequestQueryInfo;
import net.thevpc.nuts.spi.NComponentScope;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.log.NLogConfig;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

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
@NComponentScope(NScopeType.WORKSPACE)
public class JavaExecutorComponent implements NExecutorComponent {

    public static NId ID;

    @Override
    public NId getId() {
        return ID;
    }

    @Override
    public int exec(NExecutionContext executionContext) {
        return execHelper(executionContext).exec();
    }


    @Override
    public int getSupportLevel(NSupportLevelContext ctx) {
        if (ID == null) {
            ID = NId.of("net.thevpc.nuts.exec:java").get();
        }
        NDefinition def = ctx.getConstraints(NDefinition.class);
        if (def != null) {
            String shortName = def.getId().getShortName();
            //for executors
            if ("net.thevpc.nuts.exec:exec-java".equals(shortName)) {
                return NConstants.Support.DEFAULT_SUPPORT + 10;
            }
            if ("java".equals(shortName)) {
                return NConstants.Support.DEFAULT_SUPPORT + 10;
            }
            if ("jar".equals(def.getDescriptor().getPackaging())) {
                return NConstants.Support.DEFAULT_SUPPORT + 10;
            }
        }
        return NConstants.Support.NO_SUPPORT;
    }

    public static NWorkspaceOptionsBuilder createChildOptions(NExecutionContext executionContext) {
        NSession session = executionContext.getSession();
        NWorkspaceOptionsBuilder options = NBootManager.of().getBootOptions().toWorkspaceOptions().builder();
        options.setDry(executionContext.isDry());
        options.setBot(executionContext.isBot());

        //copy session parameters to the newly created workspace
        options.setShowStacktrace(session.getShowStacktrace().orDefault());
        options.setGui(session.isGui());
        options.setOutLinePrefix(session.getOutLinePrefix());
        options.setErrLinePrefix(session.getErrLinePrefix());
        options.setDebug(session.getDebug().orDefault());
        options.setTrace(session.isTrace());
        options.setPreviewRepo(session.isPreviewRepo());
        options.setCached(session.isCached());
        options.setIndexed(session.isIndexed());
        options.setConfirm(session.getConfirm().orDefault());
        options.setTransitive(session.isTransitive());
        options.setOutputFormat(session.getOutputFormat().orDefault());
        switch (options.getTerminalMode().orElse(NTerminalMode.DEFAULT)) {
            //retain filtered
            case DEFAULT:
                options.setTerminalMode(session.getTerminal().out().getTerminalMode());
                //retain filtered
            case FILTERED:
                break;
            //retain inherited
            case INHERITED:
                break;
            default:
                options.setTerminalMode(session.getTerminal().out().getTerminalMode());
                break;
        }
        options.setExpireTime(session.getExpireTime().orNull());

        Filter logFileFilter = session.getLogFileFilter();
        Filter logTermFilter = session.getLogTermFilter();
        Level logTermLevel = session.getLogTermLevel();
        Level logFileLevel = session.getLogFileLevel();
        if (logFileFilter != null || logTermFilter != null || logTermLevel != null || logFileLevel != null) {
            NLogConfig lc = options.getLogConfig().orNull();
            if (lc == null) {
                lc = new NLogConfig();
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
        for (Iterator<String> iterator = executionContext.getExecutorOptions().iterator(); iterator.hasNext(); ) {
            String a = iterator.next();
            if (a.startsWith("-Dnuts.args=")) {
                executionContext.getWorkspaceOptions().add(a.substring("-Dnuts.args=".length()));
                iterator.remove();
            }
        }
        for (String a : executionContext.getWorkspaceOptions()) {
            NWorkspaceOptions extraOptions = NWorkspaceOptionsBuilder.of().setCmdLine(
                    NCmdLine.parseDefault(a).get().toStringArray()
            ).readOnly();
            options.setAllPresent(extraOptions);
        }
        //sandbox workspace children are always confined
        if (options.getIsolationLevel().orNull() == NIsolationLevel.SANDBOX) {
            options.setIsolationLevel(NIsolationLevel.CONFINED);
        }
        options.unsetCreationOptions().unsetRuntimeOptions();
        return options;
    }

    //@Override
    public IProcessExecHelper execHelper(NExecutionContext executionContext) {
        NDefinition def = executionContext.getDefinition();
        Path contentFile = def.getContent().flatMap(NPath::toPath).orNull();
        NSession session = executionContext.getSession();
        final JavaExecutorOptions joptions = new JavaExecutorOptions(
                def,
                executionContext.isTemporary(),
                executionContext.getArguments(),
                executionContext.getExecutorOptions(),
                NBlankable.isBlank(executionContext.getDirectory()) ?
                        NPath.ofUserDirectory()
                        : executionContext.getDirectory(),
                executionContext.getWorkspace());
        switch (executionContext.getExecutionType()) {
            case EMBEDDED: {
                return new EmbeddedProcessExecHelper(def, executionContext.getWorkspace(), joptions, session.out(), executionContext);
            }
            case SPAWN:
            default: {

                HashMap<String, String> osEnv = new HashMap<>();

                NVersion nutsDependencyVersion = null;
                for (NId d : CoreNUtils.resolveNutsApiIdsFromDefinition(executionContext.getDefinition())) {
                    nutsDependencyVersion = d.getVersion();
                    if (nutsDependencyVersion != null) {
                        break;
                    }
                }
                if (nutsDependencyVersion == null) {
                    //what if nuts is added as raw classpath jar?!
                    for (String s : joptions.getClassPathNidStrings()) {
                        NId sid = NId.of(s).orNull();
                        if (sid != null && sid.equalsShortId(NId.ofApi("").orNull())) {
                            nutsDependencyVersion = sid.getVersion();
                        } else {
                            Pattern pp = Pattern.compile(".*[/\\\\]nuts-(?<v>[0-9.]+)[.]jar");
                            Matcher mm = pp.matcher(s);
                            if (mm.find()) {
                                String v = mm.group("v");
                                nutsDependencyVersion = NVersion.of(v).get();
                                break;
                            }
                        }
                    }
                }


                NWorkspaceOptionsBuilder options = createChildOptions(executionContext);
                NWorkspaceOptionsConfig config = new NWorkspaceOptionsConfig().setCompact(true);
                if (nutsDependencyVersion != null) {
                    config.setApiVersion(nutsDependencyVersion);
                    // there is no need to specify api/runtime because we are
                    // willing to run that specific version anyways...
                    options.setApiVersion(null);
                    options.setRuntimeId(null);
                }

                NCmdLine ncmdLine = options.toCmdLine(config);
                if (!joptions.getExtraNutsOptions().isEmpty()) {
                    NCmdLine zzz = NCmdLine.of(joptions.getExtraNutsOptions());
                    while (!zzz.isEmpty()) {
                        List<NArg> z = NWorkspaceCmdLineParser.nextNutsArgument(zzz, options).orNull();
                        if (z == null) {
                            zzz.skip();
                        }
                    }
                }
                List<String> extraStartWithAppArgs = new ArrayList<>();

                if (def.getId().equalsShortId(session.getWorkspace().getApiId())) {
                    extraStartWithAppArgs.addAll(ncmdLine.toStringList());
                }
                String bootArgumentsString = NCmdLineFormat.of(ncmdLine
                        .add(executionContext.getDefinition().getId().getLongName())
                        ).setShellFamily(NShellFamily.SH).setNtf(false).toString();
                if (!NBlankable.isBlank(bootArgumentsString)) {
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
                int maxDepth = Math.abs(NLiteral.of(sysProperties.getProperty("nuts.export.watchdog.max-depth")).asInt().orElse(24));
                if (maxDepth > 64) {
                    maxDepth = 64;
                }
                int currentDepth = NLiteral.of(sysProperties.getProperty("nuts.export.watchdog.depth")).asInt().orElse(-1);
                currentDepth++;
                if (currentDepth > maxDepth) {
                    session.err().println("[[Process Stack Overflow Error]]");
                    session.err().println("it is very likely that you executed an infinite process creation recursion in your program.");
                    session.err().println("at least " + currentDepth + " (>=" + maxDepth + ") processes were created.");
                    session.err().println("are you aware of such misconception ?");
                    session.err().println("sorry but we need to end all of this disgracefully...");
                    System.exit(233);
                }

                List<NText> xargs = new ArrayList<>();
                List<String> args = new ArrayList<>();

                NTexts txt = NTexts.of();
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
                NDebugString jdb = NDebugString.of(session.getDebug().orDefault());
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
                        throw new NIllegalArgumentException(NMsg.ofC("unable to resolve valid debug port %d-%d", port, port + 1000));
                    }
                    String ds = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=" + (jdb.isSuspend() ? 'y' : 'n') + ",address=" + port;
                    xargs.add(txt.ofPlain(ds));
                    args.add(ds);
                }

                if (joptions.isJar()) {
                    xargs.add(txt.ofPlain("-jar"));
                    xargs.add(NIdFormat.of(def.getId()).format());
                    args.add("-jar");
                    args.add(contentFile.toString());
                } else {
                    xargs.add(txt.ofPlain("--nuts-path"));
                    xargs.add(
                            txt.ofBuilder().appendJoined(
                                    ";", joptions.getClassPathNidStrings()
                            ).immutable()
                    );
                    xargs.add(txt.ofPlain(
                                    joptions.getMainClass()
                            )
                    );

                    if (!joptions.getJ9_modulePath().isEmpty()) {
                        args.add("--module-path");
                        args.add(joptions.getJ9_modulePath().stream().distinct().collect(Collectors.joining(File.pathSeparator)));
                    }
                    if (!joptions.getJ9_upgradeModulePath().isEmpty()) {
                        args.add("--upgradable-module-path");
                        args.add(joptions.getJ9_upgradeModulePath().stream().distinct().collect(Collectors.joining(",")));
                    }
                    if (!joptions.getJ9_addModules().isEmpty()) {
                        args.add("--add-modules");
                        args.add(joptions.getJ9_addModules().stream().distinct().collect(Collectors.joining(",")));
                    }
                    if (!NBlankable.isBlank(joptions.getSplash())) {
                        args.add("-splash:" + NStringUtils.trim(joptions.getSplash()));
                    }
                    List<String> classPathStrings = joptions.getClassPath();
                    if (!classPathStrings.isEmpty()) {
                        args.add("-classpath");
                        args.add(classPathStrings.stream().distinct().collect(Collectors.joining(File.pathSeparator)));
                    }
                    args.add(joptions.getMainClass());
                }

                xargs.addAll(
                        extraStartWithAppArgs.stream()
                                .map(txt::ofPlain)
                                .collect(Collectors.toList())
                );
                xargs.addAll(
                        joptions.getAppArgs().stream()
                                .map(txt::ofPlain)
                                .collect(Collectors.toList())
                );

                args.addAll(extraStartWithAppArgs);
                args.addAll(joptions.getAppArgs());


                return new JavaProcessExecHelper(xargs, joptions, executionContext, def, args, osEnv);

            }

        }
    }

    static class EmbeddedProcessExecHelper extends AbstractSyncIProcessExecHelper {

        private final NDefinition def;
        private final JavaExecutorOptions joptions;
        private final NPrintStream out;
        private final NExecutionContext executionContext;

        public EmbeddedProcessExecHelper(NDefinition def, NWorkspace workspace, JavaExecutorOptions joptions, NPrintStream out, NExecutionContext executionContext) {
            super(workspace);
            this.def = def;
            this.joptions = joptions;
            this.out = out;
            this.executionContext = executionContext;
        }

        @Override
        public int exec() {
            NSession session = workspace.currentSession();
            if (executionContext.isDry()) {
                NTexts text = NTexts.of();
                List<String> cmdLine = new ArrayList<>();
                cmdLine.add("embedded-java");
                cmdLine.add("-cp");
                cmdLine.add(joptions.getClassPathNodes().stream().map(NClassLoaderNode::getId).collect(Collectors.joining(":")));
                cmdLine.add(joptions.getMainClass());
                cmdLine.addAll(joptions.getAppArgs());

                session.out().println(NMsg.ofC("[dry] %s",
                        text.ofBuilder()
                                .append("exec", NTextStyle.pale())
                                .append(" ")
                                .append(NCmdLine.of(cmdLine))
                ));
                return NExecutionException.SUCCESS;
            }
            //we must make a copy not to alter caller session

            if (session.out() != null) {
                session.out().resetLine();
            }
            DefaultNClassLoader classLoader;
            Throwable th = null;
            try {
                classLoader = ((DefaultNExtensions) NExtensions.of()).getModel().getNutsURLClassLoader(
                        def.getId().toString(),
                        null//getSession().getWorkspace().config().getBootClassLoader()
                );
                for (NClassLoaderNode n : joptions.getClassPathNodes()) {
                    classLoader.add(n);
                }
                if (joptions.getMainClass() == null) {
                    if (joptions.isJar()) {
                        throw new NIllegalArgumentException(NMsg.ofC("jar mode and embedded mode are exclusive for %s", def.getId()));
                    } else {
                        throw new NIllegalArgumentException(NMsg.ofC("unable resolve class name for %s", def.getId()));
                    }
                }
                Class<?> cls = Class.forName(joptions.getMainClass(), true, classLoader);
                th = session.copy().callWith(() -> {
                    Throwable th2 = null;
                    try {
                        new ClassloaderAwareRunnableImpl(def.getId(), classLoader, cls, session, joptions, executionContext).runAndWaitFor();
                    } catch (InvocationTargetException e) {
                        th2 = e.getTargetException();
                    } catch (MalformedURLException | NoSuchMethodException | SecurityException
                             | IllegalAccessException | IllegalArgumentException
                             | ClassNotFoundException e) {
                        th2 = e;
                    } catch (Throwable ex) {
                        th2 = ex;
                    }
                    return th2;
                });
            } catch (Throwable ex) {
                th = ex;
            }
            if (th != null) {
                if (!(th instanceof NExecutionException)) {
                    NWorkspaceExt.of().getModel().recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), th), NRecommendationPhase.EXEC, false);
                    throw new NExecutionException(
                            NMsg.ofC("error executing %s : %s", def.getId(), th)
                            , th);
                }
                NExecutionException nex = (NExecutionException) th;
                if (nex.getExitCode() != NExecutionException.SUCCESS) {
                    if (def != null) {
                        NWorkspaceExt.of().getModel().recomm.getRecommendations(new RequestQueryInfo(def.getId().toString(), nex), NRecommendationPhase.EXEC, false);
                    }
                    throw new NExecutionException(NMsg.ofC("error executing %s : %s", def == null ? null : def.getId(), th), th);
                }
            }
            return NExecutionException.SUCCESS;
        }
    }

}
