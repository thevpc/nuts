package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.format.NDescriptorFormat;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NConnexionStringBuilder;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.spi.NScopeType;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;

public class RemoteConnexionStringInfo {
    private String javaCommand = "java";
    private String nutsJar;
    private long lastChecked;
    private long timoutMS = 1000;
    private String rootName = "root";
    private String userName;
    private String userHome;
    private String osType;
    private String envPath;
    private String workspaceName = "default-workspace";
    private NElement workspaceJson;
    private String storeLocationLib;
    private NPath storeLocationLibRepo;
    private String storeLocationCache;
    private NPath storeLocationCacheRepo;
    private NPath storeLocationCacheRepoSSH;
    private String target;
    private String suPath;
    private String sudoPath;

    private long loadedUserHome;
    private long loadedWorkspaceJson;
    private long loadedNutsJar;
    private long loadedSu;
    private long loadedSudo;


    public static RemoteConnexionStringInfo of(String target) {
        NAssert.requireNonBlank(target, "target");
        Map<String, RemoteConnexionStringInfo> m = NApp.of().getOrComputeProperty(RemoteConnexionStringInfo.class.getName() + "Map",
                NScopeType.WORKSPACE,
                () -> new HashMap<>()
        );
        RemoteConnexionStringInfo k = m.computeIfAbsent(target, v -> new RemoteConnexionStringInfo(v));
        return k;
    }


    public RemoteConnexionStringInfo(String target) {
        this.target = target;
    }

    public boolean copyId(NId id, NPath remoteRepo, NRef<NPath> remoteJar) {
        NDefinition def = NFetchCmd.of(id)
                .setDependencyFilter(NDependencyFilters.of().byRunnable())
                .getResultDefinition();
        NPath apiLocalPath = def.getContent().get();
        NPath remoteJarPath = remoteRepo.resolve(id.getMavenPath("jar"));
        if (remoteJar != null) {
            remoteJar.set(remoteJarPath);
        }
        if (copy(apiLocalPath, remoteJarPath)) {
            NDescriptorFormat.of(def.getDescriptor()).setNtf(false).print(
                    remoteRepo.resolve(id.getMavenPath("nuts")).mkParentDirs()
            );
            return true;
        }
        return false;
    }

