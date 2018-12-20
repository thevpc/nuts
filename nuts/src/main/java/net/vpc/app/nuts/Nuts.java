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
package net.vpc.app.nuts;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class Nuts {

    private static final Logger log = Logger.getLogger(Nuts.class.getName());

//    public static void main(String[] args) {
//        for (int i = 0; i < 10000; i++) {
//            uncheckedMain(args);
//        }
//    }
    public static void main(String[] args) {
        try {
            System.exit(uncheckedMain(args));
        } catch (Exception ex) {
            int errorCode = 204;
            //inherit error code from exception
            if (ex instanceof NutsExecutionException) {
                errorCode = ((NutsExecutionException) ex).getExitCode();
            }
            boolean showTrace = false;
            boolean showErrorClass = false;
            if (ex.getClass().getName().startsWith("java.lang.")) {
                //this is a common error
                showTrace = true;
                showErrorClass = true;
            }
            String m = ex.getMessage();
            if (m == null || m.isEmpty()) {
                m = ex.toString();
            }
            if (m == null || m.isEmpty()) {
                m = ex.getClass().getName();
            }
            if (showErrorClass) {
                m = ex.toString();
            }
            System.err.println(m);
            if (showTrace) {
                ex.printStackTrace(System.err);
            }
            System.exit(errorCode);
        }
    }

    private static NutsBootWorkspace openBootWorkspace() {
        return openBootWorkspace(null);
    }

    private static NutsBootWorkspace openBootWorkspace(NutsBootOptions bootOptions) {
        return new DefaultNutsBootWorkspace(bootOptions);
    }


    public static NutsWorkspace openInheritedWorkspace(String[] args) {
        return openWorkspace(args,true);
    }

    public static NutsWorkspace openWorkspace(String[] args) {
        return openWorkspace(args,false);
    }

    private static NutsWorkspace openWorkspace(String[] args,boolean expectedNutsArgs) {
        long startTime = System.currentTimeMillis();
        NutsArguments nutsArguments = NutsArgumentsParser.parseNutsArguments(args, expectedNutsArgs);
        if (nutsArguments instanceof NewInstanceNutsArguments) {
            NewInstanceNutsArguments i = (NewInstanceNutsArguments) nutsArguments;
            throw new IllegalArgumentException("Unable to open a distinct version " + i.getBootFile() + "<>" + i.getRequiredVersion());
        }
        ConfigNutsArguments a = (ConfigNutsArguments) nutsArguments;
        NutsWorkspaceOptions workspaceCreateOptions = a.getWorkspaceCreateOptions();
        if (workspaceCreateOptions.getCreationTime() == 0) {
            workspaceCreateOptions.setCreationTime(startTime);
        }
        return openWorkspace(workspaceCreateOptions.setCreateIfNotFound(true), a.getBootOptions());
    }

    public static NutsWorkspace openWorkspace() {
        return openWorkspace(null, null);
    }

    public static NutsWorkspace openWorkspace(String workspace) {
        return openWorkspace(new NutsWorkspaceOptions().setWorkspace(workspace), null);
    }

    public static NutsWorkspace openWorkspace(NutsWorkspaceOptions options) {
        return openWorkspace(options, null);
    }

    public static NutsWorkspace openWorkspace(NutsWorkspaceOptions options, NutsBootOptions bootOptions) {
        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            options.setCreationTime(System.currentTimeMillis());
        }
        return openBootWorkspace(bootOptions).openWorkspace(options);
    }


    public static void startNewProcess(NewInstanceNutsArguments n) {
        for (int i = 0; i < 10; i++) {
            System.out.println("");
        }
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<String> cmd = new ArrayList<>();
        String jc = n.getJavaCommand();
        if(jc==null||jc.trim().isEmpty()){
            jc=NutsUtils.resolveJavaCommand(null);
        }
        cmd.add(jc);
        Collections.addAll(cmd, NutsArgumentsParser.parseCommandLine(n.getJavaOptions()));
//        cmd.add("-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005");
        cmd.add("-jar");
        cmd.add(n.getBootFile().getPath());
        //cmd.add("--verbose");
        cmd.addAll(Arrays.asList(n.getArgs()));
        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < cmd.size(); i++) {
            String s = cmd.get(i);
            if(i>0){
                sb.append(" ");
            }
            sb.append(s);
        }
        System.out.println("[EXEC] " +sb);
        try {
            new ProcessBuilder(cmd).inheritIO().start();
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to start nuts", ex);
        }
    }

    public static int uncheckedMain(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsArguments a = NutsArgumentsParser.parseNutsArguments(args, false);
        if (a instanceof NewInstanceNutsArguments) {
            startNewProcess((NewInstanceNutsArguments) a);
            return 0;
        }
        ConfigNutsArguments o = (ConfigNutsArguments) a;
        o.getWorkspaceCreateOptions().setCreationTime(startTime);
        NutsWorkspace ws = openWorkspace(o.getWorkspaceCreateOptions(), o.getBootOptions());

        String[] commandArguments = o.getArgs().toArray(new String[0]);
        switch (o.getBootCommand()){
            case VERSION:{
                PrintStream out = ws.getTerminal().getFormattedOut();
                ws.printVersion(out,null,o.getVersionOptions());
                return 0;
            }
            case HELP:{
                return ws.createExecBuilder()
                        .setCommand(NutsConstants.NUTS_SHELL, "help")
                        .addCommand(commandArguments)
                        .exec().getResult();
            }
            case LICENSE:{
                return ws.createExecBuilder()
                        .setCommand(NutsConstants.NUTS_SHELL, "help", "--license")
                        .addCommand(commandArguments)
                        .exec().getResult()
                        ;
            }
            case UPDATE:{
                if (ws.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                                .setApplyUpdates(true)
                                .setEnableMajorUpdates(true)
                                .setEnableMajorUpdates(true)
                        , null).length > 0) {
                    return 0;
                }
                return 1;
            }
            case CHECK_UPDATES:{
                if (ws.checkWorkspaceUpdates(new NutsWorkspaceUpdateOptions()
                                .setApplyUpdates(false)
                                .setEnableMajorUpdates(true)
                                .setEnableMajorUpdates(true)
                        , null).length > 0) {
                    return 0;
                }
                return 1;
            }
            case CLEAN:{
                boolean force=false;
                for (String argument : ws.getBootOptions().getBootArguments()) {
                    if("-f".equals(argument) ||"--force".equals(argument)){
                        force=true;
                    }
                }
                File f=new File(ws.getConfigManager().getWorkspaceLocation(),"cache");
                try {
                    NutsUtils.deleteAndConfirm(f,force);
                } catch (Exception e) {
                    System.err.println(e.toString());
                    return 1;
                }
                f=new File(ws.getConfigManager().getWorkspaceLocation(),"log");
                try {
                    NutsUtils.deleteAndConfirm(f,force);
                } catch (NutsUserCancelException e) {
                    System.err.println(e.getMessage());
                    return 1;
                } catch (Exception e) {
                    System.err.println(e.toString());
                    return 1;
                }
                return 0;
            }
            case RESET:{
                boolean force=false;
                for (String argument : ws.getBootOptions().getBootArguments()) {
                    if("-f".equals(argument) ||"--force".equals(argument)){
                        force=true;
                    }
                }
                File f=new File(ws.getConfigManager().getWorkspaceLocation());
                System.out.println("**************");
                System.out.println("** ATTENTION *");
                System.out.println("**************");
                System.out.println("You are about to delete all Workspace configuration files.");
                System.out.println("Are you sure this is what you want ??");
                try {
                    NutsUtils.deleteAndConfirm(f,force);
                } catch (NutsUserCancelException e) {
                    System.err.println(e.getMessage());
                    return 1;
                } catch (Exception e) {
                    System.err.println(e.toString());
                    return 1;
                }
                return 0;
            }
        }

        if (commandArguments.length == 0) {
            return ws.createExecBuilder()
                    .setCommand(NutsConstants.NUTS_SHELL, "help")
                    .exec().getResult();
        }
        return ws.createExecBuilder()
                .setCommand(commandArguments)
                .exec().getResult();
    }

    public static String getActualVersion() {
        return NutsUtils.loadURLProperties(Nuts.class.getResource("/META-INF/nuts/net.vpc.app.nuts/nuts/nuts.properties"),null).getProperty("project.version", "0.0.0");
    }

    private static void showStaticHelp() {
        String actualVersion = getActualVersion();
        String str = null;
        try {
            str = NutsUtils.readStringFromURL(Nuts.class.getResource("/net/vpc/app/nuts/NutsStaticHelp.txt"));
            System.out.println("Nuts " + actualVersion);
            System.out.println(str);
        } catch (IOException e) {
            System.err.println("Unable to load Help");
            e.printStackTrace();
        }
    }


}
