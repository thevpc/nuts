/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.alias;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.AbstractNutsSettingsSubCommand;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 *
 * @author thevpc
 */
public class NutsSettingsAliasSubCommand extends AbstractNutsSettingsSubCommand {
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
            command = ws.commandLine().create(a.getCommand()).toString();
            executionOptions = ws.commandLine().create(a.getExecutorOptions()).toString();
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

    @Override
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsSession session) {
        if (cmdLine.next("list aliases") != null) {
            cmdLine.setCommandName("settings list aliases");
            List<String> toList=new ArrayList<>();
            while (cmdLine.hasNext()) {
                if (!cmdLine.peek().isOption()) {
                    NutsArgument a = cmdLine.next();
                    toList.add(a.toString());
                } else {
                    cmdLine.unexpectedArgument();
                }
            }
            if (cmdLine.isExecMode()) {
                List<NutsWorkspaceCustomCommand> r = session.commands().findAllCommands()
                        .stream()
                        .filter(new Predicate<NutsWorkspaceCustomCommand>() {
                            @Override
                            public boolean test(NutsWorkspaceCustomCommand nutsWorkspaceCommandAlias) {
                                if(toList.isEmpty()){
                                    return true;
                                }
                                for (String s : toList) {
                                    if(s.contains("*")){
                                        if(Pattern.compile(s.replace("*",".*")).matcher(nutsWorkspaceCommandAlias.getName()).matches()){
                                            return true;
                                        }
                                    }else{
                                        if(s.equals(nutsWorkspaceCommandAlias.getName())){
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
                    session.formats().props(
                                    r.stream().collect(
                                            Collectors.toMap(
                                                    NutsWorkspaceCustomCommand::getName,
                                                    x -> session.commandLine().create(x.getCommand()).toString(),
                                                    (x, y) -> {
                                                        throw new NutsIllegalArgumentException(session,NutsMessage.cstyle("duplicate %s",x));
                                                    },
                                                    //preserve order
                                                    LinkedHashMap::new
                                            ))
                            ).println();
                } else {
                    session.formats().object(
                            r.stream().map(x -> new AliasInfo(x, session)).collect(Collectors.toList())
                    ).println();
                }
            }
            return true;
        } else if (cmdLine.next("remove alias") != null) {
            if (cmdLine.isExecMode()) {
                while (cmdLine.hasNext()) {
                    session.commands().removeCommand(cmdLine.next().toString());
                }
                session.config().save();
            }
            return true;
        } else if (cmdLine.next("add alias") != null) {
            if (cmdLine.isExecMode()) {
                String n = null;
                LinkedHashMap<String, AliasInfo> toAdd = new LinkedHashMap<>();
                while (cmdLine.hasNext()) {
                    if (!cmdLine.peek().isOption()) {
                        NutsArgument a = cmdLine.next();
                        if (a.isKeyValue()) {
                            if (n != null) {
                                cmdLine.pushBack(a);
                                cmdLine.unexpectedArgument();
                            }
                            toAdd.put(a.getKey().getString(), new AliasInfo(a.getKey().getString(), a.getValue().getString(), null, null, null));
                        } else {
                            if (n == null) {
                                n = a.toString();
                            } else {
                                toAdd.put(n, new AliasInfo(n, a.toString(), null, null, null));
                                n = null;
                            }
                        }
                    } else {
                        cmdLine.unexpectedArgument();
                    }
                }
                if (toAdd.isEmpty()) {
                    cmdLine.required();
                }
                for (AliasInfo value : toAdd.values()) {
                    session.commands()
                            .addCommand(
                            new NutsCommandConfig()
                                    .setCommand(session.commandLine().parse(value.command).toStringArray())
                                    .setName(value.name)
                                    .setExecutorOptions(session.commandLine().parse(value.executionOptions).toStringArray())
                             );
                }
                session.config().save();
            }
            return true;
        }
        return false;
    }

}
