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
package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.core.util.io.CoreIOUtils;
import net.vpc.app.nuts.core.util.common.CoreStringUtils;
import net.vpc.app.nuts.core.util.common.CoreCommonUtils;
import net.vpc.app.nuts.core.util.common.StringKeyValueList;
import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;

import java.io.File;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;
import net.vpc.app.nuts.core.DefaultNutsWorkspaceExtensionManager;

/**
 * Created by vpc on 1/7/17.
 */
@NutsSingleton
public class JavaNutsExecutorComponent implements NutsExecutorComponent {

    public static final Logger LOG = Logger.getLogger(JavaNutsExecutorComponent.class.getName());
    public static final NutsId ID = CoreNutsUtils.parseNutsId("net.vpc.app.nuts.exec:exec-java");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsDefinition nutsDefinition) {
        if (nutsDefinition != null) {
            if ("jar".equals(nutsDefinition.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public void exec(NutsExecutionContext executionContext) {
        NutsDefinition def = executionContext.getDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        final NutsWorkspace ws = executionContext.getWorkspace();
        Path contentFile = def.getPath();
        final JavaExecutorOptions joptions = new JavaExecutorOptions(
                def,
                executionContext.isTemporary(),
                executionContext.getArguments(),
                executionContext.getExecutorOptions(),
                CoreStringUtils.isBlank(executionContext.getCwd()) ? System.getProperty("user.dir") : executionContext.getCwd(),
                executionContext.getSession());
        switch (executionContext.getExecutionType()) {
            case EMBEDDED: {
                ClassLoader classLoader = null;
                Throwable th = null;
                try {
                    classLoader = ((DefaultNutsWorkspaceExtensionManager) ws.extensions()).getNutsURLClassLoader(
                            CoreIOUtils.toURL(joptions.getClassPath().toArray(new String[0])),
                            ws.config().getBootClassLoader()
                    );
                    Class<?> cls = Class.forName(joptions.getMainClass(), true, classLoader);
                    new ClassloaderAwareRunnableImpl2(classLoader, cls, ws, joptions).runAndWaitFor();
                    return;
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
                        th = new NutsExecutionException(ws, "Error Executing " + def.getId().getLongName() + " : " + th.getMessage(), th);
                    }
                    NutsExecutionException nex = (NutsExecutionException) th;
                    if (nex.getExitCode() != 0) {
                        throw new NutsExecutionException(ws, "Error Executing " + def.getId().getLongName() + " : " + th.getMessage(), th);
                    }
                }
                break;
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
                String bootArgumentsString = ws.config().options()
                        .format().exported().compact().getBootCommandLine();
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
                // fix infinite recusion
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
                    xargs.add(ws.id().value(def.getId()).format());

                    args.add("-jar");
                    args.add(contentFile.toString());
                } else {
                    xargs.add("--nuts-path");
                    xargs.add(CoreStringUtils.join(";", joptions.getNutsPath()));
                    xargs.add(joptions.getMainClass());

                    args.add("-classpath");
                    args.add(CoreStringUtils.join(File.pathSeparator, joptions.getClassPath()));
                    args.add(joptions.getMainClass());
                }
                xargs.addAll(joptions.getApp());
                args.addAll(joptions.getApp());
                if (joptions.isShowCommand() || CoreCommonUtils.getSysBoolNutsProperty("show-command", false)) {
                    PrintStream out = executionContext.getSession().out();
                    out.println("==[nuts-exec]== ");
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
                CoreIOUtils.execAndWait(def, 
                        executionContext.getSession(), 
                        executionContext.getExecutorProperties(),
                        args.toArray(new String[0]),
                        osEnv, directory, joptions.isShowCommand(), true
                );
            }
        }
    }

    static class ClassloaderAwareRunnableImpl2 extends ClassloaderAwareRunnable {

        private final Class<?> cls;
        private final NutsWorkspace ws;
        private final JavaExecutorOptions joptions;

        public ClassloaderAwareRunnableImpl2(ClassLoader classLoader, Class<?> cls, NutsWorkspace ws, JavaExecutorOptions joptions) {
            super(classLoader);
            this.cls = cls;
            this.ws = ws;
            this.joptions = joptions;
        }

        @Override
        public Object runWithContext() throws Throwable {
            boolean isNutsApp = false;
            Method mainMethod = null;
            Object nutsApp = null;
            try {
                mainMethod = cls.getMethod("run", NutsWorkspace.class, String[].class);
                mainMethod.setAccessible(true);
                Class p = cls.getSuperclass();
                while (p != null) {
                    if (p.getName().equals("net.vpc.app.nuts.NutsApplication")) {
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
                mainMethod.invoke(nutsApp, new Object[]{ws, joptions.getApp().toArray(new String[0])});
            } else {
                //NutsWorkspace
                System.setProperty("nuts.boot.args", ws.config().options()
                        .format().exported().compact().getBootCommandLine()
                );
                mainMethod = cls.getMethod("main", String[].class);
                List<String> nargs = new ArrayList<>();
                nargs.addAll(joptions.getApp());
                mainMethod.invoke(null, new Object[]{joptions.getApp().toArray(new String[0])});
            }
            return null;
        }

    }

}
