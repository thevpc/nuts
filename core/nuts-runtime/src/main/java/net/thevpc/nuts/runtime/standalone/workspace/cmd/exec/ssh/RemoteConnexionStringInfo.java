package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommandExtensionContext;
import net.thevpc.nuts.util.*;

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
        Map<String, RemoteConnexionStringInfo> m = session.getOrComputeWorkspaceProperty(RemoteConnexionStringInfo.class.getName() + "Map",
                s -> new HashMap<>()
        );
        RemoteConnexionStringInfo k = m.computeIfAbsent(target, v -> new RemoteConnexionStringInfo(v));
        return k;
    }


    public RemoteConnexionStringInfo(String target) {
        this.target = target;
    }

    void tryUpdate(NExecCommandExtension commExec, NSession session) {
        if (isUpdatable()) {
            update(commExec, session);
            lastChecked = System.currentTimeMillis();
        }
    }

    void update(NExecCommandExtension commExec, NSession session) {
        NLog log = NLog.of(DefaultSpawnExecutableRemote.class, session);

    }

    public boolean copyId(NId id, NPath remoteRepo, NSession session, NRef<NPath> remoteJar) {
        NDefinition def = NFetchCommand.of(session)
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
        NLog log = NLog.of(DefaultSpawnExecutableRemote.class, session);
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


    public static String runOnceGrab(NExecCommandExtension commExec, String target, NSession session, String... cmd) {
        NExecOutput out = NExecOutput.ofGrabMem();
        NExecOutput err = NExecOutput.ofGrabMem();
        int e = commExec.exec(new DefaultNExecCommandExtensionContext(
                target,
                cmd, session,
                NExecInput.ofNull(),
                out,
                err
        ));
        if (e != NExecutionException.SUCCESS) {
            session.err().println(out.getResultString());
            session.err().println(err.getResultString());
            throw new NExecutionException(session, NMsg.ofC("command exit with code :%s", e), e);
        }
        return out.getResultString();
    }

    public String getJavaCommand(NExecCommandExtension commExec, NSession session) {
        return javaCommand;
    }

    public String getNutsJar(NExecCommandExtension commExec, NSession session) {
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

    public long getLastChecked(NExecCommandExtension commExec, NSession session) {
        return lastChecked;
    }

    public long getTimoutMS(NExecCommandExtension commExec, NSession session) {
        return timoutMS;
    }

    public String getRootName(NExecCommandExtension commExec, NSession session) {
        return rootName;
    }

    public String getUserName(NExecCommandExtension commExec, NSession session) {
        getUserHome(commExec, session);
        return userName;
    }

    public String getUserHome(NExecCommandExtension commExec, NSession session) {
        if (isUpdatable(loadedUserHome)) {
            loadedUserHome = System.currentTimeMillis();
            // echo -e "$USER\\n$HOME\n$OSTYPE\n$PATH"
            log(session).with().level(Level.FINER).verb(NLogVerb.START).log(NMsg.ofC("[%s] resolve remote env", target));
            String[] echoes = NStringUtils.trim(runOnceGrab(
                    commExec, target, session,
                    "echo", "-e", "$USER\\\\n$HOME\\\\n$OSTYPE\\\\n$PATH")).split("\n");
            userName = echoes[0];
            userHome = echoes[1];
            osType = echoes[2];
            envPath = echoes[3];
        }
        return userHome;
    }

    public String getOsType(NExecCommandExtension commExec, NSession session) {
        getUserHome(commExec, session);
        return osType;
    }

    public String getWorkspaceName(NExecCommandExtension commExec, NSession session) {
        return workspaceName;
    }

    public String getStoreLocationLib(NExecCommandExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationLib;
    }

    public NPath getStoreLocationLibRepo(NExecCommandExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationLibRepo;
    }

    public String getStoreLocationCache(NExecCommandExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationCache;
    }

    public NPath getStoreLocationCacheRepo(NExecCommandExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationCacheRepo;
    }

    public NPath getStoreLocationCacheRepoSSH(NExecCommandExtension commExec, NSession session) {
        getWorkspaceJson(commExec, session);
        return storeLocationCacheRepoSSH;
    }

    public String getTarget(NExecCommandExtension commExec, NSession session) {
        return target;
    }

    private NLog log(NSession session) {
        if (log == null) {
            log = NLog.of(DefaultSpawnExecutableRemote.class, session);
        }
        return log;
    }

    public NElement getWorkspaceJson(NExecCommandExtension commExec, NSession session) {
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
                workspaceJson = NElements.of(session)
                        .parse(
                                NPath.of(targetConnexion.copy()
                                        .setPath(pHome.getHome() + "/ws/" + workspaceName + "/nuts-workspace.json")
                                        .toString(), session)
                        );
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
            if (storeLocationCache != null) {
                storeLocationCache += ("/" + NConstants.Folders.ID);
            }

            storeLocationLibRepo = NPath.of(targetConnexion.copy()
                    .setPath(storeLocationLib)
                    .toString(), session).resolve(NConstants.Folders.ID);
            storeLocationCacheRepo = NPath.of(targetConnexion.copy()
                    .setPath(storeLocationCache)
                    .toString(), session).resolve(NConstants.Folders.ID);
            storeLocationCacheRepoSSH = storeLocationCacheRepo.resolve(NIdUtils.resolveIdPath(session.getAppId())).resolve("repo");
            NPath e = storeLocationCacheRepoSSH.resolve(".nuts-repository");
            if (!e.isRegularFile()) {
                e.mkParentDirs().writeString("{}");
            }
        }
        return workspaceJson;
    }

    public String getEnvPath(NExecCommandExtension commExec, NSession session) {
        getUserHome(commExec, session);
        return envPath;
    }

    public String getSuPath(NExecCommandExtension commExec, NSession session) {
        if (isUpdatable(loadedSu)) {
            loadedSu = System.currentTimeMillis();
            suPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceGrab(commExec, target, session, "which", "su")).trim());
        }
        return suPath;
    }

    public String getSudoPath(NExecCommandExtension commExec, NSession session) {
        if (isUpdatable(loadedSudo)) {
            loadedSudo = System.currentTimeMillis();
            sudoPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceGrab(commExec, target, session, "which", "sudo")).trim());
        }
        return sudoPath;
    }

    public String[] buildEffectiveCommand(String[] cmd, NRunAs runAs, String[] executionOptions, NExecCommandExtension commExec, NSession session) {
        return NSysExecUtils.buildEffectiveCommand(cmd, runAs,
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
                executionOptions,
                session).toArray(new String[0]);
    }


}
