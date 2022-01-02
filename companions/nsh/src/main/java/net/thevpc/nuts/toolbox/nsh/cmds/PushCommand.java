//package net.thevpc.nuts.toolbox.nsh.cmds;
//
///**
// * Created by vpc on 1/7/17.
// */
//public class PushCommand extends AbstractNshCommand {
//
//    public PushCommand() {
//        super("push", DEFAULT_SUPPORT);
//    }
//
//    public int exec(String[] args, NutsCommandContext context) throws Exception {
//        NutsCommand cmdLine = cmdLine(args, context);
//        String repo = null;
//        NutsPushCommand push = context.getWorkspace().push().setSession(context.getSession());
//        NutsCommandArg a;
//        do {
//            if (context.configure(cmdLine)) {
//                //
//            } else if (cmdLine.skipOnce("--repo", "-r")) {
//                repo = cmdLine.required().nextNonOption(new RepositoryNonOption("Repository", context.getWorkspace())).getString();
//            } else if (cmdLine.skipOnce("--force", "-f")) {
//                push.setForce(true);
//            } else {
//                String id = cmdLine.required().nextNonOption(new DefaultNonOption("NewNutsId")).toString();
//                if (cmdLine.isExecMode()) {
//                    push.id(id).run();
//                    context.out().printf("%s pushed successfully\n", id);
//                }
//            }
//        } while (cmdLine.hasNext());
//        return 0;
//    }
//}
