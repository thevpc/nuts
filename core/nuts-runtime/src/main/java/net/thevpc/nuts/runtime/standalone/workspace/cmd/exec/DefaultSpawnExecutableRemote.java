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
import net.thevpc.nuts.io.NIO;
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
    NConnexionString connexionString;
    private boolean showCommand = false;
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
                NSession session = r.getSession();
                NConnexionString targetConnexion = NConnexionString.of(r.getExecCommand().getTarget()).get().copy()
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

                String sharedNutsApiJar = storeLocationLib + "/" + NIdUtils.resolveJarPath(session.getWorkspace().getApiId());
                String sharedNutsApiDesc = storeLocationLib + "/" + NIdUtils.resolveDescPath(session.getWorkspace().getApiId());
                String sharedNutsRuntimeJar = storeLocationLib + "/" + NIdUtils.resolveJarPath(session.getWorkspace().getRuntimeId());
                String sharedNutsRuntimeDesc = storeLocationLib + "/" + NIdUtils.resolveDescPath(session.getWorkspace().getRuntimeId());

                if (NBlankable.isBlank(nutsJar)) {
                    if (!NPath.of(targetConnexion.copy()
                            .setPath(sharedNutsApiJar)
                            .toString(), session).exists()) {
                        NDefinition e = NFetchCommand.ofNutsApi(session)
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
                        NDefinition e = NFetchCommand.ofNutsRuntime(session)
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
                                        List<String> executorOptions, NExecCommand execCommand) {
        super(cmd[0],
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM,execCommand);
        this.cmd = cmd;
        this.def = def;
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
        Map<String, RemoteConnexionStringInfo> m = getSession().getOrComputeWorkspaceProperty(RemoteConnexionStringInfo.class.getName() + "Map",
                s -> new HashMap<>()
        );
        RemoteConnexionStringInfo k = m.computeIfAbsent(getExecCommand().getTarget(), v -> new RemoteConnexionStringInfo());
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
        NExecOutput out = NExecOutput.ofGrabMem();
        NExecOutput err = NExecOutput.ofGrabMem();
        int e = commExec.exec(new DefaultNExecCommandExtensionContext(
                getExecCommand().getTarget(),
                cmd, getSession(),
                NExecInput.ofStream(NIO.of(getSession()).ofNullInputStream()),
                out,
                err
        ));
        if (e != 0) {
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
                getExecCommand().getIn(),
                getExecCommand().getOut(),
                getExecCommand().getErr()
        ));
    }


    @Override
    public void execute() {
        resolveExecHelper().exec();
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
        return getExecCommand().getRunAs() + " " + NCmdLine.of(cmd).toString();
    }

}
