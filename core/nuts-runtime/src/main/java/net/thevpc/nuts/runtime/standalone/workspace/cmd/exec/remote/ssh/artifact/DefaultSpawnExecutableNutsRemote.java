/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.remote.ssh.artifact;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.executor.AbstractSyncIProcessExecHelper;
import net.thevpc.nuts.runtime.standalone.util.collections.CoreCollectionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.AbstractNExecutableCommand;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.DefaultNExecCommandExtensionContext;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultSpawnExecutableNutsRemote extends AbstractNExecutableCommand {

    NDefinition def;
    String[] cmd;
    List<String> executorOptions;
    NConnexionString connexionString;
    private boolean showCommand = false;
    private NExecCommandExtension commExec;
    NExecInput in;
    NExecOutput out;
    NExecOutput err;


    public DefaultSpawnExecutableNutsRemote(NExecCommandExtension commExec, NDefinition def, String[] cmd,
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
                return runOnce(cmd, getSession());
            }
        };
    }




    private int runOnce(String[] cmd, NSession session) {
        int e;
        try (DefaultNExecCommandExtensionContext d = new DefaultNExecCommandExtensionContext(
                getExecCommand().getTarget(),
                cmd, getSession(),
                in,
                out,
                err,
                getExecCommand()
        )) {
            return commExec.exec(d);
        } catch (IOException ex) {
            throw new NExecutionException(session, NMsg.ofC("command failed :%s", ex), ex);
        }
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
