/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.ssh;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.executor.system.NSysExecUtils;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommandExtensionContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.*;

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
                    RemoteConnexionStringInfo k = RemoteConnexionStringInfo.of(getExecCommand().getTarget(), getSession());
                    cmd2.add("-r=" + k.getStoreLocationCacheRepoSSH(commExec, getSession()).getLocation());
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
        RemoteConnexionStringInfo k = RemoteConnexionStringInfo.of(getExecCommand().getTarget(), session);
        int count = 0;
        for (NDependency d : def.getDependencies().get()) {
            NId id = d.toId();
            //just ignore nuts base deps!
            if (
                    !id.equalsLongId(session.getWorkspace().getApiId())
                            && !id.equalsLongId(session.getWorkspace().getRuntimeId())
            ) {
                k.copyId(id, k.getStoreLocationCacheRepoSSH(commExec, getSession()), session, null);
                count++;
            }
        }
        if (true) {
            k.copyId(def.getId(), k.getStoreLocationCacheRepoSSH(commExec, getSession()), session, null);
            count++;
        }
        return count > 0;
    }

    private String[] resolveNutsExecutableCommand() {
        NSession session1 = getSession();
        RemoteConnexionStringInfo k = RemoteConnexionStringInfo.of(getExecCommand().getTarget(), session1);
        k.tryUpdate(commExec, session1);
        ArrayList<String> cmd = new ArrayList<>();
        cmd.add(k.getJavaCommand(commExec, session1));
        cmd.add("-jar");
        cmd.add(k.getNutsJar(commExec, session1));
        cmd.add("-w");
        cmd.add(k.getWorkspaceName(commExec, session1));

        NSession session = getExecCommand().getSession();
        String[] remoteCommand = k.buildEffectiveCommand(cmd.toArray(new String[0]), getExecCommand().getRunAs(), commExec, session);
        return remoteCommand;
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
