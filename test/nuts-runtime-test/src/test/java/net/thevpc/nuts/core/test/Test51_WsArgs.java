package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NMsg;
import net.thevpc.nuts.NOptional;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.boot.NWorkspaceCmdLineParser;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.core.test.utils.TestUtils;

import java.util.List;

public class Test51_WsArgs {

    //@Test
    public static void main(String[] args) {
        NCmdLine cmdLine = NCmdLine.of(
                new String[]{"--args", "--bot"}
        );
        while (cmdLine.hasNext()) {
            NOptional<List<NArg>> e = NWorkspaceCmdLineParser.nextNutsArgument(cmdLine, null, null);
            if (e.isPresent()) {
                System.out.println("ACCEPT " + e.get());
            } else {
                System.out.println("REJECT " + cmdLine.peek().get());
                cmdLine.skip();
            }
        }
    }
}
