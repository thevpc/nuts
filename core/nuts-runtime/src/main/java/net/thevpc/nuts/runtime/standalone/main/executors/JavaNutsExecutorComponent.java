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
package net.thevpc.nuts.runtime.standalone.main.executors;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.io.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.ext.DefaultNutsWorkspaceExtensionManager;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.common.CoreCommonUtils;
import net.thevpc.nuts.runtime.standalone.util.common.StringKeyValueList;
import net.thevpc.nuts.runtime.standalone.util.io.IProcessExecHelper;
import net.thevpc.nuts.NutsExecutorComponent;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JavaNutsExecutorComponent implements NutsExecutorComponent {

    public static final NutsId ID = CoreNutsUtils.parseNutsId("net.thevpc.nuts.exec:exec-java");

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
        if (nutsDefinition != null) {
            if ("jar".equals(nutsDefinition.getConstraints().getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    //@Override
    public IProcessExecHelper execHelper(NutsExecutionContext executionContext) {
        NutsDefinition def = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        final NutsWorkspace ws = executionContext.getWorkspace();
        Path contentFile = def.getPath();
        final JavaExecutorOptions joptions = new JavaExecutorOptions(
                def,
                executionContext.isTemporary(),
                executionContext.getArguments(),
                executionContext.getExecutorArguments(),
                CoreStringUtils.isBlank(executionContext.getCwd()) ? System.getProperty("user.dir") : executionContext.getCwd(),
                executionContext.getTraceSession().copy().setProgressOptions("none"));
        switch (executionContext.getExecutionType()) {
            case EMBEDDED: {
                return new EmbeddedProcessExecHelper(def, executionContext.getExecSession(), joptions, executionContext.getExecSession().out());
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

                //copy session parameters to new created workspace
                options.setTrace(executionContext.getExecSession().isTrace());
                options.setCached(executionContext.getExecSession().isCached());
                options.setIndexed(executionContext.getExecSession().isIndexed());
                options.setConfirm(executionContext.getExecSession().getConfirm());
                options.setTransitive(executionContext.getExecSession().isTransitive());
                options.setOutputFormat(executionContext.getExecSession().getOutputFormat());
                if (options.getTerminalMode() == NutsTerminalMode.FILTERED) {
                    //retain filtered
                } else if (options.getTerminalMode() == NutsTerminalMode.INHERITED) {
                    //retain inherited
                } else {
                    options.setTerminalMode(executionContext.getExecSession().getTerminal().getOutMode());
                }
                NutsVersion nutsDependencyVersion = null;
                for (String s : joptions.getClassPath()) {
                    Pattern pp = Pattern.compile(".*[/\\\\]nuts-(?<v>.+)[.]jar");
                    Matcher mm = pp.matcher(s);
                    if (mm.find()) {
                        String v = mm.group("v");
                        nutsDependencyVersion = executionContext.getWorkspace().version().parser().parse(v);
                        break;
                    }
                }
//                List<String> validBootCommand = new ArrayList<>();
                options.setTrace(executionContext.getExecSession().isTrace());
                options.setExpireTime(executionContext.getExecSession().getExpireTime());
                options.setOutputFormat(executionContext.getExecSession().getOutputFormat());
                options.setConfirm(executionContext.getExecSession().getConfirm());

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
                int maxDepth = Math.abs(CoreCommonUtils.convertToInteger(sysProperties.getProperty("nuts.export.watchdog.max-depth"), 24));
                if (maxDepth > 512) {
                    maxDepth = 512;
                }
                int currentDepth = CoreCommonUtils.convertToInteger(sysProperties.getProperty("nuts.export.watchdog.depth"), -1);
                currentDepth++;
                if (currentDepth > maxDepth) {
                    System.err.println("############# Process Stack Overflow Error");
                    System.err.println("It is very likely that you executed an infinite process creation recusion in your program.");
                    System.err.println("At least " + currentDepth + " (>=" + maxDepth + ") prcosses were created.");
                    System.err.println("Are ou aware of such misconception ?");
                    System.err.println("Sorry but nee to end all of this disgracely...");
                    System.exit(233);
                }

                List<String> xargs = new ArrayList<>();
                List<String> args = new ArrayList<>();

                xargs.add(joptions.getJavaHome());
                xargs.addAll(joptions.getJvmArgs());

                args.add(joptions.getJavaHome());
                args.addAll(joptions.getJvmArgs());

                if (!CoreStringUtils.isBlank(bootArgumentsString)) {
                    String Dnuts_boot_args = "-Dnuts.boot.args=" + bootArgumentsString;
                    xargs.add(Dnuts_boot_args);
                    args.add(Dnuts_boot_args);
                }
                if (joptions.isJar()) {
                    xargs.add("-jar");
                    xargs.add(ws.id().formatter(def.getId()).format());

                    args.add("-jar");
                    args.add(contentFile.toString());
                } else {
                    xargs.add("--nuts-path");
                    xargs.add(String.join(";", joptions.getNutsPath()));
                    xargs.add(joptions.getMainClass());

                    args.add("-classpath");
                    args.add(String.join(File.pathSeparator, joptions.getClassPath()));
                    args.add(joptions.getMainClass());
                }
                xargs.addAll(joptions.getApp());
                args.addAll(joptions.getApp());
                return new AbstractSyncIProcessExecHelper(executionContext.getExecSession()) {
                    @Override
                    public void dryExec() {
                        PrintStream out = executionContext.getExecSession().out();
                        out.println("[dry] ==[nuts-exec]== ");
                        for (int i = 0; i < xargs.size(); i++) {
                            String xarg = xargs.get(i);
                            if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
                                for (String s : xarg.split(";")) {
                                    out.println("\t\t\t " + s);
                                }
                            } else {
                                out.println("\t\t " + xarg);
                            }
                        }
                        String directory = CoreStringUtils.isBlank(joptions.getDir()) ? null : ws.io().expandPath(joptions.getDir());
                        NutsWorkspaceUtils.of(executionContext.getWorkspace()).execAndWait(def,
                                executionContext.getTraceSession(),
                                executionContext.getExecSession(),
                                executionContext.getExecutorProperties(),
                                args.toArray(new String[0]),
                                osEnv, directory, joptions.isShowCommand(), true,
                                executionContext.getSleepMillis()

                        ).dryExec();
                    }

                    @Override
                    public int exec() {
                        return preExec().exec();
                    }

                    private CoreIOUtils.ProcessExecHelper preExec() {
                        if (joptions.isShowCommand() || CoreCommonUtils.getSysBoolNutsProperty("show-command", false)) {
                            PrintStream out = executionContext.getExecSession().out();
                            out.println("##[nuts-exec]## ");
                            for (int i = 0; i < xargs.size(); i++) {
                                String xarg = xargs.get(i);
                                if (i > 0 && xargs.get(i - 1).equals("--nuts-path")) {
                                    for (String s : xarg.split(";")) {
                                        out.println("\t\t\t " + s);
                                    }
                                } else {
                                    out.println("\t\t " + xarg);
                                }
                            }
                        }
                        String directory = CoreStringUtils.isBlank(joptions.getDir()) ? null : ws.io().expandPath(joptions.getDir());
                        return NutsWorkspaceUtils.of(executionContext.getWorkspace()).execAndWait(def,
                                executionContext.getTraceSession(),
                                executionContext.getExecSession(),
                                executionContext.getExecutorProperties(),
                                args.toArray(new String[0]),
                                osEnv, directory, joptions.isShowCommand(), true,
                                executionContext.getSleepMillis()
                        );
                    }

                    @Override
                    public Future<Integer> execAsync() {
                        return preExec().execAsync();
                    }
                };

            }
        }
    }

    static class EmbeddedProcessExecHelper extends AbstractSyncIProcessExecHelper {
        private NutsDefinition def;
        private JavaExecutorOptions joptions;
        private PrintStream out;

        public EmbeddedProcessExecHelper(NutsDefinition def, NutsSession session, JavaExecutorOptions joptions, PrintStream out) {
            super(session);
            this.def = def;
            this.joptions = joptions;
            this.out = out;
        }

        @Override
        public void dryExec() {
            out.print("[dry] ==[exec]== ");
            out.printf("[dry] ==embedded-java== ```option +cp``` %s ######%s###### %s%n"
                    , String.join(":", joptions.getClassPath())
                    , joptions.getMainClass()
                    , String.join(":", joptions.getApp())
            );
        }


        @Override
        public int exec() {
            ClassLoader classLoader = null;
            Throwable th = null;
            try {
                classLoader = ((DefaultNutsWorkspaceExtensionManager) getSession().getWorkspace().extensions()).getNutsURLClassLoader(
                        def.getId().toString(),
                        CoreIOUtils.toURL(joptions.getClassPath().toArray(new String[0])),
                        joptions.getNutsPath().stream().map(x->getSession().getWorkspace().id().parser().parse(x)).toArray(NutsId[]::new),
                        null//getSession().getWorkspace().config().getBootClassLoader()
                );
                Class<?> cls = Class.forName(joptions.getMainClass(), true, classLoader);
                new ClassloaderAwareRunnableImpl2(def.getId(), classLoader, cls, getSession(), joptions).runAndWaitFor();
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
                    throw new NutsExecutionException(getSession().getWorkspace(), "error executing " + def.getId().getLongName() + " : " + CoreStringUtils.exceptionToString(th), th);
                }
                NutsExecutionException nex = (NutsExecutionException) th;
                if (nex.getExitCode() != 0) {
                    throw new NutsExecutionException(getSession().getWorkspace(), "error executing " + def.getId().getLongName() + " : " + CoreStringUtils.exceptionToString(th), th);
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
            if(cls.getName().equals("net.thevpc.nuts.Nuts")){
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
                Class p = cls.getSuperclass();
                while (p != null) {
                    if (
                            p.getName().equals("net.thevpc.nuts.NutsApplication")
                                    //this is the old nuts apps (version < 0.8.0)
                            || p.getName().equals("net.vpc.app.nuts.NutsApplication")
                    ) {
                        isNutsApp = true;
                        break;
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
                mainMethod.invoke(nutsApp, new Object[]{getSession(), joptions.getApp().toArray(new String[0])});
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

}
