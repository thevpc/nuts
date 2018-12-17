package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.common.javashell.ConsoleContext;
import net.vpc.common.javashell.cmds.Command;

public class ShellToNutsCommand extends AbstractNutsCommand {

    private final Command command;

    public ShellToNutsCommand(Command command) {
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
