/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNSettingsSubCommand;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NMsg;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NSettingsAliasSubCommand extends AbstractNSettingsSubCommand {
    @Override
    public boolean exec(NCmdLine cmdLine, Boolean autoSave, NSession session) {
        if (cmdLine.next("list aliases").isPresent()) {
            cmdLine.setCommandName("settings list aliases");
            List<String> toList = new ArrayList<>();
            while (cmdLine.hasNext()) {
                if (!cmdLine.isNextOption()) {
                    NArg a = cmdLine.next().get(session);
                    toList.add(a.toString());
                } else {
                    cmdLine.throwUnexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                List<NCustomCmd> r = NCommands.of(session).findAllCommands()
                        .stream()
                        .filter(new Predicate<NCustomCmd>() {
                            @Override
                            public boolean test(NCustomCmd nutsWorkspaceCommandAlias) {
                                if (toList.isEmpty()) {
                                    return true;
                                }
                                for (String s : toList) {
                                    if (s.contains("*")) {
                                        if (Pattern.compile(s.replace("*", ".*")).matcher(nutsWorkspaceCommandAlias.getName()).matches()) {
                                            return true;
                                        }
                                    } else {
                                        if (s.equals(nutsWorkspaceCommandAlias.getName())) {
                                            return true;
                                        }
                                    }
                                }
                                return false;
                            }
                        })
                        .sorted((x, y) -> x.getName().compareTo(y.getName()))
                        .collect(Collectors.toList());
                if (session.isPlainOut()) {
                    NPropertiesFormat.of(session).setValue(
                            r.stream().collect(
                                    Collectors.toMap(
                                            NCustomCmd::getName,
                                            x -> NCmdLine.of(x.getCommand()).toString(),
                                            (x, y) -> {
                                                throw new NIllegalArgumentException(session, NMsg.ofC("duplicate %s", x));
                                            },
                                            //preserve order
                                            LinkedHashMap::new
                                    ))
                    ).println();
                } else {
                    session.out().println(
                            r.stream().map(x -> new AliasInfo(x, session)).collect(Collectors.toList())
                    );
                }
            }
            return true;
        } else if (cmdLine.next("remove alias").isPresent()) {
            if (cmdLine.isExecMode()) {
                while (cmdLine.hasNext()) {
                    NCommands.of(session).removeCommand(cmdLine.next().get(session).toString());
                }
                NConfigs.of(session).save();
            }
            return true;
        } else if (cmdLine.next("add alias").isPresent()) {
            if (cmdLine.isExecMode()) {
                String n = null;
                LinkedHashMap<String, AliasInfo> toAdd = new LinkedHashMap<>();
                while (cmdLine.hasNext()) {
                    if (!cmdLine.isNextOption()) {
                        NArg a = cmdLine.next().get(session);
                        if (a.isKeyValue()) {
                            if (n != null) {
                                cmdLine.pushBack(a);
                                cmdLine.throwUnexpectedArgument();
                            }
                            String[] cmdAndArgs = splitCmdAndExecArgs(a.getStringValue().get(session), session);
                            toAdd.put(a.key(), new AliasInfo(a.getKey().asString().get(session), cmdAndArgs[0], null, null, cmdAndArgs[1]));
                        } else {
                            if (n == null) {
                                n = a.toString();
                            } else {
                                String[] cmdAndArgs = splitCmdAndExecArgs(a.toString(), session);
                                toAdd.put(n, new AliasInfo(n, cmdAndArgs[0], null, null, cmdAndArgs[1]));
                                n = null;
                            }
                        }
                    } else {
                        cmdLine.throwUnexpectedArgument();
                    }
                }
                if (toAdd.isEmpty()) {
                    cmdLine.next().get(session);
                }
                for (AliasInfo value : toAdd.values()) {
                    NCommands.of(session)
                            .addCommand(
                                    new NCommandConfig()
                                            .setCommand(NCmdLine.of(value.command, NShellFamily.BASH, session).setExpandSimpleOptions(false).toStringArray())
                                            .setName(value.name)
                                            .setExecutorOptions(
                                                    NCmdLine.of(value.executionOptions, NShellFamily.BASH, session)
                                                            .setExpandSimpleOptions(false).toStringList())
                            );
                }
                NConfigs.of(session).save();
            }
            return true;
        }
        return false;
    }
    private String[] splitCmdAndExecArgs(String aliasValue, NSession session){
        NCmdLine cmdLine2 = NCmdLine.of(aliasValue, NShellFamily.BASH, session).setExpandSimpleOptions(false);
        List<String> executionOptions = new ArrayList<>();
        while (cmdLine2.hasNext()) {
            NArg r = cmdLine2.peek().get(session);
            if (r.isOption()) {
                executionOptions.add(cmdLine2.next().flatMap(NLiteral::asString).get(session));
            } else {
                break;
            }
        }
        if (executionOptions.isEmpty()) {
            return new String[]{aliasValue,null};
        } else {
            return new String[]{cmdLine2.toString(), NCmdLine.of(executionOptions.toArray(new String[0])).toString()};
        }
    }

    public static class AliasInfo {

        public String name;
        public String command;
        public String factoryId;
        public NId owner;
        public String executionOptions;

        public AliasInfo(String name, String command, String factoryId, NId owner, String executionOptions) {
            this.name = name;
            this.command = command;
            this.factoryId = factoryId;
            this.owner = owner;
            this.executionOptions = executionOptions;
        }

        public AliasInfo(NCustomCmd a, NSession session) {
            name = a.getName();
            command = NCmdLine.of(a.getCommand()).toString();
            executionOptions = NCmdLine.of(a.getExecutorOptions()).toString();
            factoryId = a.getFactoryId();
            owner = a.getOwner();
        }

        public String getName() {
            return name;
        }

        public String getCommand() {
            return command;
        }

        public String getFactoryId() {
            return factoryId;
        }

        public NId getOwner() {
            return owner;
        }

        public String getExecutionOptions() {
            return executionOptions;
        }

    }

}
