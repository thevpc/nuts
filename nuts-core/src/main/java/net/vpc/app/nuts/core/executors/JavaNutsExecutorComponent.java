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
package net.vpc.app.nuts.core.executors;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.*;
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
        NutsDefinition nutsMainDef = executionContext.getNutsDefinition();//executionContext.getWorkspace().fetch(.getId().toString(), true, false);
        String contentFile = nutsMainDef.getContent().getFile();
        JavaExecutorOptions joptions = new JavaExecutorOptions(
                nutsMainDef, executionContext.getArgs(),
                executionContext.getExecutorOptions(),
                executionContext.getCwd(),
                executionContext.getWorkspace(),
                executionContext.getSession());

        StringKeyValueList runnerProps = new StringKeyValueList();
        if (executionContext.getExecutorDescriptor() != null) {
            runnerProps.add((Map) executionContext.getExecutorDescriptor().getProperties());
        }

        if (executionContext.getEnv() != null) {
            runnerProps.add((Map) executionContext.getEnv());
        }

        HashMap<String, String> osEnv = new HashMap<>();
        String bootArgumentsString = executionContext.getWorkspace().getConfigManager().getOptions().getBootArgumentsString();
        if (!StringUtils.isEmpty(bootArgumentsString)) {
            osEnv.put("nuts_boot_args", bootArgumentsString);
            joptions.getJvmArgs().add("-Dnuts.boot.args=" + bootArgumentsString);
        }

        List<String> xargs = new ArrayList<String>();
        List<String> args = new ArrayList<String>();

        xargs.add(joptions.getJavaHome());
        xargs.addAll(joptions.getJvmArgs());

        args.add(joptions.getJavaHome());
        args.addAll(joptions.getJvmArgs());

        String Dnuts_boot_args_value = executionContext.getWorkspace().getConfigManager().getOptions().getBootArguments() == null ? null :
                NutsMinimalCommandLine.escapeArguments(executionContext.getWorkspace().getConfigManager().getOptions().getBootArguments());
        if (!StringUtils.isEmpty(Dnuts_boot_args_value)) {
            String Dnuts_boot_args = "-Dnuts-boot-args=" + Dnuts_boot_args_value;
            xargs.add(Dnuts_boot_args);
            args.add(Dnuts_boot_args);
        }
        if (joptions.isJar()) {
            xargs.add("-jar");
            xargs.add(executionContext.getWorkspace().getFormatManager().createIdFormat().format(nutsMainDef.getId()));

            args.add("-jar");
            args.add(contentFile);
        } else {
            xargs.add("--nuts-path");
            xargs.add(StringUtils.join(File.pathSeparator, joptions.getNutsPath()));
            xargs.add(joptions.getMainClass());

            args.add("-classpath");
            args.add(StringUtils.join(File.pathSeparator, joptions.getClassPath()));
            args.add(joptions.getMainClass());
        }
        xargs.addAll(joptions.getApp());
        args.addAll(joptions.getApp());
        if (joptions.isShowCommand()) {
            PrintStream out = executionContext.getTerminal().getOut();
//            out.println("==[nuts-exec]== " + NutsArgumentsParser.escapeArguments(xargs.toArray(new String[0])));
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

        File directory = StringUtils.isEmpty(joptions.getDir()) ? null : new File(executionContext.getWorkspace().getIOManager().expandPath(joptions.getDir()));
        return CoreIOUtils.execAndWait(nutsMainDef, executionContext.getWorkspace(), executionContext.getSession(), executionContext.getExecutorProperties(),
                args.toArray(new String[0]),
                osEnv, directory
                , executionContext.getTerminal(), joptions.isShowCommand(), executionContext.isFailFast()
        );

    }


}
