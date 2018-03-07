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
package net.vpc.app.nuts.extensions.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.io.File;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class JavaNutsExecutorComponent implements NutsExecutorComponent {

    public static final Logger log = Logger.getLogger(JavaNutsExecutorComponent.class.getName());
    public static final NutsId ID = CoreNutsUtils.parseNutsId("java");

    @Override
    public NutsId getId() {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsFile nutsFile) {
        if (nutsFile != null) {
            if ("jar".equals(nutsFile.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public int exec(NutsExecutionContext executionContext) {
        NutsFile nutMainFile = executionContext.getNutsFile();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        String[][] envAndApp0 = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getExecArgs());
        String[][] envAndApp = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getArgs());

        List<String> env = new ArrayList<>();
        env.addAll(Arrays.asList(envAndApp0[0]));
        env.addAll(Arrays.asList(envAndApp[0]));

        List<String> app = new ArrayList<>();
        app.addAll(Arrays.asList(envAndApp0[1]));
        app.addAll(Arrays.asList(envAndApp[1]));

        Properties runnerProps = new Properties();
        if (executionContext.getExecutorDescriptor() != null) {
            runnerProps = (Properties) CoreCollectionUtils.mergeMaps(executionContext.getExecutorDescriptor().getProperties(), runnerProps);
        }
        for (String k : env) {
            String[] strings = CoreNutsUtils.splitNameAndValue(k);
            if (strings != null) {
                runnerProps.put(strings[0], strings[1]);
            } else {
                runnerProps.put(k, "");
            }
        }

        if (executionContext.getEnv() != null) {
            runnerProps = (Properties) CoreCollectionUtils.mergeMaps(executionContext.getEnv(), runnerProps);
        }
        if (runnerProps == null) {
            runnerProps = new Properties();
        }

        List<String> jvmArgs = new ArrayList<String>();
        String javaVersion = null;//runnerProps.getProperty("java.version");
        String mainClass = null;
        boolean showCommand = false;
        boolean jar = false;
        List<String> classPath = new ArrayList<>();
        for (Map.Entry<Object, Object> e : runnerProps.entrySet()) {
            String k = (String) e.getKey();
            String value = (String) e.getValue();
            if (k.startsWith("-J")) {
                jvmArgs.add(k.substring(2) + (value.isEmpty() ? "" : ("=" + value)));
            } else if (k.startsWith("-D") || k.startsWith("-Xmx") || k.startsWith("-Xms")) {
                jvmArgs.add(k + "=" + value);
            } else if (k.equals("-java-version") || k.equals("java-version")) {
                javaVersion = "java#" + value;
            } else if (k.equals("-class-path") || k.equals("-cp") || k.equals("-classpath") || k.equals("class-path")) {
                classPath.add(value);
            } else if (k.equals("-main-class")) {
                mainClass = value;
            } else if (k.equals("-show-command")) {
                showCommand = true;
            } else if (k.equals("-jar")) {
                jar = true;
            } else {
                if (k.startsWith("-")) {
                    executionContext.getTerminal().getErr().printf("Ignored env param %s%s\n", k, (value == null ? "" : ("=" + value)));
                }
            }
        }

        if (CoreStringUtils.isEmpty(javaVersion)) {
            javaVersion = "java";
        }

        List<NutsFile> nutsFiles = new ArrayList<>();
        NutsDescriptor descriptor = nutMainFile.getDescriptor();
        descriptor = executionContext.getWorkspace().resolveEffectiveDescriptor(descriptor, executionContext.getSession());
        for (NutsDependency d : descriptor.getDependencies()) {
            nutsFiles.addAll(
                    Arrays.asList(executionContext.getWorkspace().fetchDependencies(
                            new NutsDependencySearch(d.toId())
                                    .setIncludeMain(true)
                                    .setScope(NutsDependencyScope.RUN),
                            executionContext.getSession().copy().setTransitive(true)))
            );
        }
        List<String> args = new ArrayList<String>();
        args.add("${" + javaVersion + "}");
        args.addAll(jvmArgs);
        if (mainClass == null) {
            File file = nutMainFile.getFile();
            if (file != null) {
                List<String> classes = CorePlatformUtils.resolveMainClasses(file);
                mainClass = CoreStringUtils.join(":", classes);
            }
        }

        if (jar) {
            if (mainClass != null) {
                executionContext.getTerminal().getErr().printf("Ignored main-class=%s. running jar!\n", mainClass);
            }
            if (!classPath.isEmpty()) {
                executionContext.getTerminal().getErr().printf("Ignored class-path=%s. running jar!\n", classPath);
            }
            args.add("-jar");
            args.add(nutMainFile.getFile().getPath());
        } else {
            if (mainClass == null) {
                throw new NutsIllegalArgumentException("Missing Main-Class in Manifest for " + nutMainFile.getId());
            }
            args.add("-classpath");
            StringBuilder sb = new StringBuilder();
            sb.append(nutMainFile.getFile().getPath());
            for (NutsFile nutsFile : nutsFiles) {
                if (nutsFile.getFile() != null) {
                    sb.append(File.pathSeparatorChar);
                    sb.append(nutsFile.getFile().getPath());
                }
            }
            for (String cp : classPath) {
                sb.append(File.pathSeparatorChar);
                sb.append(cp);
            }
            args.add(sb.toString());
            if (mainClass.contains(":")) {
                List<String> possibleClasses = CoreStringUtils.split(mainClass, ":");
                switch (possibleClasses.size()) {
                    case 0:
                        throw new NutsIllegalArgumentException("Missing Main-Class in Manifest for " + nutMainFile.getId());
                    case 1:
                        args.add(mainClass);
                        break;
                    default:
                        while (true) {
                            executionContext.getTerminal().getOut().printf("Multiple runnable classes detected  - actually [[%s]] . Select one :\n", possibleClasses.size());
                            for (int i = 0; i < possibleClasses.size(); i++) {
                                executionContext.getTerminal().getOut().printf("==[%s]== [[%s]]\n", (i + 1), possibleClasses.get(i));
                            }
                            String line = executionContext.getTerminal().readLine("Enter class # or name to run it. Type 'cancel' to cancel : ");
                            if (line != null) {
                                if (line.equals("cancel")) {
                                    return -1;
                                }
                                if (CoreStringUtils.isInt(line)) {
                                    int i = Integer.parseInt(line);
                                    if (i >= 1 && i <= possibleClasses.size()) {
                                        args.add(possibleClasses.get(i - 1));
                                        break;
                                    }
                                } else {
                                    for (String possibleClass : possibleClasses) {
                                        if (possibleClass.equals(line)) {
                                            args.add(possibleClass);
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                        break;
                }
            } else {
                args.add(mainClass);
            }
        }
        args.addAll(app);

        return CoreIOUtils.execAndWait(nutMainFile, executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecProperties(),
                args.toArray(new String[args.size()]),
                null, null, executionContext.getTerminal(), showCommand
        );

    }

}
