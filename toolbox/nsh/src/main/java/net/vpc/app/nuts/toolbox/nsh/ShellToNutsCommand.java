package net.vpc.app.nuts.toolbox.nsh;

import net.vpc.common.javashell.JavaShellEvalContext;
import net.vpc.common.javashell.cmds.JavaShellInternalCmd;

public class ShellToNutsCommand extends AbstractNutsCommand {

    private final JavaShellInternalCmd command;

    public ShellToNutsCommand(JavaShellInternalCmd command) {
        super(command.getName(), DEFAULT_SUPPORT);
        this.command = command;
    }

    @Override
    public String getName() {
        return command.getName();
    }

    @Override
    public int exec(String[] args, NutsCommandContext context) throws Exception {
        return command.exec(args, (JavaShellEvalContext) context.getUserProperties().get(JavaShellEvalContext.class.getName()));
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
