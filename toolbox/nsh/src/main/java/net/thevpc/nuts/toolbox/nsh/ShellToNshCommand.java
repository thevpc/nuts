package net.thevpc.nuts.toolbox.nsh;

import net.thevpc.jshell.JShellBuiltin;

public class ShellToNshCommand extends AbstractNshBuiltin {

    private final JShellBuiltin command;

    public ShellToNshCommand(JShellBuiltin command) {
        super(command.getName(), DEFAULT_SUPPORT);
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getName();
    }

    @Override
    public void exec(String[] args, NshExecutionContext context) {
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
