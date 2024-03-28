package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.env.NDesktopEnvironmentFamily;
import net.thevpc.nuts.env.NOsFamily;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPath;
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
    private NLog log;

    private long loadedUserHome;
    private long loadedWorkspaceJson;
    private long loadedNutsJar;
    private long loadedSu;
    private long loadedSudo;


    public static RemoteConnexionStringInfo of(String target, NSession session) {
        NAssert.requireNonBlank(target,"target");
        Map<String, RemoteConnexionStringInfo> m = session.getOrComputeProperty(RemoteConnexionStringInfo.class.getName() + "Map",
                NScopeType.WORKSPACE,
                s -> new HashMap<>()
        );
        RemoteConnexionStringInfo k = m.computeIfAbsent(target, v -> new RemoteConnexionStringInfo(v));
        return k;
    }


    public RemoteConnexionStringInfo(String target) {
        this.target = target;
    }

    public boolean copyId(NId id, NPath remoteRepo, NSession session, NRef<NPath> remoteJar) {
        NDefinition def = NFetchCmd.of(session)
                .setId(id)
                .setContent(true)
                .getResultDefinition();
        NPath apiLocalPath = def.getContent().get();
        NPath remoteJarPath = remoteRepo.resolve(NIdUtils.resolveJarPath(id));
        if (remoteJar != null) {
            remoteJar.set(remoteJarPath);
        }
        if (copy(apiLocalPath, remoteJarPath, session)) {
            def.getDescriptor().formatter(session).setNtf(false).print(
                    remoteRepo.resolve(NIdUtils.resolveDescPath(id)).mkParentDirs()
            );
            return true;
        }
        return false;
    }

    public boolean copy(NPath local, NPath remote, NSession session) {
        NLog log = log(session);
        log.with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("try copy %s %s", local, remote));
        long localContentLength = local.getContentLength();
        long remoteContentLength = remote.getContentLength();
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


    public static String runOnceSystemGrab(NExecCmdExtension commExec, String target, NSession session, String... cmd) {

        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();
        int e;
        try (MyNExecCmdExtensionContext d = new MyNExecCmdExtensionContext(
                NExecCmd.of(session).setTarget(target).system(),
                commExec, target, session, cmd, out, err)) {
            e = commExec.exec(d);
        } catch (RuntimeException ex) {
            throw new NExecutionException(session, NMsg.ofC("command failed :%s", ex), ex);
        }
        if (e != NExecutionException.SUCCESS) {
            session.err().println(out.toString());
            session.err().println(err.toString());
            throw new NExecutionException(session, NMsg.ofC("command exit with code :%s", e), e);
        }
        return out.toString();
    }

    public String getJavaCommand(NExecCmdExtension commExec, NSession session) {
        return javaCommand;
    }

    public String getNutsJar(NExecCmdExtension commExec, NSession session) {
        if (isUpdatable(loadedNutsJar)) {
            loadedNutsJar = System.currentTimeMillis();
            log(session).with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] resolve remote jar", target));
            NRef<NPath> remoteApiJar = NRef.ofNull();
            copyId(session.getWorkspace().getApiId(), getStoreLocationLibRepo(commExec, session), session, remoteApiJar);
            copyId(session.getWorkspace().getRuntimeId(), getStoreLocationLibRepo(commExec, session), session, null);
            nutsJar = remoteApiJar.get().getLocation();
        }
        return nutsJar;
    }

    public long getLastChecked(NExecCmdExtension commExec, NSession session) {
        return lastChecked;
    }

    public long getTimoutMS(NExecCmdExtension commExec, NSession session) {
        return timoutMS;
    }

    public String getRootName(NExecCmdExtension commExec, NSession session) {
        return rootName;
    }

    public String getUserName(NExecCmdExtension commExec, NSession session) {
        getUserHome(commExec, session);
        return userName;
    }

    public String getUserHome(NExecCmdExtension commExec, NSession session) {
        if (isUpdatable(loadedUserHome)) {
            loadedUserHome = System.currentTimeMillis();
            // echo -e "$USER\\n$HOME\n$OSTYPE\n$PATH"
            log(session).with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] resolve remote env", target));
            String[] echoes = NStringUtils.trim(runOnceSystemGrab(
                    commExec, target, session,
                    "echo", "-e", "$USER\\\\n$HOME\\\\n$OSTYPE\\\\n$PATH")).split("\n");
            userName = echoes[0];
            userHome = echoes[1];
            osType = echoes[2];
            envPath = echoes[3];
        }
        return userHome;
    }

    public String getOsType(NExecCmdExtension commExec, NSession session) {
        getUserHome(commExec, session);
        return osType;
    }

    public String getWorkspaceName(NExecCmdExtension commExec, NSession session) {
        return workspaceName;
    }

    public String getStoreLocationLib(NExecCmdExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationLib;
    }

    public NPath getStoreLocationLibRepo(NExecCmdExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationLibRepo;
    }

    public String getStoreLocationCache(NExecCmdExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationCache;
    }

    public NPath getStoreLocationCacheRepo(NExecCmdExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationCacheRepo;
    }

    public NPath getStoreLocationCacheRepoSSH(NExecCmdExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationCacheRepoSSH;
    }

    public String getTarget(NExecCmdExtension commExec, NSession session) {
        return target;
    }

    private NLog log(NSession session) {
        if (log == null) {
            log = NLog.of(RemoteConnexionStringInfo.class, session);
        }
        return log;
    }

    public NElement getWorkspaceJson(NExecCmdExtension commExec, NSession session) {
        if (isUpdatable(loadedWorkspaceJson)) {
            loadedWorkspaceJson = System.currentTimeMillis();
            NConnexionString targetConnexion = NConnexionString.of(target).get().copy()
                    .setQueryString(null)
                    .setPath(null);
            NPlatformHome pHome = NPlatformHome.ofPortable(NOsFamily.LINUX, false, null, p -> {
                switch (p) {
                    case "user.name": {
                        return getUserName(commExec, session);
                    }
                    case "user.home": {
                        return getUserHome(commExec, session);
                    }
                }
                return null;
            });
            try {
                workspaceJson = null;
                NPath rpath = NPath.of(targetConnexion.copy()
                        .setPath(pHome.getHome() + "/ws/" + workspaceName + "/nuts-workspace.json")
                        .toString(), session);
                if (rpath.isRegularFile()) {
                    workspaceJson = NElements.of(session)
                            .parse(
                                    rpath
                            );
                }
            } catch (Exception e) {
                //not found!
            }
            storeLocationLib = null;
            if (workspaceJson != null && workspaceJson.isObject()) {
                storeLocationLib = workspaceJson.asObject().get().getStringByPath("storeLocations", "lib").orNull();
                storeLocationCache = workspaceJson.asObject().get().getStringByPath("storeLocations", "cache").orNull();
            }
            if (storeLocationLib == null) {
                storeLocationLib = pHome.getWorkspaceStore(NStoreType.LIB, workspaceName);
            }
            if (storeLocationCache == null) {
                storeLocationCache = pHome.getWorkspaceStore(NStoreType.CACHE, workspaceName);
            }

            storeLocationLibRepo = NPath.of(targetConnexion.copy()
                    .setPath(storeLocationLib)
                    .toString(), session).resolve(NConstants.Folders.ID);
            storeLocationCacheRepo = NPath.of(targetConnexion.copy()
                    .setPath(storeLocationCache)
                    .toString(), session).resolve(NConstants.Folders.ID);
            NId appId = session.getAppId();
            if (appId == null) {
                appId = session.getWorkspace().getApiId();
            }
            storeLocationCacheRepoSSH = storeLocationCacheRepo.resolve(NIdUtils.resolveIdPath(appId)).resolve("repo");
            NPath e = storeLocationCacheRepoSSH.resolve(".nuts-repository");
            if (!e.isRegularFile()) {
                e.mkParentDirs().writeString("{}");
            }
        }
        return workspaceJson;
    }

    public String getEnvPath(NExecCmdExtension commExec, NSession session) {
        getUserHome(commExec, session);
        return envPath;
    }

    public String getSuPath(NExecCmdExtension commExec, NSession session) {
        if (isUpdatable(loadedSu)) {
            loadedSu = System.currentTimeMillis();
            suPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceSystemGrab(commExec, target, session, "which", "su")).trim());
        }
        return suPath;
    }

    public String getSudoPath(NExecCmdExtension commExec, NSession session) {
        if (isUpdatable(loadedSudo)) {
            loadedSudo = System.currentTimeMillis();
            sudoPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceSystemGrab(commExec, target, session, "which", "sudo")).trim());
        }
        return sudoPath;
    }

    public String[] buildEffectiveCommand(String[] cmd, NRunAs runAs, String[] executionOptions, NExecCmdExtension commExec, NSession session) {
        return NEnvs.of(session).buildEffectiveCommand(cmd, runAs,
                new HashSet<NDesktopEnvironmentFamily>(),
                s -> {
                    if (s == null) {
                        return null;
                    }
                    switch (s) {
                        case "su":
                            return getSuPath(commExec, session);
                        case "sudo":
                            return getSudoPath(commExec, session);
                    }
                    return null;
                },
                false,
                getRootName(commExec, session),
                getUserName(commExec, session),
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
            nullInput = NIO.of(session).ofNullRawInputStream();
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
