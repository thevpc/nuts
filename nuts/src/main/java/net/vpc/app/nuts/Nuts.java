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
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Created by vpc on 1/5/17.
 */
public class Nuts {

    private static final Logger log = Logger.getLogger(Nuts.class.getName());

    public static void main(String[] args) {
        try {
            System.exit(uncheckedMain(args));
        } catch (Exception ex) {
            boolean showErrorClass = false;
            int errorCode = 204;
            //inherit error code from exception
            if (ex instanceof NutsExecutionException) {
                errorCode = ((NutsExecutionException) ex).getExitCode();
            }
            boolean showTrace = false;
            for (int i = 0; i < args.length; i++) {
                if (args[i].startsWith("-")) {
                    if (args[i].equals("--verbose")) {
                        showTrace = true;
                        showErrorClass = true;
                    }
                } else {
                    break;
                }
            }
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

    public static NutsWorkspace openInheritedWorkspace(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot=null;
        if (args.length > 0 && args[0].startsWith("--nuts-boot-args=")) {
            boot = new NutsBootWorkspace(NutsMinimalCommandLine.parseCommandLine(args[0].substring("--nuts-boot-args=".length())));
            boot.getOptions().setApplicationArguments(Arrays.copyOfRange(args,1,args.length));
        }else{
            String d = System.getProperty("nuts-boot-args");
            if(d!=null){
                boot = new NutsBootWorkspace(NutsMinimalCommandLine.parseCommandLine(d));
                boot.getOptions().setApplicationArguments(args);
            }else {
                NutsWorkspaceOptions t = new NutsWorkspaceOptions();
                t.setApplicationArguments(args);
                boot = new NutsBootWorkspace(t);
            }
        }
        if (boot.isRequiredNewProcess()) {
            throw new IllegalArgumentException("Unable to open a distinct version " + boot.getOptions().getRequiredBootVersion());
        }
        boot.getOptions().setCreationTime(startTime);
        return openWorkspace(boot.getOptions().setCreateIfNotFound(true));
    }

    public static NutsWorkspace openWorkspace(String[] args) {
        long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        if (boot.isRequiredNewProcess()) {
            throw new IllegalArgumentException("Unable to open a distinct version " + boot.getOptions().getRequiredBootVersion());
        }
        if (boot.getOptions().getCreationTime() == 0) {
            boot.getOptions().setCreationTime(startTime);
        }
        return openWorkspace(boot.getOptions().setCreateIfNotFound(true));
    }

    public static NutsWorkspace openWorkspace() {
        return openWorkspace((NutsWorkspaceOptions) null);
    }

    public static NutsWorkspace openWorkspace(String workspace) {
        return openWorkspace(new NutsWorkspaceOptions().setWorkspace(workspace));
    }

    public static NutsWorkspace openWorkspace(NutsWorkspaceOptions options) {
        if (options == null) {
            options = new NutsWorkspaceOptions();
        }
        if (options.getCreationTime() == 0) {
            options.setCreationTime(System.currentTimeMillis());
        }
        return new NutsBootWorkspace(options).openWorkspace();
    }


    public static int uncheckedMain(String[] args) throws Exception {
        //long startTime = System.currentTimeMillis();
        NutsBootWorkspace boot = new NutsBootWorkspace(args);
        return boot.run();
    }


    public static String getDefaultNutsHome() {
        if (Boolean.getBoolean("nuts.debug.emulate-windows")) {
            return System.getProperty("user.home") + "\\AppData\\Roaming\\nuts".replace("\\", File.separator);
        }
        switch (NutsUtils.getPlatformOsFamily()) {
            case "windows":
                return System.getProperty("user.home") + "\\AppData\\Roaming\\nuts";
            default:
                return System.getProperty("user.home") + "/.nuts";
        }
    }

    public static String getDefaultHomeFolder(StoreFolder folderType, String home,StoreLocationLayout storeLocationLayout) {
        if(folderType==null){
            folderType= StoreFolder.CONFIG;
        }
        boolean absolute = false;
        if (home == null || home.trim().isEmpty()) {
            home = "";
        } else {
            if (new File(home).isAbsolute()) {
                absolute = true;
            }
        }

        if (storeLocationLayout == null || storeLocationLayout == StoreLocationLayout.SYSTEM) {
            if ("windows".equals(NutsUtils.getPlatformOsFamily())) {
                storeLocationLayout = StoreLocationLayout.WINDOWS;
            } else {
                storeLocationLayout = StoreLocationLayout.LINUX;
            }
        }
        switch (folderType) {
            case LOGS:
            case VAR:
            case CONFIG:
            case PROGRAMS:
            case LIB:
                {
                if (absolute) {
                    return syspath(home);
                } else if (home.isEmpty()) {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + syspath("/AppData/Roaming/nuts");
                        case LINUX:
                            return System.getProperty("user.home") + syspath("/.nuts");
                    }
                } else {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + syspath("/AppData/Roaming/nuts/boot/" + home);
                        case LINUX:
                            return System.getProperty("user.home") + syspath("/.nuts/boot/" + home);
                    }
                }
                break;
            }
            case CACHE: {
                if (absolute) {
                    return syspath(home);
                } else if (home.isEmpty()) {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + syspath("/AppData/Local/nuts");
                        case LINUX:
                            return System.getProperty("user.home") + syspath("/.cache/nuts");
                    }
                } else {
                    switch (storeLocationLayout) {
                        case WINDOWS:
                            return System.getProperty("user.home") + syspath("/AppData/Local/nuts/boot/" + home);
                        case LINUX:
                            return System.getProperty("user.home") + syspath("/.cache/nuts/boot/" + home);
                    }
                }
                break;
            }
            case TEMP: {
                return System.getProperty("java.io.tmpdir") + ("/"+System.getProperty("user.name")+"-tmp/nuts".replace("/", File.separator));
            }
        }
        throw new NutsIllegalArgumentException("Unsupported " + storeLocationLayout);
    }

    public static String getDefaultWorkspaceFolder(String path,StoreFolder folderType, String home,String workspace,StoreLocationStrategy storeLocationStrategy) {
        if (path != null && !path.trim().isEmpty() && NutsUtils.isAbsolutePath(path)) {
            return path;
        }
        if (storeLocationStrategy == null) {
            storeLocationStrategy = StoreLocationStrategy.SYSTEM;
        }
        if (home == null || home.trim().isEmpty()) {
            throw new NutsIllegalArgumentException("Missing Home");
        }
        if (workspace == null || workspace.isEmpty()) {
            workspace = NutsConstants.DEFAULT_WORKSPACE_NAME;
        }
        if (workspace.startsWith(home)) {
            String w = workspace.substring(home.length());
            while (w.startsWith("/") || w.startsWith("\\")) {
                w = w.substring(1);
            }
            workspace = w;
        }
        String workspaceName=new File(workspace).getName();
        if (NutsUtils.isAbsolutePath(workspace)) {
            if (folderType == null) {
                //root
                return workspace;
            }
            String name = folderType.name().toLowerCase();
            switch (folderType) {
                case LOGS:
                case VAR:
                case CONFIG:
                case PROGRAMS:
                case LIB:
                    {
                    return workspace + File.separator + name;
                }
                case TEMP:
                case CACHE: {
                    if (storeLocationStrategy == StoreLocationStrategy.BUNDLE) {
                        return workspace + File.separator + name;
                    }
                    return home + syspath("/" + workspaceName + "/" + name);
                }
            }
        } else {
            if (folderType == null) {
                return home;
            }
            String name = folderType.name().toLowerCase();
            if (storeLocationStrategy == StoreLocationStrategy.BUNDLE) {
                return home + syspath("/" + workspaceName + "/" + name);
            }
            return home + syspath("/" + workspaceName + "/" + name);
        }
        throw new NutsIllegalArgumentException("Unsupported");
    }

    public static String syspath(String s) {
        return s.replace("/", File.separator).replace("\\", File.separator);
    }
}
