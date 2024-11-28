package net.thevpc.nuts.core.test;

import net.thevpc.nuts.cmdline.NWorkspaceCmdLineParser;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;

import java.util.List;

public class Test51_WsArgs {

    //@Test
    public static void main(String[] args) {
        NCmdLine cmdLine = NCmdLine.of(
                new String[]{"--args", "--bot"}
        );
        while (cmdLine.hasNext()) {
            NOptional<List<NArg>> e = NWorkspaceCmdLineParser.nextNutsArgument(cmdLine, null);
            if (e.isPresent()) {
                System.out.println("ACCEPT " + e.get());
            } else {
                System.out.println("REJECT " + cmdLine.peek().get());
                cmdLine.skip();
            }
        }
    }
}
