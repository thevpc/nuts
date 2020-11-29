//package net.thevpc.nuts.toolbox.nsh.cmds;
//
//import java.nio.file.Paths;
//import NutsDefinition;
//import NutsWorkspace;
//import net.thevpc.nuts.toolbox.nsh.AbstractNshCommand;
//import net.thevpc.nuts.toolbox.nsh.NutsCommandContext;
//import net.thevpc.nuts.app.options.NutsIdNonOption;
//import net.thevpc.common.commandline.Argument;
//import net.thevpc.common.commandline.FolderNonOption;
//
///**
// * Created by vpc on 1/7/17.
// */
//public class CheckoutCommand extends AbstractNshCommand {
//
//    public CheckoutCommand() {
//        super("checkout", DEFAULT_SUPPORT);
//    }
//
//    @Override
//    public int exec(String[] args, NutsCommandContext context) throws Exception {
//        NutsCommand cmdLine = cmdLine(args, context);
//
//        NutsCommandArg a;
//        while (cmdLine.hasNext()) {
//            if (context.configure(cmdLine)) {
//                //
//            } else {
//                String id = cmdLine.required().nextNonOption(new NutsIdNonOption("Nuts", context.getWorkspace())).getString();
//                String contentFile = cmdLine.required().nextNonOption(new FolderNonOption("folder")).getString();
//                if (cmdLine.isExecMode()) {
//                    NutsWorkspace ws = context.getWorkspace();
//                    NutsDefinition nf = ws.checkout(
//                            id,
//                            ws.io().path(contentFile),
//                            context.getSession()
//                    );
//                    context.out().printf("Folder ####%s#### initialized with ####%s####\n", contentFile, nf.getId());
//                }
//            }
//        }
//        return 0;
//    }
//}
