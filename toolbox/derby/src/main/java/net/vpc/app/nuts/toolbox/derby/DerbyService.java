/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2019 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.toolbox.derby;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.NutsStoreLocation;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class DerbyService {

    NutsApplicationContext appContext;
    DerbyOptions options;

    public DerbyService(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    private Path download(String id, Path folder) {
        final NutsId iid = appContext.getWorkspace().format().id().parse(id);
//        Path downloadBaseFolder = folder//.resolve(iid.getVersion().getValue());
        Path targetFile = folder.resolve(iid.getName() + ".jar");
        if (!Files.exists(targetFile)) {
            appContext.getWorkspace().fetch().location(targetFile).id(id).getResultPath();
            if (appContext.session().isVerbose()) {
                appContext.getSession().getTerminal().out().println("downloading " + id + " to " + targetFile);
            }
        } else {
            if (appContext.session().isVerbose()) {
                appContext.getSession().getTerminal().out().println("using " + id + " form " + targetFile);
            }
        }
        return targetFile;
    }

    void exec(DerbyOptions options) {
        List<String> command = new ArrayList<>();
        List<String> executorOptions = new ArrayList<>();
        NutsWorkspace ws = appContext.getWorkspace();
        String currentDerbyVersion = options.derbyVersion;
        if (currentDerbyVersion == null) {
            NutsId best = ws.search().addId("org.apache.derby:derbynet").duplicates(false).latest().getResultIds().singleton();
            currentDerbyVersion = best.getVersion().toString();
        }

        Path derbyDataHome = null;
        if (options.derbyDataHomeReplace != null) {
            derbyDataHome = appContext.getVarFolder();
        } else {
            if (options.derbyDataHomeRoot != null) {
                derbyDataHome = ws.io().path(getAbsoluteFile(options.derbyDataHomeRoot, appContext.getVarFolder().toString()))
                        .resolve("derby-db");
            } else {
                derbyDataHome = appContext.getVarFolder().resolve("derby-db");
            }
        }
        Path derbyDataHomeRoot = derbyDataHome.getParent();
        try {
            Files.createDirectories(derbyDataHomeRoot);
        } catch (IOException ex) {
            throw new NutsExecutionException(ws, 1);
        }
        Path derbyBinHome = ws.config().getStoreLocation(appContext.getAppId(), NutsStoreLocation.PROGRAMS).resolve(currentDerbyVersion);
        Path derbyLibHome = derbyBinHome.resolve("lib");
        Path derby = download("org.apache.derby:derby#" + currentDerbyVersion, derbyLibHome);
        Path derbynet = download("org.apache.derby:derbynet#" + currentDerbyVersion, derbyLibHome);
        Path derbyoptionaltools = download("org.apache.derby:derbyoptionaltools#" + currentDerbyVersion, derbyLibHome);
        Path derbyclient = download("org.apache.derby:derbyclient#" + currentDerbyVersion, derbyLibHome);
        Path derbytools = download("org.apache.derby:derbytools#" + currentDerbyVersion, derbyLibHome);
        Path policy = derbyBinHome.resolve("derby.policy");
        if (!Files.exists(policy)) {
            try {
                String permissions = net.vpc.common.io.IOUtils.loadString(DerbyMain.class.getResourceAsStream("policy-file.policy"))
                        .replace("${{DB_PATH}}", derbyDataHomeRoot.toString())
                        ;
                Files.write(policy, permissions.getBytes());
            } catch (IOException ex) {
                throw new NutsExecutionException(ws, 1);
            }
        }
        //use named jar because derby does test upon jar names at runtime (what a shame !!!)
        command.add("org.apache.derby:derbytools#" + currentDerbyVersion);
        //derby-db could not be created due to a security exception: java.security.AccessControlException: access denied ("java.io.FilePermission"
        executorOptions.add("-Djava.security.manager");
        executorOptions.add("-Djava.security.policy=" + policy.toString());
        executorOptions.add(
                "--classpath=" + derby + ":" + derbynet + ":" + derbyclient + ":" + derbytools + ":" + derbyoptionaltools
        );
        if (appContext.session().isVerbose()) {
            executorOptions.add("--show-command");
        }
        executorOptions.add("--main-class=org.apache.derby.drda.NetworkServerControl");
        executorOptions.add("-Dderby.system.home=" + derbyDataHome.toString());

        if (options.host != null) {
            command.add("-h");
            command.add(options.host);
        }
        if (options.port != -1) {
            command.add("-p");
            command.add(String.valueOf(options.port));
        }
        if (options.sslmode != null) {
            command.add("-ssl");
            command.add(String.valueOf(options.sslmode));
        }
        command.add(options.cmd.toString());
        if (options.extraArg != null) {
            command.add(options.extraArg);
        }

        ws
                .exec()
                .executorOptions(executorOptions)
                .command(command)
                .directory(derbyBinHome.toString())
                .failFast()
                .run().getResult();
    }

    /**
     * should promote this to FileUtils !!
     *
     * @param path
     * @param cwd
     * @return
     */
    public static String getAbsoluteFile(String path, String cwd) {
        if (new File(path).isAbsolute()) {
            return path;
        }
        if (cwd == null) {
            cwd = System.getProperty("user.dir");
        }
        switch (path) {
            case "~":
                return System.getProperty("user.home");
            case ".": {
                File file = new File(cwd);
                try {
                    return file.getCanonicalPath();
                } catch (IOException ex) {
                    return file.getAbsolutePath();
                }
            }
            case "..": {
                File file = new File(cwd, "..");
                try {
                    return file.getCanonicalPath();
                } catch (IOException ex) {
                    return file.getAbsolutePath();
                }
            }
        }
        int j = -1;
        char[] chars = path.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '/' || chars[i] == '\\') {
                j = i;
                break;
            }
        }
        if (j > 0) {
            switch (path.substring(0, j)) {
                case "~":
                    String e = path.substring(j + 1);
                    if (e.isEmpty()) {
                        return System.getProperty("user.home");
                    }
                    File file = new File(System.getProperty("user.home"), e);
                    try {
                        return file.getCanonicalPath();
                    } catch (IOException ex) {
                        return file.getAbsolutePath();
                    }
            }
        }
        File file = new File(cwd, path);
        try {
            return file.getCanonicalPath();
        } catch (IOException ex) {
            return file.getAbsolutePath();
        }
    }

}
