/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.*;
import net.vpc.common.strings.StringUtils;

import java.io.File;
import java.io.PrintStream;
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
    public int getSupportLevel(NutsDefinition nutsDefinition) {
        if (nutsDefinition != null) {
            if ("jar".equals(nutsDefinition.getDescriptor().getPackaging())) {
                return DEFAULT_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    @Override
    public int exec(NutsExecutionContext executionContext) {
        NutsIdFormat nutsIdFormat = executionContext.getWorkspace().getFormatManager().createIdFormat().setOmitNamespace(true);
        NutsDefinition nutMainFile = executionContext.getNutsDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);

        List<String> app = new ArrayList<>(Arrays.asList(executionContext.getArgs()));

        StringKeyValueList runnerProps = new StringKeyValueList();
        if (executionContext.getExecutorDescriptor() != null) {
            runnerProps.add((Map) executionContext.getExecutorDescriptor().getProperties());
        }


        if (executionContext.getEnv() != null) {
            runnerProps.add((Map) executionContext.getEnv());
        }

        List<String> jvmArgs = new ArrayList<String>();

        HashMap<String, String> osEnv = new HashMap<>();
        String bootArgumentsString = executionContext.getWorkspace().getConfigManager().getOptions().getBootArgumentsString();
        if(!StringUtils.isEmpty(bootArgumentsString)) {
            osEnv.put("nuts_boot_args", bootArgumentsString);
            jvmArgs.add("-Dnuts.boot.args=" + bootArgumentsString);
        }
        String javaVersion = null;//runnerProps.getProperty("java.version");
        String javaHome = null;//runnerProps.getProperty("java.version");
        String mainClass = null;
        boolean mainClassApp = false;
        String dir = executionContext.getCwd();
        boolean showCommand = false;
        boolean jar = false;
        List<String> classPath = new ArrayList<>();
        String[] execArgs = executionContext.getExecutorOptions();
        //will accept all -- and - based options!
        for (int i = 0; i < execArgs.length; i++) {
            String arg = execArgs[i];
            if (arg.equals("--java-version") || arg.equals("-java-version")) {
                i++;
                javaVersion = execArgs[i];
            } else if (arg.startsWith("--java-version=") || arg.startsWith("-java-version=")) {
                javaVersion = execArgs[i].substring(arg.indexOf('=') + 1);

            } else if (arg.equals("--java-home") || arg.equals("-java-home")) {
                i++;
                javaHome = execArgs[i];
            } else if (
                    arg.startsWith("--java-home=")
                            || arg.startsWith("-java-home=")
            ) {
                javaHome = execArgs[i].substring(arg.indexOf('=') + 1);

            } else if (
                    arg.equals("--class-path") || arg.equals("--classpath") || arg.equals("--cp")
                            || arg.equals("-class-path") || arg.equals("-classpath") || arg.equals("-cp")
            ) {
                i++;
                addToCp(classPath, execArgs[i]);
            } else if (
                    arg.startsWith("--class-path=") || arg.startsWith("--classpath=") || arg.startsWith("--cp=")
                            || arg.startsWith("-class-path=") || arg.startsWith("-classpath=") || arg.startsWith("-cp=")
            ) {
                addToCp(classPath, execArgs[i].substring(arg.indexOf('=') + 1));

            } else if (arg.equals("--nuts-path") || arg.equals("--nutspath") || arg.equals("--np")) {
                i++;
                npToCp(executionContext, classPath, execArgs[i]);
            } else if (arg.startsWith("--nuts-path=") || arg.startsWith("--nutspath=") || arg.startsWith("--np=")) {
                npToCp(executionContext, classPath, execArgs[i].substring(arg.indexOf('=') + 1));

            } else if (arg.equals("--jar") || arg.equals("-jar")) {
                jar = true;
            } else if (arg.equals("--main-class") || arg.equals("-main-class") || arg.equals("--class") || arg.equals("-class")) {
                i++;
                mainClass = execArgs[i];
            } else if (
                    arg.startsWith("--main-class=") || arg.startsWith("-main-class=") || arg.startsWith("--class=")
                            || arg.startsWith("-class=")
            ) {
                mainClass = execArgs[i].substring(arg.indexOf('=') + 1);
            } else if (arg.equals("--show-command") || arg.equals("-show-command")) {
                showCommand = true;
            } else if (arg.equals("--dir") || arg.equals("-dir")) {
                i++;
                dir = execArgs[i];
            } else if (arg.startsWith("--dir=") || arg.startsWith("-dir=")) {
                dir = execArgs[i].substring(arg.indexOf('=') + 1);
            } else {
                jvmArgs.add(arg);
            }
        }
        if (javaHome == null) {
            if (!StringUtils.isEmpty(javaVersion)) {
                javaHome = "${java#" + javaVersion + "}";
            } else {
                javaHome = "${java}";
            }
        } else {
            javaHome = javaHome + "/bin/java";
        }

        List<NutsDefinition> nutsDefinitions = new ArrayList<>();
        NutsDescriptor descriptor = nutMainFile.getDescriptor();
        descriptor = executionContext.getWorkspace().resolveEffectiveDescriptor(descriptor, executionContext.getSession());
        nutsDefinitions.addAll(
                executionContext.getWorkspace()
                        .createQuery().addId(descriptor.getId())
                        .setSession(executionContext.getSession().copy().setTransitive(true))
                        .addScope(NutsDependencyScope.PROFILE_RUN)
                        .setIncludeOptional(false)
                        .includeDependencies()
                        .fetch()

        );
        List<String> xargs = new ArrayList<String>();
        List<String> args = new ArrayList<String>();
        args.add(javaHome);
        xargs.add(javaHome);
        args.addAll(jvmArgs);
        xargs.addAll(jvmArgs);
        String Dnuts_boot_args_value = NutsMinimalCommandLine.escapeArguments(executionContext.getWorkspace().getConfigManager().getOptions().getBootArguments());
        if(!StringUtils.isEmpty(Dnuts_boot_args_value)) {
            String Dnuts_boot_args = "-Dnuts-boot-args=" + Dnuts_boot_args_value;
            args.add(Dnuts_boot_args);
            xargs.add(Dnuts_boot_args);
        }
        if (jar) {
            if (mainClass != null) {
                executionContext.getTerminal().getFormattedErr().printf("Ignored main-class=%s. running jar!\n", mainClass);
            }
            if (!classPath.isEmpty()) {
                executionContext.getTerminal().getFormattedErr().printf("Ignored class-path=%s. running jar!\n", classPath);
            }
            args.add("-jar");
            args.add(nutMainFile.getFile());
            xargs.add("-jar");
            xargs.add(executionContext.getWorkspace().getFormatManager().createIdFormat().format(nutMainFile.getId()));
        } else {
            if (mainClass == null) {
                File file = CoreIOUtils.fileByPath(nutMainFile.getFile());
                if (file != null) {
                    //check manifest!
                    NutsExecutionEntry[] classes = CorePlatformUtils.parseMainClasses(file);
                    if (classes.length > 0) {
                        mainClass = StringUtils.join(":", classes, NutsExecutionEntry::getName);
                    }
                }
            }
            if (mainClass == null) {
                throw new NutsIllegalArgumentException("Missing Main Class for " + nutMainFile.getId());
            }
            xargs.add("--nuts-path");
            args.add("-classpath");
            StringBuilder xsb = new StringBuilder();
            StringBuilder sb = new StringBuilder();
            xsb.append(nutsIdFormat.format(nutMainFile.getId()));
            sb.append(nutMainFile.getFile());
            for (NutsDefinition nutsDefinition : nutsDefinitions) {
                if (nutsDefinition.getFile() != null) {
                    sb.append(File.pathSeparatorChar);
                    sb.append(nutsDefinition.getFile());
                    xsb.append(";");
                    xsb.append(nutsIdFormat.format(nutsDefinition.getId()));
                }
            }
            for (String cp : classPath) {
                sb.append(File.pathSeparatorChar);
                sb.append(cp);
                xsb.append(";");
                xsb.append(cp);
            }
            args.add(sb.toString());
            xargs.add(xsb.toString());
            if (mainClass.contains(":")) {
                List<String> possibleClasses = CoreStringUtils.split(mainClass, ":");
                switch (possibleClasses.size()) {
                    case 0:
                        throw new NutsIllegalArgumentException("Missing Main-Class in Manifest for " + nutMainFile.getId());
                    case 1:
                        xargs.add(mainClass);
                        args.add(mainClass);
                        break;
                    default:
                        while (true) {
                            PrintStream out = executionContext.getTerminal().getFormattedOut();
                            out.printf("Multiple runnable classes detected  - actually [[%s]] . Select one :\n", possibleClasses.size());
                            for (int i = 0; i < possibleClasses.size(); i++) {
                                out.printf("==[%s]== [[%s]]\n", (i + 1), possibleClasses.get(i));
                            }
                            String line = executionContext.getTerminal().readLine("Enter class ==%s== or ==%s== to run it. Type @@%s@@ to cancel : ", "#", "name", "cancel");
                            if (line != null) {
                                if (line.equals("cancel")) {
                                    return -1;
                                }
                                if (CoreStringUtils.isInt(line)) {
                                    int i = Integer.parseInt(line);
                                    if (i >= 1 && i <= possibleClasses.size()) {
                                        xargs.add(possibleClasses.get(i - 1));
                                        args.add(possibleClasses.get(i - 1));
                                        break;
                                    }
                                } else {
                                    for (String possibleClass : possibleClasses) {
                                        if (possibleClass.equals(line)) {
                                            xargs.add(possibleClass);
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
                xargs.add(mainClass);
                args.add(mainClass);
            }
        }
        xargs.addAll(app);
        args.addAll(app);
        if (showCommand) {
            PrintStream out = executionContext.getTerminal().getOut();
//            out.println("==[nuts-exec]== " + NutsArgumentsParser.escapeArguments(xargs.toArray(new String[0])));
            out.println("==[nuts-exec]== ");
            for (int i = 0; i < xargs.size(); i++) {
                String xarg = xargs.get(i);
                if(i>0 && xargs.get(i-1).equals("--nuts-path")){
                    for (String s : xarg.split(";")) {
                        out.println("\t\t\t " + s);
                    }
                }else {
                    out.println("\t\t " + xarg);
                }
            }
        }

        File directory = StringUtils.isEmpty(dir) ? null : new File(executionContext.getWorkspace().getIOManager().resolvePath(dir));
        return CoreIOUtils.execAndWait(nutMainFile, executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecutorProperties(),
                args.toArray(new String[0]),
                osEnv, directory
                , executionContext.getTerminal(), showCommand, executionContext.isFailFast()
        );

    }

    private void addToCp(List<String> classPath, String value) {
        for (String n : CoreStringUtils.split(value, ":;, ")) {
            if (!StringUtils.isEmpty(n)) {
                classPath.add(n);
            }
        }
    }

    private void npToCp(NutsExecutionContext executionContext, List<String> classPath, String value) {
        NutsQuery ns = executionContext.getWorkspace().createQuery().setLatestVersions(true)
                .setSession(executionContext.getSession());
        for (String n : CoreStringUtils.split(value, ";, ")) {
            if (!StringUtils.isEmpty(n)) {
                ns.addId(n);
            }
        }
        for (NutsId nutsId : ns.find()) {
            NutsDefinition f = executionContext.getWorkspace().fetch(nutsId, executionContext.getSession());
            classPath.add(f.getFile());
        }
    }

}