    public boolean copy(NPath local, NPath remote) {
        NLog log = LOG();
        log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("try copy %s %s", local, remote));
        long localContentLength = local.contentLength();
        long remoteContentLength = remote.contentLength();
        if (remoteContentLength >= 0) {
            if (localContentLength == remoteContentLength) {
                String ld = local.getDigestString();
                String rd = remote.getDigestString();
                if (ld.equals(rd)) {
                    log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("do not copy %s %s", local, remote));
                    return false;
                }
            }
        }
        log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("copy %s %s", local, remote));
        local.copyTo(remote.mkParentDirs());
        return true;
    }

    boolean isUpdatable() {
        return lastChecked == 0 || (System.currentTimeMillis() - lastChecked) > timoutMS;
    }

    boolean isUpdatable(long lastChecked) {
        return lastChecked == 0 || (System.currentTimeMillis() - lastChecked) > timoutMS;
    }


    public static String runOnceSystemGrab(NExecCmdExtension commExec, String target, String... cmd) {

        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();
        int e;
        NSession session = NSession.of();
        try (MyNExecCmdExtensionContext d = new MyNExecCmdExtensionContext(
                NExecCmd.of().setTarget(target).system(),
                commExec, target, session, cmd, out, err)) {
            e = commExec.exec(d);
        } catch (RuntimeException ex) {
            throw new NExecutionException(NMsg.ofC("command failed :%s", ex), ex);
        }
        if (e != NExecutionException.SUCCESS) {
            session.err().println(out.toString());
            session.err().println(err.toString());
            throw new NExecutionException(NMsg.ofC("command exit with code :%s", e), e);
        }
        return out.toString();
    }

    public String getJavaCommand(NExecCmdExtension commExec) {
        return javaCommand;
    }

    public String getNutsJar(NExecCmdExtension commExec) {
        if (isUpdatable(loadedNutsJar)) {
            loadedNutsJar = System.currentTimeMillis();
            LOG().with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] resolve remote jar", target));
            NRef<NPath> remoteApiJar = NRef.ofNull();
            NWorkspace workspace = NWorkspace.of();
            copyId(workspace.getApiId(), getStoreLocationLibRepo(commExec), remoteApiJar);
            copyId(workspace.getRuntimeId(), getStoreLocationLibRepo(commExec), null);
            nutsJar = remoteApiJar.get().getLocation();
        }
        return nutsJar;
    }

    public long getLastChecked(NExecCmdExtension commExec) {
        return lastChecked;
    }

    public long getTimoutMS(NExecCmdExtension commExec) {
        return timoutMS;
    }

    public String getRootName(NExecCmdExtension commExec) {
        return rootName;
    }

    public String getUserName(NExecCmdExtension commExec) {
        getUserHome(commExec);
        return userName;
    }

    public String getUserHome(NExecCmdExtension commExec) {
        if (isUpdatable(loadedUserHome)) {
            loadedUserHome = System.currentTimeMillis();
            // echo -e "$USER\\n$HOME\n$OSTYPE\n$PATH"
            LOG().with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] resolve remote env", target));
            String[] echoes = NStringUtils.trim(runOnceSystemGrab(
                    commExec, target,
                    "echo", "-e", "$USER\\\\n$HOME\\\\n$OSTYPE\\\\n$PATH")).split("\n");
            userName = echoes[0];
            userHome = echoes[1];
            osType = echoes[2];
            envPath = echoes[3];
        }
        return userHome;
    }

    public String getOsType(NExecCmdExtension commExec) {
        getUserHome(commExec);
        return osType;
    }

    public String getWorkspaceName(NExecCmdExtension commExec) {
        return workspaceName;
    }

    public String getStoreLocationLib(NExecCmdExtension commExec) {
        getWorkspaceJson(commExec);
        return storeLocationLib;
    }

    public NPath getStoreLocationLibRepo(NExecCmdExtension commExec) {
        getWorkspaceJson(commExec);
        return storeLocationLibRepo;
    }

    public String getStoreLocationCache(NExecCmdExtension commExec) {
        getWorkspaceJson(commExec);
        return storeLocationCache;
    }

    public NPath getStoreLocationCacheRepo(NExecCmdExtension commExec) {
        getWorkspaceJson(commExec);
        return storeLocationCacheRepo;
    }

    public NPath getStoreLocationCacheRepoSSH(NExecCmdExtension commExec) {
        getWorkspaceJson(commExec);
        return storeLocationCacheRepoSSH;
    }

    public String getTarget(NExecCmdExtension commExec) {
        return target;
    }

    private NLog LOG() {
        return NLog.of(RemoteConnexionStringInfo.class);
    }

    public NElement getWorkspaceJson(NExecCmdExtension commExec) {
        if (isUpdatable(loadedWorkspaceJson)) {
            loadedWorkspaceJson = System.currentTimeMillis();
            NConnexionStringBuilder targetConnexion = DefaultNConnexionStringBuilder.of(target).get()
                    .setQueryString(null)
                    .setPath(null);
            NPlatformHome pHome = NPlatformHome.ofPortable(NOsFamily.LINUX, false, null, p -> {
                switch (p) {
                    case "user.name": {
                        return getUserName(commExec);
                    }
                    case "user.home": {
                        return getUserHome(commExec);
                    }
                }
                return null;
            });
            try {
                workspaceJson = null;
                NPath rpath = NPath.of(targetConnexion.copy()
                        .setPath(pHome.getHome() + "/ws/" + workspaceName + "/nuts-workspace.json")
                        .toString());
                if (rpath.isRegularFile()) {
                    workspaceJson = NElements.of()
                            .parse(
                                    rpath
                            );
                }
            } catch (Exception e) {
                //not found!
            }
            storeLocationLib = null;
            if (workspaceJson != null && workspaceJson.isObject()) {
                storeLocationLib = workspaceJson.asObject().get().getByPath("storeLocations", "lib").map(NElement::asLiteral).flatMap(NLiteral::asString).orNull();
                storeLocationCache = workspaceJson.asObject().get().getByPath("storeLocations", "cache").map(NElement::asLiteral).flatMap(NLiteral::asString).orNull();
            }
            if (storeLocationLib == null) {
                storeLocationLib = pHome.getWorkspaceStore(NStoreType.LIB, workspaceName);
            }
            if (storeLocationCache == null) {
                storeLocationCache = pHome.getWorkspaceStore(NStoreType.CACHE, workspaceName);
            }

            storeLocationLibRepo = NPath.of(targetConnexion.copy()
                    .setPath(storeLocationLib)
                    .toString()).resolve(NConstants.Folders.ID);
            storeLocationCacheRepo = NPath.of(targetConnexion.copy()
                    .setPath(storeLocationCache)
                    .toString()).resolve(NConstants.Folders.ID);
            NId appId = NApp.of().getId().orElseGet(()->NWorkspace.of().getApiId());
            storeLocationCacheRepoSSH = storeLocationCacheRepo.resolve(appId.getMavenFolder()).resolve("repo");
            NPath e = storeLocationCacheRepoSSH.resolve(".nuts-repository");
            if (!e.isRegularFile()) {
                e.mkParentDirs().writeString("{}");
            }
        }
        return workspaceJson;
    }

    public String getEnvPath(NExecCmdExtension commExec) {
        getUserHome(commExec);
        return envPath;
    }

    public String getSuPath(NExecCmdExtension commExec) {
        if (isUpdatable(loadedSu)) {
            loadedSu = System.currentTimeMillis();
            suPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceSystemGrab(commExec, target, "which", "su")).trim());
        }
        return suPath;
    }

    public String getSudoPath(NExecCmdExtension commExec) {
        if (isUpdatable(loadedSudo)) {
            loadedSudo = System.currentTimeMillis();
            sudoPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceSystemGrab(commExec, target, "which", "sudo")).trim());
        }
        return sudoPath;
    }

    public String[] buildEffectiveCommand(String[] cmd, NRunAs runAs, String[] executionOptions, NExecCmdExtension commExec) {
        return NWorkspace.of().buildEffectiveCommand(cmd, runAs,
                new HashSet<NDesktopEnvironmentFamily>(),
                s -> {
                    if (s == null) {
                        return null;
                    }
                    switch (s) {
                        case "su":
                            return getSuPath(commExec);
                        case "sudo":
                            return getSudoPath(commExec);
                    }
                    return null;
                },
                false,
                getRootName(commExec),
                getUserName(commExec),
                executionOptions
        ).toArray(new String[0]);
    }


    private static class MyNExecCmdExtensionContext implements NExecCmdExtensionContext, AutoCloseable {
        NExecCmdExtension commExec;
        String target;
        NSession session;
        String[] cmd;
        OutputStream out;
        OutputStream err;
        InputStream nullInput;
        NExecCmd ec;

        public MyNExecCmdExtensionContext(NExecCmd ec, NExecCmdExtension commExec, String target, NSession session, String[] cmd, OutputStream out, OutputStream err) {
            this.ec = ec;
            this.commExec = commExec;
            this.target = target;
            this.session = session;
            this.cmd = cmd;
            this.out = out;
            this.err = err;
            nullInput = NIO.ofNullRawInputStream();
        }

        @Override
        public String getTarget() {
            return target;
        }

        @Override
        public String[] getCommand() {
            return cmd;
        }

        @Override
        public InputStream in() {
            return nullInput;
        }

        @Override
        public OutputStream out() {
            return out;
        }

        @Override
        public OutputStream err() {
            return err;
        }

        @Override
        public NExecCmd getExecCommand() {
            return ec;
        }

        @Override
        public NSession getSession() {
            return session;
        }

        @Override
        public void close() {

        }
    }
}
