/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultSpawnExecutableRemote extends AbstractNExecutableCommand {

    NDefinition def;
    String[] cmd;
    List<String> executorOptions;
    NConnexionString connexionString;
    private boolean showCommand = false;
    private NExecCommandExtension commExec;
    NExecInput in;
    NExecOutput out;
    NExecOutput err;


    private static class RemoteConnexionStringInfo {
        public String javaCommand = "java";
        public String nutsJar;
        long lastChecked;
        long timoutMS = 1000;
        String userName;
        String userHome;
        String osType;
        String workspaceName = "default-workspace";
        NElement workspaceJson;
        String storeLocationLib;
        NPath storeLocationLibRepo;
        String storeLocationCache;
        NPath storeLocationCacheRepo;
        NPath storeLocationCacheRepoSSH;

        void tryUpdate(DefaultSpawnExecutableRemote r) {
            if (isUpdatable()) {
                update(r);
                lastChecked = System.currentTimeMillis();
            }
        }

        void update(DefaultSpawnExecutableRemote r) {
            if (userHome == null || userName == null) {
                // echo -e "$USER\\n$HOME\n$OSTYPE"
                String[] echoes = NStringUtils.trim(r.runOnceGrab("echo", "-e", "$USER\\\\n$HOME\\\\n$OSTYPE")).split("\n");
                userName = echoes[0];
                userHome = echoes[1];
                osType = echoes[2];
            }
            if (NBlankable.isBlank(nutsJar)) {
                NSession session = r.getSession();
                NConnexionString targetConnexion = NConnexionString.of(r.getExecCommand().getTarget()).get().copy()
                        .setQueryString(null)
                        .setPath(null);
                NPlatformHome pHome = NPlatformHome.ofPortable(NOsFamily.LINUX, false, null, p -> {
                    switch (p) {
                        case "user.name": {
                            return userName;
                        }
                        case "user.home": {
                            return userHome;
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
                storeLocationCacheRepoSSH.resolve(".nuts-repository").mkParentDirs().writeString("{}");
                if (NBlankable.isBlank(nutsJar)) {
                    NRef<NPath> remoteApiJar = NRef.ofNull();
                    copyId(session.getWorkspace().getApiId(), storeLocationLibRepo, session, remoteApiJar);
                    copyId(session.getWorkspace().getRuntimeId(), storeLocationLibRepo, session, null);
                    nutsJar = remoteApiJar.get().getLocation();
                }
            }
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
            if (copy(apiLocalPath, remoteJarPath)) {
                def.getDescriptor().formatter(session).setNtf(false).print(
                        remoteRepo.resolve(NIdUtils.resolveDescPath(id)).mkParentDirs()
                );
                return true;
            }
            return false;
        }

        public boolean copy(NPath local, NPath remote) {
            long localContentLength = local.getContentLength();
            long remoteContentLength = remote.getContentLength();
            if(remoteContentLength>=0) {
                if (localContentLength == remoteContentLength) {
                    String ld = local.getDigestString();
                    String rd = remote.getDigestString();
                    if (ld.equals(rd)) {
                        return false;
                    }
                }
            }
            local.copyTo(remote.mkParentDirs());
            return true;
        }

        boolean isUpdatable() {
            return lastChecked == 0 || (System.currentTimeMillis() - lastChecked) > timoutMS;
        }
    }


    public DefaultSpawnExecutableRemote(NExecCommandExtension commExec, NDefinition def, String[] cmd,
                                        List<String> executorOptions, NExecCommand execCommand,
                                        NExecInput in,
                                        NExecOutput out,
                                        NExecOutput err

    ) {
        super(def.getId().toString(),
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.def = def;
        this.in = in;
        this.out = out;
        this.err = err;
        this.cmd = cmd;
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.commExec = commExec;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get(getSession());
            switch (aa.key()) {
                case "--show-command": {
                    cmdLine.withNextFlag((v, a, s) -> this.showCommand = (v));
                    break;
                }
                default: {
                    cmdLine.skip();
                }
            }
        }
    }

    @Override
    public NId getId() {
        return def.getId();
    }

    private AbstractSyncIProcessExecHelper resolveExecHelper() {
        return new AbstractSyncIProcessExecHelper(getSession()) {
            @Override
            public int exec() {
                String[] nec = resolveNutsExecutableCommand();
                boolean requireTempRepo = false;
                if (def != null) {
                    requireTempRepo = prepareExecution();
                }
                ArrayList<String> cmd2 = new ArrayList<>();
                cmd2.addAll(Arrays.asList(nec));
                cmd2.add("--bot");
                cmd2.add("--yes");
                cmd2.add("---caller-app=remote-nuts");
                if (requireTempRepo) {
                    RemoteConnexionStringInfo k = getRemoteConnexionStringInfo(getSession());
                    cmd2.add("-r+=" + k.storeLocationCacheRepoSSH.getLocation());
                }
                cmd2.add("--exec");
                cmd2.addAll(executorOptions);
                if (def != null) {
                    cmd2.add(def.getId().toString());
                }
                cmd2.addAll(Arrays.asList(cmd));
                return runOnce(cmd2.toArray(new String[0]));
            }
        };
    }

    private boolean prepareExecution() {
        NSession session = getSession();
        RemoteConnexionStringInfo k = getRemoteConnexionStringInfo(session);
        int count = 0;
        for (NDependency d : def.getDependencies().get()) {
            NId id = d.toId();
            //just ignore nuts base deps!
            if (
                    !id.equalsLongId(session.getWorkspace().getApiId())
                            && !id.equalsLongId(session.getWorkspace().getRuntimeId())
            ) {
                k.copyId(id, k.storeLocationCacheRepoSSH, session, null);
                count++;
            }
        }
        k.copyId(def.getId(), k.storeLocationCacheRepoSSH, session, null);
        return count > 0;
    }

    private String[] resolveNutsExecutableCommand() {
        RemoteConnexionStringInfo k = getRemoteConnexionStringInfo(getSession());
        k.tryUpdate(this);
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(k.javaCommand);
        cmd.add("-jar");
        cmd.add(k.nutsJar);
        cmd.add("-w");
        cmd.add(k.workspaceName);
        return cmd.toArray(new String[0]);
    }

    @NotNull
    private RemoteConnexionStringInfo getRemoteConnexionStringInfo(NSession session) {
        Map<String, RemoteConnexionStringInfo> m = session.getOrComputeWorkspaceProperty(RemoteConnexionStringInfo.class.getName() + "Map",
                s -> new HashMap<>()
        );
        RemoteConnexionStringInfo k = m.computeIfAbsent(getExecCommand().getTarget(), v -> new RemoteConnexionStringInfo());
        return k;
    }

    private String runOnceGrab(String... cmd) {
        NExecOutput out = NExecOutput.ofGrabMem();
        NExecOutput err = NExecOutput.ofGrabMem();
        int e = commExec.exec(new DefaultNExecCommandExtensionContext(
                getExecCommand().getTarget(),
                cmd, getSession(),
                NExecInput.ofNull(),
                out,
                err
        ));
        if (e != NExecutionException.SUCCESS) {
            getSession().err().println(out.getResultString());
            getSession().err().println(err.getResultString());
            throw new NExecutionException(getSession(), NMsg.ofC("command exit with code :%s", e), e);
        }
        return out.getResultString();
    }

    private int runOnce(String[] cmd) {
        return commExec.exec(new DefaultNExecCommandExtensionContext(
                getExecCommand().getTarget(),
                cmd, getSession(),
                in,
                out,
                err
        ));
    }


    @Override
    public int execute() {
        return resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        switch (NEnvs.of(getSession()).getOsFamily()) {
            case WINDOWS: {
                return NTexts.of(getSession()).ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NTexts.of(getSession()).ofStyled(
                                "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return getExecCommand().getRunAs() + " " + (def == null ? "" : (def.getId() + " ")) + NCmdLine.of(cmd).toString();
    }

}
