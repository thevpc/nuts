//package net.thevpc.nuts.toolbox.nsh;
//
//import NutsSession;
//import NutsWorkspace;
//import net.thevpc.javashell.ConsoleContext;
//import net.thevpc.javashell.cmds.Command;
//
//class NutsShellInternalCmd implements Command {
//
//    private final NshCommand ncommand;
//    private final NutsJavaShell component;
//
//    public NutsShellInternalCmd(NshCommand ncommand, NutsJavaShell component) {
//        this.ncommand = ncommand;
//        this.component = component;
//    }
//
//    @Override
//    public int exec(String[] command, ConsoleContext shell) throws Exception {
//        NutsJavaShellEvalContext ncontext = (NutsJavaShellEvalContext) shell;
//        NutsConsoleContext commandContext = ncontext.getCommandContext();
//        NutsConsoleContext context = component.getContext();
//        NutsSession session = context.getSession().copy();
//        NutsWorkspace workspace = context.getWorkspace();
//
//        session.setTerminal(ncontext.getTerminal().copy());
//        commandContext.setSession(session);
//        commandContext.setEnv(shell.env().env());
//        return ncommand.exec(command, new DefaultNutsCommandContext(commandContext,ncommand));
//    }
//
//    @Override
//    public String getHelp() {
//        return ncommand.getHelp();
//    }
//
//    @Override
//    public String getName() {
//        return ncommand.getName();
//    }
//
//    @Override
//    public String getHelpHeader() {
//        return ncommand.getHelpHeader();
//    }
//}
