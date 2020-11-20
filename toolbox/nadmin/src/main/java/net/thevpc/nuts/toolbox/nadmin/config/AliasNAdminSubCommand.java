/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.toolbox.nadmin.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import net.thevpc.nuts.NutsAddOptions;
import net.thevpc.nuts.NutsApplicationContext;
import net.thevpc.nuts.NutsArgument;
import net.thevpc.nuts.NutsCommandAliasConfig;
import net.thevpc.nuts.NutsCommandLine;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsRemoveOptions;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.NutsWorkspaceCommandAlias;

/**
 *
 * @author vpc
 */
public class AliasNAdminSubCommand extends AbstractNAdminSubCommand {
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

        public AliasInfo(NutsWorkspaceCommandAlias a, NutsWorkspace ws) {
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
    public boolean exec(NutsCommandLine cmdLine, Boolean autoSave, NutsApplicationContext context) {
        if (cmdLine.next("list aliases") != null) {
            cmdLine.setCommandName("nadmin list aliases");
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
                List<NutsWorkspaceCommandAlias> r = context.getWorkspace().aliases().findAll(context.getSession())
                        .stream()
                        .filter(new Predicate<NutsWorkspaceCommandAlias>() {
                            @Override
                            public boolean test(NutsWorkspaceCommandAlias nutsWorkspaceCommandAlias) {
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
                if (context.getSession().isPlainOut()) {
                    context.getWorkspace().formats().props()
                            .setSession(context.getSession())
                            .setValue(
                                    r.stream().collect(
                                            Collectors.toMap(
                                                    NutsWorkspaceCommandAlias::getName,
                                                    x -> context.getWorkspace().commandLine().create(x.getCommand()).toString(),
                                                    (x, y) -> {
                                                        throw new IllegalArgumentException("Duplicate " + x);
                                                    },
                                                    //preserve order
                                                    LinkedHashMap::new
                                            ))
                            ).println();
                } else {
                    context.getSession().formatObject(
                            r.stream().map(x -> new AliasInfo(x, context.getWorkspace())).collect(Collectors.toList())
                    ).println();
                }
            }
            return true;
        } else if (cmdLine.next("remove alias") != null) {
            if (cmdLine.isExecMode()) {
                while (cmdLine.hasNext()) {
                    context.getWorkspace().aliases().remove(cmdLine.next().toString(), new NutsRemoveOptions()
                            .setSession(context.getSession()));
                }
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
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
                            toAdd.put(a.getStringKey(), new AliasInfo(a.getStringKey(), a.getStringValue(), null, null, null));
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
                    context.getWorkspace().aliases().add(
                            new NutsCommandAliasConfig()
                                    .setCommand(context.getWorkspace().commandLine().parse(value.command).toStringArray())
                                    .setName(value.name)
                                    .setExecutorOptions(context.getWorkspace().commandLine().parse(value.executionOptions).toStringArray()),
                             new NutsAddOptions().setSession(context.getSession()));
                }
                trySave(context, context.getWorkspace(), null, autoSave, cmdLine);
            }
            return true;
        }
        return false;
    }

}
