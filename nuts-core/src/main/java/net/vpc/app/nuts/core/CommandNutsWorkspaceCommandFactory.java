package net.vpc.app.nuts.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.core.util.CoreNutsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class CommandNutsWorkspaceCommandFactory implements NutsWorkspaceCommandFactory {
    private int priority = 10;
    private String factoryId;
    private String[] findCommand;
    private String[] execCommand;
    private String[] listCommand;
    private NutsWorkspace ws;

    public CommandNutsWorkspaceCommandFactory(NutsWorkspace ws) {
        this.ws = ws;
    }

    public void configure(NutsCommandAliasFactoryConfig config) {
        factoryId = config.getFactoryId();
        factoryId = "command";
        priority = config.getPriority();
        if (priority <= 0) {
            priority = 1;
        }
        Map<String, String> p = config.getParameters();
        if (p != null) {
            findCommand = validateCommand(p.get("find"));
            execCommand = validateCommand(p.get("exec"));
            String slistCommand = p.get("list");
            listCommand = slistCommand == null ? new String[0] : ws.parser().parseCommandLine(slistCommand).toArray();
            if(listCommand.length>0 && !listCommand[0].contains(":")){
                listCommand=new String[0];
            }
        }
    }
    private String[] replaceParam(String[] command,String name){
        String[] command2= Arrays.copyOf(command,command.length);
        for (int i = 0; i < command2.length; i++) {
            if(command2[i].equals("%n")){
                command2[i]=name;
            }
        }
        return command2;
    }

    private String[] validateCommand(String command){
        if(command==null){
            return new String[0];
        }
        String[] commandArr= ws.parser().parseCommandLine(command).toArray();
        if(commandArr.length==0){
            return commandArr;
        }
        boolean found=false;
        for (String s : commandArr) {
            if(s.equals("%n")){
                found=true;
                break;
            }
        }
        if(!found){
            commandArr=Arrays.copyOf(commandArr,commandArr.length+1);
            commandArr[commandArr.length-1]="%n";
        }
        if(commandArr.length>0 && !commandArr[0].contains(":")){
            commandArr=new String[0];
        }
        return commandArr;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public String getFactoryId() {
        return factoryId;
    }

    @Override
    public NutsCommandAliasConfig findCommand(String name, NutsWorkspace workspace) {
        if(findCommand.length>0 && execCommand.length>0){
            String[] fc = replaceParam(findCommand, name);
            String[] ec = replaceParam(execCommand, name);
            NutsExecCommand exec = workspace.exec().command(fc)
//                        .setExecutorOptions("--show-command")
                    .redirectErrorStream()
                    .grabOutputString()
                    .run();
            int r = exec.getResult();
            if (r == 0) {
                return new NutsCommandAliasConfig()
                        .setFactoryId(getFactoryId())
                        .setOwner(CoreNutsUtils.parseNutsId(ec[0]))
                        .setName(name)
                        .setCommand(Arrays.copyOfRange(ec,1,ec.length))
                        ;
            }
        }
        return null;
    }

    @Override
    public List<NutsCommandAliasConfig> findCommands(NutsWorkspace workspace) {
        List<NutsCommandAliasConfig> c = new ArrayList<>();
        if(listCommand.length>0) {
            NutsExecCommand b = workspace.exec().command(listCommand)
                    .redirectErrorStream()
                    .grabOutputString();
            int r = b.run().getResult();
            if (r == 0) {
                for (String s : b.getOutputString().split("\n")) {
                    s = s.trim();
                    if (s.length() > 0) {
                        c.add(new NutsCommandAliasConfig().setName(s).setCommand(new String[]{"nsh", s}));
                    }
                }
            }
        }
        return c;
    }
}
