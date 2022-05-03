/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.AbstractNutsSettingsSubCommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class NutsSettingsAliasSubCommand extends AbstractNutsSettingsSubCommand {
    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("list aliases").isPresent()) {
            cmdLine.setCommandName("settings list aliases");
            List<String> toList = new ArrayList<>();
            while (cmdLine.hasNext()) {
                if (!cmdLine.isNextOption()) {
                    NutsArgument a = cmdLine.next().get(session);
                    toList.add(a.toString());
                } else {
                    cmdLine.throwUnexpectedArgument(session);
                }
            }
            if (cmdLine.isExecMode()) {
                List<NutsWorkspaceCustomCommand> r = session.commands().findAllCommands()
                        .stream()
                        .filter(new Predicate<NutsWorkspaceCustomCommand>() {
                            @Override
                            public boolean test(NutsWorkspaceCustomCommand nutsWorkspaceCommandAlias) {
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
                    NutsPropertiesFormat.of(session).setValue(
                            r.stream().collect(
                                    Collectors.toMap(
                                            NutsWorkspaceCustomCommand::getName,
                                            x -> NutsCommandLine.of(x.getCommand()).toString(),
                                            (x, y) -> {
                                                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("duplicate %s", x));
                                            },
                                            //preserve order
                                            LinkedHashMap::new
                                    ))
                    ).println();
                } else {
                    session.out().printlnf(
                            r.stream().map(x -> new AliasInfo(x, session)).collect(Collectors.toList())
                    );
                }
            }
            return true;
        } else if (cmdLine.next("remove alias").isPresent()) {
            if (cmdLine.isExecMode()) {
                while (cmdLine.hasNext()) {
                    session.commands().removeCommand(cmdLine.next().get(session).toString());
                }
                session.config().save();
            }
            return true;
        } else if (cmdLine.next("add alias").isPresent()) {
            if (cmdLine.isExecMode()) {
                String n = null;
                LinkedHashMap<String, AliasInfo> toAdd = new LinkedHashMap<>();
                while (cmdLine.hasNext()) {
                    if (!cmdLine.isNextOption()) {
                        NutsArgument a = cmdLine.next().get(session);
                        if (a.isKeyValue()) {
                            if (n != null) {
                                cmdLine.pushBack(a, session);
                                cmdLine.throwUnexpectedArgument(session);
                            }
                            String[] cmdAndArgs = splitCmdAndExecArgs(a.getStringValue().get(session), session);
                            toAdd.put(a.getKey().asString().get(session), new AliasInfo(a.getKey().asString().get(session), cmdAndArgs[0], null, null, cmdAndArgs[1]));
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
                        cmdLine.throwUnexpectedArgument(session);
                    }
                }
                if (toAdd.isEmpty()) {
                    cmdLine.next().get(session);
                }
                for (AliasInfo value : toAdd.values()) {
                    session.commands()
                            .addCommand(
                                    new NutsCommandConfig()
                                            .setCommand(NutsCommandLine.of(value.command,NutsShellFamily.BASH, session).setExpandSimpleOptions(false).toStringArray())
                                            .setName(value.name)
                                            .setExecutorOptions(
                                                    NutsCommandLine.of(value.executionOptions,NutsShellFamily.BASH, session)
                                                            .setExpandSimpleOptions(false).toStringList())
                            );
                }
                session.config().save();
            }
            return true;
        }
        return false;
    }
    private String[] splitCmdAndExecArgs(String aliasValue,NutsSession session){
        NutsCommandLine cmdLine2 = NutsCommandLine.of(aliasValue, NutsShellFamily.BASH, session).setExpandSimpleOptions(false);
        List<String> executionOptions = new ArrayList<>();
        while (cmdLine2.hasNext()) {
            NutsArgument r = cmdLine2.peek().get(session);
            if (r.isOption()) {
                executionOptions.add(cmdLine2.next().flatMap(NutsValue::asString).get(session));
            } else {
                break;
            }
        }
        if (executionOptions.isEmpty()) {
            return new String[]{aliasValue,null};
        } else {
            return new String[]{cmdLine2.toString(),NutsCommandLine.of(executionOptions.toArray(new String[0])).toString()};
        }
    }

    public static class AliasInfo {

        public String name;
        public String command;
        public String factoryId;
        public NutsId owner;
        public String executionOptions;

        public AliasInfo(String name, String command, String factoryId, NutsId owner, String executionOptions) {
            this.name = name;
            this.command = command;
            this.factoryId = factoryId;
            this.owner = owner;
            this.executionOptions = executionOptions;
        }

        public AliasInfo(NutsWorkspaceCustomCommand a, NutsSession ws) {
            name = a.getName();
            command = NutsCommandLine.of(a.getCommand()).toString();
            executionOptions = NutsCommandLine.of(a.getExecutorOptions()).toString();
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

        public NutsId getOwner() {
            return owner;
        }

        public String getExecutionOptions() {
            return executionOptions;
        }

    }

}
