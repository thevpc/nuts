/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.toolbox.nadmin.config;

import java.util.List;
import java.util.stream.Collectors;
import net.vpc.app.nuts.NutsAddOptions;
import net.vpc.app.nuts.NutsApplicationContext;
import net.vpc.app.nuts.NutsCommandAliasConfig;
import net.vpc.app.nuts.toolbox.nadmin.NAdminMain;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsRemoveOptions;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.NutsWorkspaceCommandAlias;

/**
 *
 * @author vpc
 */
public class AliasNAdminSubCommand extends AbstractNAdminSubCommand {

    public static class AliasInfo {

        public String name;
        public String command;

        public AliasInfo(NutsWorkspaceCommandAlias a, NutsWorkspace ws) {
            name = a.getName();
            command = ws.commandLine().setArgs(a.getCommand()).toString();
        }
    }

    @Override
    public boolean exec(NutsCommandLine cmdLine, NAdminMain config, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("list aliases") != null) {
            cmdLine.setCommandName("nadmin list aliases").unexpectedArgument();
            if (cmdLine.isExecMode()) {
                List<NutsCommandAliasConfig> r = context.getWorkspace().config().findCommandAliases()
                        .stream().map(x -> {
                            NutsCommandAliasConfig cc = new NutsCommandAliasConfig();
                            cc.setName(x.getName());
                            cc.setCommand(x.getCommand());
                            return cc;
                        })
                        .collect(Collectors.toList());
                if (context.session().isPlainOut()) {
                    context.getWorkspace().props()
                            .session(context.getSession())
                            .model(
                                    r.stream().collect(Collectors.toMap(
                                            NutsCommandAliasConfig::getName,
                                            x -> context.getWorkspace().commandLine().setArgs(x.getCommand()).toString()
                                    ))
                            ).println();
                } else {
                    context.workspace().object().session(context.session()).value(r).println();
                }
            }
            return true;
        } else if (cmdLine.next("remove alias") != null) {
            if (cmdLine.isExecMode()) {
                while (cmdLine.hasNext()) {
                    context.getWorkspace().config().removeCommandAlias(cmdLine.next().toString(), new NutsRemoveOptions()
                            .session(context.getSession()));
                }
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        } else if (cmdLine.next("add alias") != null) {
            if (cmdLine.isExecMode()) {
                String n = null;
                String c = null;
                while (cmdLine.hasNext()) {
                    if (n == null) {
                        n = cmdLine.next().toString();
                    } else if (c == null) {
                        c = cmdLine.next().toString();
                    } else {
                        cmdLine.unexpectedArgument();
                    }
                }
                if (n == null || c == null) {
                    cmdLine.required();
                }
                context.getWorkspace().config().addCommandAlias(
                        new NutsCommandAliasConfig()
                                .setCommand(context.getWorkspace().commandLine().parseLine(c).toArray())
                                .setName(n),
                        new NutsAddOptions().session(context.getSession()));
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getSupportLevel(Object criteria) {
        return DEFAULT_SUPPORT;
    }

}
