package net.thevpc.nuts.ext.ssh;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.artifact.NDependencyFilters;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.command.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.*;
import net.thevpc.nuts.spi.NExecTargetCommandContext;
import net.thevpc.nuts.spi.NExecTargetSPI;
import net.thevpc.nuts.text.NDescriptorFormat;
import net.thevpc.nuts.io.NIO;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.net.NConnectionStringBuilder;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NRef;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.*;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class RemoteConnectionStringInfo {
    private String javaCommand = "java";
    private String nutsJar;
    private long lastChecked;
    private long timoutMS = 1000;
    private String workspaceName = "default-workspace";
    private NElement workspaceJson;
    private String storeLocationLib;
    private NPath storeLocationLibRepo;
    private String storeLocationCache;
    private NPath storeLocationCacheRepo;
    private NPath storeLocationCacheRepoSSH;
    private NConnectionString connectionString;
    private String suPath;
    private String sudoPath;

    private long loadedUserHome;
    private long loadedWorkspaceJson;
    private long loadedNutsJar;
    private long loadedSu;
    private long loadedSudo;
    private NEnv probed;


    public static RemoteConnectionStringInfo of(NConnectionString target) {
        NAssert.requireNonBlank(target, "target");
        Map<NConnectionString, RemoteConnectionStringInfo> m = NWorkspace.of().getOrComputeProperty(RemoteConnectionStringInfo.class.getName() + "Map",
                () -> new HashMap<>()
        );
        RemoteConnectionStringInfo k = m.computeIfAbsent(target, v -> new RemoteConnectionStringInfo(v));
        return k;
    }

    public NEnv getProbedOs() {
        if(probed==null){
            probed= NEnv.of(connectionString);
        }
        return probed;
    }

    public RemoteConnectionStringInfo(NConnectionString connectionString) {
        this.connectionString = connectionString;
    }

    public boolean copyId(NId id, NPath remoteRepo, NRef<NPath> remoteJar) {
        NDefinition def = NFetch.of(id)
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
        log.log(NMsg.ofC("try copy %s %s", local, remote).asFiner().withIntent(NMsgIntent.START));
        long localContentLength = local.getContentLength();
        long remoteContentLength = remote.getContentLength();
        if (remoteContentLength >= 0) {
            if (localContentLength == remoteContentLength) {
                String ld = local.getDigestString();
                String rd = remote.getDigestString();
                if (ld.equals(rd)) {
                    log.log(NMsg.ofC("do not copy %s %s", local, remote).asFiner().withIntent(NMsgIntent.START));
                    return false;
                }
            }
        }
        log.log(NMsg.ofC("copy %s %s", local, remote).asFiner().withIntent(NMsgIntent.START));
        local.copyTo(remote.mkParentDirs());
        return true;
    }

    boolean isUpdatable() {
        return lastChecked == 0 || (System.currentTimeMillis() - lastChecked) > timoutMS;
    }

    boolean isUpdatable(long lastChecked) {
        return lastChecked == 0 || (System.currentTimeMillis() - lastChecked) > timoutMS;
    }


    public static String runOnceSystemGrab(NExecTargetSPI commExec, NConnectionString target, String... cmd) {

        OutputStream out = new ByteArrayOutputStream();
        OutputStream err = new ByteArrayOutputStream();
        int e;
        NSession session = NSession.of();
        try (MyNExecTargetCommandContext d = new MyNExecTargetCommandContext(
                NExec.of().setConnectionString(target).system(),
                commExec, target, cmd, out, err)) {
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

    public String getJavaCommand(NExecTargetSPI commExec) {
        return javaCommand;
    }

    public String getNutsJar(NExecTargetSPI commExec) {
        if (isUpdatable(loadedNutsJar)) {
            loadedNutsJar = System.currentTimeMillis();
            LOG().log(NMsg.ofC("[%s] resolve remote jar", connectionString).asFiner().withIntent(NMsgIntent.START));
            NRef<NPath> remoteApiJar = NRef.ofNull();
            NWorkspace workspace = NWorkspace.of();
            copyId(workspace.getApiId(), getStoreLocationLibRepo(commExec), remoteApiJar);
            copyId(workspace.getRuntimeId(), getStoreLocationLibRepo(commExec), null);
            nutsJar = remoteApiJar.get().getLocation();
        }
        return nutsJar;
    }

    public long getLastChecked(NExecTargetSPI commExec) {
        return lastChecked;
    }

    public long getTimeoutMS(NExecTargetSPI commExec) {
        return timoutMS;
    }

    public String getRootName(NExecTargetSPI commExec) {
        NEnv o = getProbedOs();
        synchronized (o) {
            return o.getRootUserName();
        }
    }

    public String getUserName(NExecTargetSPI commExec) {
        NEnv o = getProbedOs();
        synchronized (o) {
            return o.getUserName();
        }
    }

    public String getUserHome(NExecTargetSPI commExec) {
        NEnv o = getProbedOs();
        synchronized (o) {
            return o.getUserHome();
        }
    }

    public NOsFamily getOsFamily(NExecTargetSPI commExec) {
        NEnv o = getProbedOs();
        synchronized (o) {
            return o.getOsFamily();
        }
    }

    public String getWorkspaceName(NExecTargetSPI commExec) {
        return workspaceName;
    }

    public String getStoreLocationLib(NExecTargetSPI commExec) {
        getWorkspaceJson(commExec);
        return storeLocationLib;
    }

    public NPath getStoreLocationLibRepo(NExecTargetSPI commExec) {
        getWorkspaceJson(commExec);
        return storeLocationLibRepo;
    }

    public String getStoreLocationCache(NExecTargetSPI commExec) {
        getWorkspaceJson(commExec);
        return storeLocationCache;
    }

    public NPath getStoreLocationCacheRepo(NExecTargetSPI commExec) {
        getWorkspaceJson(commExec);
        return storeLocationCacheRepo;
    }

    public NPath getStoreLocationCacheRepoSSH(NExecTargetSPI commExec) {
        getWorkspaceJson(commExec);
        return storeLocationCacheRepoSSH;
    }

    public NConnectionString getConnectionString() {
        return connectionString;
    }

    private NLog LOG() {
        return NLog.of(RemoteConnectionStringInfo.class);
    }

    public NElement getWorkspaceJson(NExecTargetSPI commExec) {
        if (isUpdatable(loadedWorkspaceJson)) {
            loadedWorkspaceJson = System.currentTimeMillis();
            NConnectionStringBuilder targetConnection = connectionString.builder()
                    .setQueryString(null)
                    .setPath(null);
            NPlatformHome pHome = NPlatformHome.ofPortable(getOsFamily(commExec), false, null, p -> {
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
                NPath rpath = NPath.of(targetConnection.copy()
                        .setPath(pHome.getHome() + "/ws/" + workspaceName + "/" + NConstants.Files.WORKSPACE_CONFIG_FILE_NAME)
                        .toString());
                if (rpath.isRegularFile()) {
                    workspaceJson = NElementParser.ofJson()
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

            storeLocationLibRepo = NPath.of(targetConnection.copy()
                    .setPath(storeLocationLib)
                    .toString()).resolve(NConstants.Folders.ID);
            storeLocationCacheRepo = NPath.of(targetConnection.copy()
                    .setPath(storeLocationCache)
                    .toString()).resolve(NConstants.Folders.ID);
            NId appId = NApp.of().getId().orElseGet(() -> NWorkspace.of().getApiId());
            storeLocationCacheRepoSSH = storeLocationCacheRepo.resolve(appId.getMavenFolder()).resolve("repo");
            NPath e = storeLocationCacheRepoSSH.resolve(".nuts-repository");
            if (!e.isRegularFile()) {
                e.mkParentDirs().writeString("{}");
            }
        }
        return workspaceJson;
    }

    public String getSuPath(NExecTargetSPI commExec) {
        if (isUpdatable(loadedSu)) {
            loadedSu = System.currentTimeMillis();
            suPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceSystemGrab(commExec, connectionString, "which", "su")).trim());
        }
        return suPath;
    }

    public String getSudoPath(NExecTargetSPI commExec) {
        if (isUpdatable(loadedSudo)) {
            loadedSudo = System.currentTimeMillis();
            sudoPath = NStringUtils.trimToNull(NStringUtils.trim(runOnceSystemGrab(commExec, connectionString, "which", "sudo")).trim());
        }
        return sudoPath;
    }

    public String[] buildEffectiveCommand(String[] cmd, NRunAs runAs, String[] executionOptions, NExecTargetSPI commExec) {
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


    public static class MyNExecTargetCommandContext implements NExecTargetCommandContext, AutoCloseable {
        NExecTargetSPI commExec;
        NConnectionString target;
        String[] cmd;
        OutputStream out;
        OutputStream err;
        InputStream nullInput;
        NExec ec;
        boolean rawCommand;

        public MyNExecTargetCommandContext(NExec ec, NExecTargetSPI commExec, NConnectionString target, String[] cmd, OutputStream out, OutputStream err) {
            this.ec = ec;
            this.commExec = commExec;
            this.target = target;
            this.cmd = cmd;
            this.out = out;
            this.err = err;
            nullInput = NIO.ofNullRawInputStream();
            this.rawCommand = ec.isRawCommand();
        }

        @Override
        public boolean isRawCommand() {
            return rawCommand;
        }

        @Override
        public NConnectionString getConnectionString() {
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
        public NExec getExecCommand() {
            return ec;
        }

        @Override
        public void close() {

        }
    }
}
