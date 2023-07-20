package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandNWorkspaceCommandFactory implements NWorkspaceCommandFactory {

    private int priority = 10;
    private String factoryId;
    private String[] findCommand;
    private String[] execCommand;
    private String[] listCommand;
    private NSession session;

    public CommandNWorkspaceCommandFactory(NSession session) {
        this.session = session;
    }

    public void configure(NCommandFactoryConfig config) {
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
            listCommand = slistCommand == null ? new String[0] : NCmdLine.of(slistCommand, NShellFamily.BASH, session).setExpandSimpleOptions(false).toStringArray();
            if (listCommand.length > 0 && !listCommand[0].contains(":")) {
                listCommand = new String[0];
            }
        }
    }

    private String[] replaceParam(String[] command, String name) {
        String[] command2 = Arrays.copyOf(command, command.length);
        for (int i = 0; i < command2.length; i++) {
            if (command2[i].equals("%n")) {
                command2[i] = name;
            }
        }
        return command2;
    }

    private String[] validateCommand(String command) {
        if (command == null) {
            return new String[0];
        }
        String[] commandArr = NCmdLine.of(command, NShellFamily.BASH, session).setExpandSimpleOptions(false).toStringArray();
        if (commandArr.length == 0) {
            return commandArr;
        }
        boolean found = false;
        for (String s : commandArr) {
            if (s.equals("%n")) {
                found = true;
                break;
            }
        }
        if (!found) {
            commandArr = Arrays.copyOf(commandArr, commandArr.length + 1);
            commandArr[commandArr.length - 1] = "%n";
        }
        if (commandArr.length > 0 && !commandArr[0].contains(":")) {
            commandArr = new String[0];
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
    public NCommandConfig findCommand(String name, NSession session) {
        if (findCommand.length > 0 && execCommand.length > 0) {
            String[] fc = replaceParam(findCommand, name);
            String[] ec = replaceParam(execCommand, name);
            NExecCommand exec = NExecCommand.of(session).addCommand(fc)
                    //                        .setExecutorOptions("--show-command")
                    .grabOutputString()
                    .run();
            int r = exec.getResult();
            if (r == 0) {
                return new NCommandConfig()
                        .setFactoryId(getFactoryId())
                        .setOwner(NId.of(ec[0]).get(session))
                        .setName(name)
                        .setCommand(Arrays.copyOfRange(ec, 1, ec.length));
            }
        }
        return null;
    }

    @Override
    public List<NCommandConfig> findCommands(NSession session) {
        List<NCommandConfig> c = new ArrayList<>();
        if (listCommand.length > 0) {
            NExecCommand b = NExecCommand.of(session).addCommand(listCommand)
                    .redirectErrorStream()
                    .grabOutputString();
            int r = b.getResult();
            if (r == 0) {
                for (String s : b.getOutputString().split("\n")) {
                    s = s.trim();
                    if (s.length() > 0) {
                        c.add(new NCommandConfig().setName(s).setCommand(new String[]{"nsh", s}));
                    }
                }
            }
        }
        return c;
    }
}
