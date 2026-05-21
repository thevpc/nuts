package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.command.NCommandConfig;
import net.thevpc.nuts.command.NCommandFactoryConfig;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NWorkspaceCmdFactory;
import net.thevpc.nuts.platform.NShellFamily;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class CommandNWorkspaceCommandFactory implements NWorkspaceCmdFactory {

    private int priority = 10;
    private String factoryId;
    private String[] findCommand;
    private String[] execCommand;
    private String[] listCommand;

    public CommandNWorkspaceCommandFactory() {
    }

    public void configure(NCommandFactoryConfig config) {
        factoryId = config.factoryId();
        factoryId = "command";
        priority = config.priority();
        if (priority <= 0) {
            priority = 1;
        }
        Map<String, String> p = config.parameters();
        if (p != null) {
            findCommand = validateCommand(p.get("find"));
            execCommand = validateCommand(p.get("exec"));
            String slistCommand = p.get("list");
            listCommand = slistCommand == null ? new String[0] : NCmdLine.of(slistCommand, NShellFamily.BASH).expandSimpleOptions(false).toStringArray();
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
        String[] commandArr = NCmdLine.of(command, NShellFamily.BASH).expandSimpleOptions(false).toStringArray();
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
    public int priority() {
        return priority;
    }

    @Override
    public String factoryId() {
        return factoryId;
    }

    @Override
    public NCommandConfig findCommand(String name) {
        if (findCommand.length > 0 && execCommand.length > 0) {
            String[] fc = replaceParam(findCommand, name);
            String[] ec = replaceParam(execCommand, name);
            NExec exec = NExec.of().addCommand(fc)
                    //                        .setExecutorOptions("--show-command")
                    .grabAll()
                    .run();
            int r = exec.exitCode();
            if (r == 0) {
                return new NCommandConfig()
                        .factoryId(factoryId())
                        .owner(NId.get(ec[0]).get())
                        .name(name)
                        .command(Arrays.copyOfRange(ec, 1, ec.length));
            }
        }
        return null;
    }

    @Override
    public List<NCommandConfig> findCommands() {
        List<NCommandConfig> c = new ArrayList<>();
        if (listCommand.length > 0) {
            NExec b = NExec.of().addCommand(listCommand)
                    .grabAll();
            int r = b.exitCode();
            if (r == 0) {
                for (String s : b.grabbedOut().split("\n")) {
                    s = s.trim();
                    if (s.length() > 0) {
                        c.add(new NCommandConfig().name(s).command(new String[]{NConstants.Ids.NSH, s}));
                    }
                }
            }
        }
        return c;
    }
}
