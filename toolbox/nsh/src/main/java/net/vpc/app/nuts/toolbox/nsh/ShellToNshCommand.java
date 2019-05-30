package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.common.javashell.cmds.JavaShellCommand;

public class ShellToNshCommand extends AbstractNshCommand {

    private final JavaShellCommand command;

    public ShellToNshCommand(JavaShellCommand command) {
        super(command.getName(), DEFAULT_SUPPORT);
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getName();
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        return command.exec(args, context);
    }

    @Override
    public String getHelp() {
        return command.getHelp();
    }

    @Override
    public String getHelpHeader() {
        return command.getHelpHeader();
    }
}
