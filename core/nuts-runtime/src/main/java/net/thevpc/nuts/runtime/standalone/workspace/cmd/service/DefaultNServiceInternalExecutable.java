/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.service;

import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.command.NExecutionException;
import net.thevpc.nuts.command.NInstallSvcCmd;
import net.thevpc.nuts.runtime.standalone.app.util.NAppUtils;
import net.thevpc.nuts.runtime.standalone.util.ExtraApiUtils;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.exec.local.internal.DefaultInternalNExecutableCommand;
import net.thevpc.nuts.text.NMsg;

import java.util.List;

/**
 *
 * @author thevpc
 */
public class DefaultNServiceInternalExecutable extends DefaultInternalNExecutableCommand {

    public DefaultNServiceInternalExecutable(String[] args, NExec execCommand, List<String> executorOptions) {
        super("service", args, execCommand, executorOptions);
    }

    @Override
    public int execute() {
        boolean dry = ExtraApiUtils.asBoolean(getExecCommand().dry());
        if (dry) {
            dryExecute();
            return NExecutionException.SUCCESS;
        }
        if (NAppUtils.processHelpOptions(args)) {
            showDefaultHelp();
            return NExecutionException.SUCCESS;
        }
        NCmdLine cmline = NCmdLine.of(args);
        if (!cmline.isEmpty()) {
            String cmd = cmline.peek().get().image();
            switch (cmd) {
                case "install": {
                    NInstallSvcCmd c = NInstallSvcCmd.of();
                    c.configure(false, args);
                    c.install();
                    return NExecutionException.SUCCESS;
                }
                case "uninstall": {
                    NInstallSvcCmd c = NInstallSvcCmd.of();
                    c.configure(false, args);
                    c.uninstall();
                    return NExecutionException.SUCCESS;
                }
            }
        }
        throw new NExecutionException(NMsg.ofC("missing service sub-command (install, uninstall)"), 1);
    }
}
