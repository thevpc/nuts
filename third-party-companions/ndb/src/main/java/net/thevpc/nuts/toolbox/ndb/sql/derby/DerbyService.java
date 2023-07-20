/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
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
package net.thevpc.nuts.toolbox.ndb.sql.derby;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NLog;
import net.thevpc.nuts.util.NLogVerb;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DerbyService {

    NSession session;
    NLog LOG;

    public DerbyService(NSession session) {
        this.session = session;
        LOG = NLog.of(getClass(), session);
    }

    public boolean isRunning() {
        NDerbyConfig options=new NDerbyConfig();
        options.setCmd(Command.ping);
        try {
            String s= command(options).setFailFast(true).grabOutputString().getOutputString();
            if(s!=null){
                return true;
            }
        } catch (NExecutionException ex) {
            //
        }
        return false;
    }

    /**
     * should promote this to FileUtils !!
     *
     * @param path path
     * @param cwd cwd
     * @return absolute path
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
            case "src/test": {
                File file = new File(cwd, "src/test");
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

    private Path download(String id, Path folder, boolean optional) {
        final NId iid = NId.of(id).get(session);
//        Path downloadBaseFolder = folder//.resolve(iid.getVersion().getValue());
        Path targetFile = folder.resolve(iid.getArtifactId() + ".jar");
        if (!Files.exists(targetFile)) {
            if (optional) {
                Path r = NFetchCommand.of(id,session).setLocation(targetFile).setFailFast(false).getResultPath();
                if (r != null) {
                    LOG.with().session(session).level(Level.FINEST).verb(NLogVerb.READ).log(NMsg.ofJ("downloading {0} to {1}", id, targetFile));
                }
            } else {
                NFetchCommand.of(id,session).setLocation(targetFile).setFailFast(true).getResultPath();
                LOG.with().session(session).level(Level.FINEST).verb(NLogVerb.READ).log(NMsg.ofJ("downloading {0} to {1}", id, targetFile));
            }
        } else {
            LOG.with().session(session).level(Level.FINEST).verb(NLogVerb.READ).log(NMsg.ofJ("using {0} form {1}", id, targetFile));
        }
        return targetFile;
    }

    public Set<String> findVersions() {
        NId java = NEnvs.of(session).getPlatform();
        List<String> all = NSearchCommand.of(session.copy()).addId("org.apache.derby:derbynet").setDistinct(true)
                .setIdFilter(
                        (java.getVersion().compareTo("1.9") < 0) ? NVersionFilters.of(session).byValue("[,10.15.1.3[").get().to(NIdFilter.class) :
                                null)
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

    public NExecCommand command(NDerbyConfig options) {
        List<String> command = new ArrayList<>();
        List<String> executorOptions = new ArrayList<>();
        String currentDerbyVersion = options.getDerbyVersion();
        if (currentDerbyVersion == null) {
            NId java = NEnvs.of(session).getPlatform();
            NId best = NSearchCommand.of(session.copy()).addId("org.apache.derby:derbynet").setDistinct(true).setLatest(true)
                    .setIdFilter(
                            (java.getVersion().compareTo("1.9") < 0) ? NVersionFilters.of(session).byValue("[,10.15.1.3[").get().to(NIdFilter.class) :
                                    null)
                    .getResultIds().findSingleton().get();
            currentDerbyVersion = best.getVersion().toString();
        }

        NPath derbyDataHome = null;
        if (options.getDerbyDataHomeReplace() != null) {
            derbyDataHome = session.getAppSharedVarFolder();
        } else {
            if (options.getDerbyDataHomeRoot() != null && options.getDerbyDataHomeRoot().trim().length() > 0) {
                derbyDataHome = NPath.of(options.getDerbyDataHomeRoot(),session).toAbsolute(session.getAppSharedVarFolder());
            } else {
                derbyDataHome = session.getAppSharedVarFolder().resolve("derby-db");
            }
        }
        NPath derbyDataHomeRoot = derbyDataHome.getParent();
        derbyDataHome.mkdirs();
        Path derbyBinHome = NLocations.of(session).getStoreLocation(session.getAppId(), NStoreType.BIN).resolve(currentDerbyVersion).toPath().get();
        Path derbyLibHome = derbyBinHome.resolve("lib");
        Path derby = download("org.apache.derby:derby#" + currentDerbyVersion, derbyLibHome, false);
        Path derbynet = download("org.apache.derby:derbynet#" + currentDerbyVersion, derbyLibHome, false);
        Path derbyoptionaltools = download("org.apache.derby:derbyoptionaltools#" + currentDerbyVersion, derbyLibHome, true);
        Path derbyclient = download("org.apache.derby:derbyclient#" + currentDerbyVersion, derbyLibHome, false);
        Path derbytools = download("org.apache.derby:derbytools#" + currentDerbyVersion, derbyLibHome, false);
        Path policy = derbyBinHome.resolve("derby.policy");
        if (!Files.exists(policy) || session.isYes()) {
            try (InputStream is=NDerbyMain.class.getResourceAsStream("/net/thevpc/nuts/toolbox/ndb/derby/policy-file.policy")){
                String permissions = new String(DerbyUtils.loadByteArray(is))
                        .replace("${{DB_PATH}}", derbyDataHomeRoot.toString());
                Files.write(policy, permissions.getBytes());
            } catch (IOException ex) {
                throw new NExecutionException(session, NMsg.ofC("unable to create %s",policy), NExecutionException.ERROR_1);
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
//        if (session.isPlainTrace()) {
//            executorOptions.add("--show-command");
//        }
        executorOptions.add("--main-class=org.apache.derby.drda.NetworkServerControl");
        executorOptions.add("-Dderby.system.home=" + derbyDataHome.toString());

        if (options.getHost() != null) {
            command.add("-h");
            command.add(options.getHost());
        }
        if (options.getPort() != -1) {
            command.add("-p");
            command.add(String.valueOf(options.getPort()));
        }
        if (options.getSslmode() != null) {
            command.add("-ssl");
            command.add(String.valueOf(options.getSslmode()));
        }
        command.add(options.getCmd().toString());
        if (options.getExtraArg() != null) {
            command.add(options.getExtraArg());
        }
        return NExecCommand.of(session)
                .addExecutorOptions(executorOptions)
                .addCommand(command)
                .setDirectory(NPath.of(derbyBinHome,session))
                .setFailFast(true)
                ;
    }

    void exec(NDerbyConfig options) {
        NExecCommand cmd = command(options);
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

    public NSession getSession() {
        return session;
    }

}
