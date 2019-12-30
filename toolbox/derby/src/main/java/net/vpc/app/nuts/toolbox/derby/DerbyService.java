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
 * Copyright (C) 2016-2019 Taha BEN SALAH
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
package net.vpc.app.nuts.toolbox.derby;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

import net.vpc.app.nuts.*;

/**
 * @author vpc
 */
public class DerbyService {

    NutsApplicationContext appContext;
    DerbyOptions options;
    NutsLogger LOG;

    public DerbyService(NutsApplicationContext appContext) {
        this.appContext = appContext;
        LOG = appContext.workspace().log().of(getClass());
    }

    private Path download(String id, Path folder, boolean optional) {
        final NutsId iid = appContext.getWorkspace().id().parse(id);
//        Path downloadBaseFolder = folder//.resolve(iid.getVersion().getValue());
        Path targetFile = folder.resolve(iid.getArtifactId() + ".jar");
        if (!Files.exists(targetFile)) {
            if (optional) {
                Path r = appContext.getWorkspace().fetch().location(targetFile).id(id).failFast(false).getResultPath();
                if (r != null) {
                    LOG.with().level(Level.FINEST).verb("READ").log("downloading {0} to {1}", id, targetFile);
                }
            } else {
                appContext.getWorkspace().fetch().location(targetFile).id(id).failFast().getResultPath();
                LOG.with().level(Level.FINEST).verb("READ").log( "downloading {0} to {1}", id, targetFile);
            }
        } else {
            LOG.with().level(Level.FINEST).verb("READ").log( "using {0} form {1}", id, targetFile);
        }
        return targetFile;
    }

    public Set<String> findVersions() {
        NutsWorkspace ws = appContext.getWorkspace();
        NutsId java = appContext.getWorkspace().config().getPlatform();
        List<String> all = ws.search().session(appContext.getSession().copy().silent()).addId("org.apache.derby:derbynet").distinct()
                .setIdFilter((id, session) -> {
                    if (java.getVersion().compareTo("1.9") < 0) {
                        return id.getVersion().compareTo("10.15.1.3") < 0;
                    }
                    return true;
                })
                .getResultIds().stream().map(x -> x.getVersion().toString()).collect(Collectors.toList());
        TreeSet<String> lastFirst = new TreeSet<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.compareTo(o1);
            }
        });
        lastFirst.addAll(all);
        return lastFirst;
    }

    public NutsExecCommand command(DerbyOptions options) {
        List<String> command = new ArrayList<>();
        List<String> executorOptions = new ArrayList<>();
        NutsWorkspace ws = appContext.getWorkspace();
        String currentDerbyVersion = options.derbyVersion;
        if (currentDerbyVersion == null) {
            NutsId java = appContext.getWorkspace().config().getPlatform();
            NutsId best = ws.search().session(appContext.getSession().copy().silent()).addId("org.apache.derby:derbynet").distinct().latest()
                    .setIdFilter((id, session) -> {
                        if (java.getVersion().compareTo("1.9") < 0) {
                            return id.getVersion().compareTo("10.15.1.3") < 0;
                        }
                        return true;
                    })
                    .session(appContext.getSession().copy().silent())
                    .getResultIds().singleton();
            currentDerbyVersion = best.getVersion().toString();
        }

        Path derbyDataHome = null;
        if (options.derbyDataHomeReplace != null) {
            derbyDataHome = appContext.getVarFolder();
        } else {
            if (options.derbyDataHomeRoot != null && options.derbyDataHomeRoot.trim().length() > 0) {
                derbyDataHome = Paths.get(getAbsoluteFile(options.derbyDataHomeRoot, appContext.getVarFolder().toString()));
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
        Path derbyBinHome = ws.config().getStoreLocation(appContext.getAppId(), NutsStoreLocation.APPS).resolve(currentDerbyVersion);
        Path derbyLibHome = derbyBinHome.resolve("lib");
        Path derby = download("org.apache.derby:derby#" + currentDerbyVersion, derbyLibHome, false);
        Path derbynet = download("org.apache.derby:derbynet#" + currentDerbyVersion, derbyLibHome, false);
        Path derbyoptionaltools = download("org.apache.derby:derbyoptionaltools#" + currentDerbyVersion, derbyLibHome, true);
        Path derbyclient = download("org.apache.derby:derbyclient#" + currentDerbyVersion, derbyLibHome, false);
        Path derbytools = download("org.apache.derby:derbytools#" + currentDerbyVersion, derbyLibHome, false);
        Path policy = derbyBinHome.resolve("derby.policy");
        if (!Files.exists(policy) || appContext.session().isYes()) {
            try {
                String permissions = net.vpc.common.io.IOUtils.loadString(DerbyMain.class.getResourceAsStream("policy-file.policy"))
                        .replace("${{DB_PATH}}", derbyDataHomeRoot.toString());
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
                "--classpath=" + derby + ":" + derbynet + ":" + derbyclient + ":" + derbytools
                        +
                        (derbyoptionaltools != null ? (":" + derbyoptionaltools) : "")
        );
//        if (appContext.session().isPlainTrace()) {
//            executorOptions.add("--show-command");
//        }
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
        return ws
                .exec()
                .executorOptions(executorOptions)
                .command(command)
                .directory(derbyBinHome.toString())
                .failFast()
                .session(appContext.getSession());
    }

    void exec(DerbyOptions options) {
        NutsExecCommand cmd = command(options);
        boolean[] finished = new boolean[1];
        Thread t = new Thread(() -> {
            try {
                cmd.run();
            } finally {
                finished[0] = true;
            }

        }, "Derby");
        t.setDaemon(true);
        t.start();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
