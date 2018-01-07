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
import net.vpc.app.nuts.extensions.util.CoreIOUtils;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.util.NutsUtils;
import net.vpc.app.nuts.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/7/17.
 */
public class JavaNutsExecutorComponent implements NutsExecutorComponent {

    public static final Logger log = Logger.getLogger(JavaNutsExecutorComponent.class.getName());
    public static final NutsId ID = NutsId.parse("java");

    @Override
    public NutsId getId() throws IOException {
        return ID;
    }

    @Override
    public int getSupportLevel(NutsFile nutsFile) {
        if (nutsFile != null) {
            if ("jar".equals(nutsFile.getDescriptor().getPackaging())) {
                return CORE_SUPPORT + 1;
            }
        }
        return NO_SUPPORT;
    }

    public void exec(NutsExecutionContext executionContext) throws IOException {
        NutsFile nutMainFile = executionContext.getNutsFile();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);

        String[][] envAndApp0 = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getExecArgs());
        String[][] envAndApp = CoreNutsUtils.splitEnvAndAppArgs(executionContext.getArgs());

        List<String> env = new ArrayList<>();
        env.addAll(Arrays.asList(envAndApp0[0]));
        env.addAll(Arrays.asList(envAndApp[0]));

        List<String> app = new ArrayList<>();
        app.addAll(Arrays.asList(envAndApp0[1]));
        app.addAll(Arrays.asList(envAndApp[1]));


        Properties runnerProps = null;
        if (executionContext.getExecutorDescriptor() != null) {
            runnerProps = executionContext.getExecutorDescriptor().getProperties();
        }
        if (runnerProps == null) {
            runnerProps = new Properties();
        }
        String javaVersion = runnerProps.getProperty("java.version");
        if (StringUtils.isEmpty(javaVersion)) {
            javaVersion = "java";
        } else {
            javaVersion = "java#" + javaVersion;
        }
        for (Iterator<String> iterator = env.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            if (s.startsWith("-java-version=")) {
                javaVersion = "java#" + s.substring("-java-version=".length());
                iterator.remove();
                break;
            }
        }

        List<NutsFile> nutsFiles = new ArrayList<>();
        NutsDescriptor descriptor = nutMainFile.getDescriptor();
        descriptor=executionContext.getWorkspace().fetchEffectiveDescriptor(descriptor,executionContext.getSession());
        for (NutsDependency d : descriptor.getDependencies()) {
            nutsFiles.addAll(
                    executionContext.getWorkspace().fetchWithDependencies(d.toId().toString(), true,
                            NutsUtils.EXEC_DEPENDENCIES_FILTER,
                            executionContext.getSession().copy().setTransitive(true))
            );
        }
        List<String> args = new ArrayList<String>();
        args.add("${" + javaVersion + "}");
        String mainClass = null;
        boolean execAsJar = false;
        boolean showCommand = false;
        List<String> classPath = new ArrayList<>();
        for (Iterator<String> iterator = env.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            if (s.equals("-jar")) {
                execAsJar = true;
            } else if (s.startsWith("-main-class=")) {
                mainClass = s.substring("-main-class=".length());
                iterator.remove();
            } else if (s.startsWith("-class-path=")) {
                iterator.remove();
                classPath.add(s.substring("-class-class=".length()));
            } else if (s.equals("-show-command")) {
                iterator.remove();
                showCommand=true;
            } else {
                args.add(s);
            }
        }
        for (String s : env) {
            args.add(s);
        }
        if (execAsJar) {
            args.add("-jar");
            args.add(nutMainFile.getFile().getPath());
        } else {
            args.add("-classpath");
            StringBuilder sb = new StringBuilder();
            sb.append(nutMainFile.getFile().getPath());
            for (NutsFile nutsFile : nutsFiles) {
                if(nutsFile.getFile()!=null) {
                    sb.append(File.pathSeparatorChar);
                    sb.append(nutsFile.getFile().getPath());
                }
            }
            for (String cp : classPath) {
                sb.append(File.pathSeparatorChar);
                sb.append(cp);
            }
            args.add(sb.toString());
            if (mainClass == null) {
                throw new IllegalArgumentException("Missing Main-Class in Manifest for " + nutMainFile.getId());
            }
            if (mainClass.contains(":")) {
                List<String> possibleClasses = StringUtils.split(mainClass, ":");
                if (possibleClasses.size() == 0) {
                    throw new IllegalArgumentException("Missing Main-Class in Manifest for " + nutMainFile.getId());
                } else if (possibleClasses.size() == 1) {
                    args.add(mainClass);
                } else {
                    while (true) {
                        executionContext.getTerminal().getOut().println("Multiple runnable classes detected  - actually <<<"+possibleClasses.size()+">>> . Select one :");
                        for (int i = 0; i < possibleClasses.size(); i++) {
                            executionContext.getTerminal().getOut().drawln("==[" + (i + 1) + "]== <<<" + possibleClasses.get(i) + ">>>");
                        }
                        String line=executionContext.getTerminal().readLine("Enter class # or name to run it. Type 'cancel' to cancel : ");
                        if(line!=null){
                            if(line.equals("cancel")){
                                return;
                            }
                            if(StringUtils.isInt(line)){
                                int i=Integer.parseInt(line);
                                if(i>=1 && i<=possibleClasses.size()){
                                    args.add(possibleClasses.get(i-1));
                                    break;
                                }
                            }else{
                                for (String possibleClass : possibleClasses) {
                                    if(possibleClass.equals(line)){
                                        args.add(possibleClass);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }else{
                args.add(mainClass);
            }
        }
        args.addAll(app);


        CoreIOUtils.execAndWait(nutMainFile, executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecProperties(),
                args.toArray(new String[args.size()]),
                null, null, executionContext.getTerminal(),showCommand
        );


    }

}
