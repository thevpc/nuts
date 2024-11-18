/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.artifact;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.lib.common.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableInformationExt;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCmdExtensionContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.lib.common.str.NConnexionString;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultSpawnExecutableNutsRemote extends AbstractNExecutableInformationExt {

    NDefinition def;
    String[] cmd;
    // effective cmd (incudes def)
    String[] ecmd;
    List<String> executorOptions;
    NConnexionString connexionString;
    private boolean showCommand = false;
    private NExecCmdExtension commExec;
    NExecInput in;
    NExecOutput out;
    NExecOutput err;
    NWorkspace workspace;


    public DefaultSpawnExecutableNutsRemote(NWorkspace workspace,NExecCmdExtension commExec, NDefinition def, String[] cmd,
                                            List<String> executorOptions, NExecCmd execCommand,
                                            NExecInput in,
                                            NExecOutput out,
                                            NExecOutput err

    ) {
        super(workspace,def.getId().toString(),
                NCmdLine.of(cmd).toString(),
                NExecutableType.SYSTEM, execCommand);
        this.def = def;
        this.in = in;
        this.out = out;
        this.err = err;
        this.cmd = cmd;
        List<String> ecmdList = new ArrayList<>();
        if (def != null) {
            ecmdList.add(def.getId().toString());
        }
        ecmdList.addAll(Arrays.asList(cmd));
        ecmd = ecmdList.toArray(new String[0]);
        this.executorOptions = CoreCollectionUtils.nonNullList(executorOptions);
        this.commExec = commExec;
        NCmdLine cmdLine = NCmdLine.of(this.executorOptions);
        NSession session = workspace.currentSession();
        while (cmdLine.hasNext()) {
            NArg aa = cmdLine.peek().get();
            switch (aa.key()) {
                case "--show-command": {
                    cmdLine.withNextFlag((v, a) -> this.showCommand = (v));
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
        NSession session = workspace.currentSession();
        return new AbstractSyncIProcessExecHelper(workspace) {
            @Override
            public int exec() {
                return runOnce(ecmd, session);
            }
        };
    }


    private int runOnce(String[] cmd, NSession session) {
        int e;
        try (DefaultNExecCmdExtensionContext d = new DefaultNExecCmdExtensionContext(
                getExecCommand().getTarget(),
                cmd, session,
                in,
                out,
                err,
                getExecCommand()
        )) {
            return commExec.exec(d);
        } catch (IOException ex) {
            throw new NExecutionException(NMsg.ofC("command failed :%s", ex), ex);
        }
    }


    @Override
    public int execute() {
        return resolveExecHelper().exec();
    }


    @Override
    public NText getHelpText() {
        NSession session = workspace.currentSession();
        switch (NEnvs.of().getOsFamily()) {
            case WINDOWS: {
                return NTexts.of().ofStyled(
                        "No help available. Try " + getName() + " /help",
                        NTextStyle.error()
                );
            }
            default: {
                return
                        NTexts.of().ofStyled(
                                "No help available. Try 'man " + getName() + "' or '" + getName() + " --help'",
                                NTextStyle.error()
                        );
            }
        }
    }

    @Override
    public String toString() {
        return getExecCommand().getRunAs() + " " + NCmdLine.of(ecmd);
    }

}
