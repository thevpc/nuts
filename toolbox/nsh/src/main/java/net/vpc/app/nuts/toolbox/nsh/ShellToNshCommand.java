package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.common.javashell.JShellCommand;

public class ShellToNshCommand extends AbstractNshBuiltin {

    private final JShellCommand command;

    public ShellToNshCommand(JShellCommand command) {
        super(command.getName(), DEFAULT_SUPPORT);
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getName();
    }

    @Override
    public void exec(String[] args, NutsCommandContext context){
        command.exec(args, context);
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
