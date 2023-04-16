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
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NIdUtils;
import net.thevpc.nuts.util.NPlatformHome;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;

/**
 * @author thevpc
 */
public class DefaultSpawnExecutableRemote extends AbstractNExecutableCommand {

    NDefinition def;
    String[] cmd;
    List<String> executorOptions;
    NSession session;
    NSession execSession;
    NExecCommand execCommand;
    NConnexionString connexionString;
    private boolean showCommand = false;
    private final boolean inheritSystemIO;
    private NExecCommandExtension commExec;

    private static class RemoteConnexionStringInfo {
        public String javaCommand = "java";
        public String nutsJar;
        long lastChecked;
        long timoutMS = 1000;
        String userName;
        String userHome;
        String osType;
        String workspaceName = "default-workspace";

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
                NSession session = r.session;
                NConnexionString targetConnexion = NConnexionString.of(r.execCommand.getTarget()).get().copy()
                        .setQueryString(null)
                        .setPath(null);
                NElement workspaceJson = null;
                NPlatformHome pHome = NPlatformHome.ofPortable(NOsFamily.LINUX, userHome);
                try {
                    workspaceJson = NElements.of(session)
                            .parse(
                                    NPath.of(targetConnexion.copy()
                                            .setPath(pHome.getHome() + "/ws/" + workspaceName + "/nuts-workspace.json")
                                            .toString(), session)
                            );
                } catch (Exception e) {
                    //not found!
                }
                String storeLocationLib = null;
                if (workspaceJson != null && workspaceJson.isObject()) {
                    storeLocationLib = workspaceJson.asObject().get().getStringByPath("storeLocations", "lib").orNull();
                }
                if (storeLocationLib == null) {
                    storeLocationLib = pHome.getWorkspaceStore(NStoreType.LIB, workspaceName);
                }

                String sharedNutsApiJar = storeLocationLib + "/" + NIdUtils.resolveJarPath(r.session.getWorkspace().getApiId());
                String sharedNutsApiDesc = storeLocationLib + "/" + NIdUtils.resolveDescPath(r.session.getWorkspace().getApiId());
                String sharedNutsRuntimeJar = storeLocationLib + "/" + NIdUtils.resolveJarPath(r.session.getWorkspace().getRuntimeId());
                String sharedNutsRuntimeDesc = storeLocationLib + "/" + NIdUtils.resolveDescPath(r.session.getWorkspace().getRuntimeId());

                if (NBlankable.isBlank(nutsJar)) {
                    if (!NPath.of(targetConnexion.copy()
                            .setPath(sharedNutsApiJar)
                            .toString(), session).exists()) {
                        NDefinition e = NFetchCommand.of(session)
                                .setId(session.getWorkspace().getApiId())
                                .setContent(true)
                                .getResultDefinition();
                        e.getContent().get().copyTo(
                                NPath.of(targetConnexion
                                                + ":" + sharedNutsApiJar,
                                        session
                                ).mkParentDirs()
                        );
                        e.getDescriptor().formatter(session).setNtf(false).print(
                                NPath.of(targetConnexion
                                                + ":" + sharedNutsApiDesc,
                                        session
                                ).mkParentDirs()
                        );
                    }
                    if (!NPath.of(targetConnexion.copy()
                            .setPath(sharedNutsRuntimeJar)
                            .toString(), session).exists()) {
                        NDefinition e = NFetchCommand.of(session)
                                .setId(session.getWorkspace().getRuntimeId())
                                .setContent(true)
                                .getResultDefinition();
                        e.getContent().get().copyTo(
                                NPath.of(targetConnexion
                                                + ":" + sharedNutsApiJar,
                                        session
                                ).mkParentDirs()
                        );
                        e.getDescriptor().formatter(session).setNtf(false).print(
                                NPath.of(targetConnexion
                                                + ":" + sharedNutsRuntimeDesc,
                                        session
                                ).mkParentDirs()
                        );
                    }
                    nutsJar = sharedNutsApiJar;
                }
            }
        }

        boolean isUpdatable() {
            return lastChecked == 0 || (System.currentTimeMillis() - lastChecked) > timoutMS;
        }
    }


    public DefaultSpawnExecutableRemote(NExecCommandExtension commExec, NDefinition def, String[] cmd,
                                        List<String> executorOptions, NSession session, NSession execSession, NExecCommand execCommand) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM);
        this.inheritSystemIO = execCommand.isInheritSystemIO();
        this.cmd = cmd;
        this.def = def;
        this.execCommand = execCommand;
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.session = session;
        this.execSession = execSession;
        this.commExec = commExec;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get(session);
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
        return new AbstractSyncIProcessExecHelper(session) {
            @Override
            public int exec() {
                String[] nec = resolveNutsExecutableCommand();
                ArrayList<String> cmd2 = new ArrayList<>();
                cmd2.addAll(Arrays.asList(nec));
                cmd2.add("--bot");
                cmd2.add("--yes");
                cmd2.add("---caller-app=remote-nuts");
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

    private String[] resolveNutsExecutableCommand() {
        Map<String, RemoteConnexionStringInfo> m = execSession.getOrComputeWorkspaceProperty(RemoteConnexionStringInfo.class.getName() + "Map",
                s -> new HashMap<>()
        );
        RemoteConnexionStringInfo k = m.computeIfAbsent(execCommand.getTarget(), v -> new RemoteConnexionStringInfo());
        k.tryUpdate(this);
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(k.javaCommand);
        cmd.add("-jar");
        cmd.add(k.nutsJar);
        cmd.add("-w");
        cmd.add(k.workspaceName);
        return cmd.toArray(new String[0]);
    }

    private String runOnceGrab(String... cmd) {
        NSession sc = execSession.copy();
        sc.setTerminal(NSessionTerminal.ofMem(sc));
        int e = commExec.exec(new DefaultNExecCommandExtensionContext(
                execCommand.getTarget(),
                cmd, sc
        ));
        if (e != 0) {
            execSession.err().println(sc.out().toString());
            execSession.err().println(sc.err().toString());
            throw new NExecutionException(session, NMsg.ofC("command exit with code :%s", e), e);
        }
        return sc.out().toString();
    }

    private int runOnce(String[] cmd) {
        return commExec.exec(new DefaultNExecCommandExtensionContext(
                execCommand.getTarget(),
                cmd, execSession
        ));
    }


    @Override
    public void execute() {
        resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        switch (NEnvs.of(execSession).getOsFamily()) {
            case WINDOWS: {
                return NTexts.of(session).ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NTexts.of(session).ofStyled(
                                "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return execCommand.getRunAs() + " " + NCmdLine.of(cmd).toString();
    }

    @Override
    public NSession getSession() {
        return session;
    }
}
