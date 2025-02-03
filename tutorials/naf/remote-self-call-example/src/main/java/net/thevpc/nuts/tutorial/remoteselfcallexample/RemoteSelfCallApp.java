package net.thevpc.nuts.tutorial.remoteselfcallexample;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLine;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * This example show how you can write a code that executes itself on another machine.
 *
 * When you run this app on machine '192.168.1.1' using these arguments
 * <pre>
 *  nuts net.thevpc.nuts.tutorial:remote-self-call-example --call-self --host=ssh://me:mypassword@192.168.1.89 something
 * </pre>
 * This will connect on ssh to 192.168.1.2 as user 'me' and password 'mypassword' and then wil run the following command
 * <pre>
 *  nuts net.thevpc.nuts.tutorial:remote-self-call-example --on-call-self from=192.168.1.1 something
 * </pre>
 * Then will grab the result and redirect it to 192.168.1.1.
 *
 *
 * @author vpc
 */
public class RemoteSelfCallApp implements NApplication {

    public static void main(String[] args) {
        new RemoteSelfCallApp().runAndExit(args);
    }

    private static class Options {
        String host;

        String command = "";
        List<String> nonOptions = new ArrayList<>();
    }

    @Override
    public void run() {
        NSession session = NSession.of();
        NCmdLine cmdLine = NApp.of().getCmdLine();
        log(NMsg.ofC("%s", cmdLine));
        Options options = new Options();
        while (cmdLine.hasNext()) {
            switch (cmdLine.peek().get().key()) {
                case "--host": {
                    cmdLine.withNextEntry((v, a) -> {
                        options.host = v;
                    });
                    break;
                }
                case "--on-call-self": {
                    cmdLine.withNextFlag((v, a) -> {
                        options.command = "on-call-self";
                    });
                    break;
                }
                case "--call-self": {
                    cmdLine.withNextFlag((v, a) -> {
                        options.command = "call-self";
                    });
                    break;
                }
                default: {
                    if (cmdLine.isNextNonOption()) {
                        options.nonOptions.add(cmdLine.next().get().toString());
                    }else {
                        session.configureLast(cmdLine);
                    }
                }
            }
        }
        if (cmdLine.isExecMode()) {
            log(NMsg.ofC("start"));
            log(NMsg.ofC("arguments-count : %s", options.nonOptions.size()));
            List<String> nonOptions = options.nonOptions;
            for (int i = 0; i < nonOptions.size(); i++) {
                String nonOption = nonOptions.get(i);
                log(NMsg.ofC("\t[%s] %s", i + 1, nonOption));
            }
            switch (options.command) {
                case "call-self": {
                    //call remote machine wi
                    if (NBlankable.isBlank(options.host)) {
                        cmdLine.throwMissingArgument("--host");
                    }
                    String e = NStringUtils.trim(
                            NExecCmd.of()
                                    // host is ion the form
                                    // ssh://user@machine
                                    .setTarget(options.host)
                                    .setCommand(
                                            NStringUtils.toStringOrEmpty(NApp.of().getId().orNull()),
                                            "--on-call-self"
                                    )
                                    .addCommand("from=" + NWorkspace.of().getHostName())
                                    .addCommand(options.nonOptions)
                                    .failFast()
                                    .getGrabbedAllString()
                    );
                    log(NMsg.ofC("received"));
                    NOut.println(e);
                    break;
                }
                case "on-call-self": {
                    log(NMsg.ofC("executing here!!"));
                    break;
                }
                default: {
                    cmdLine.throwMissingArgument("--call-self");
                }
            }
        }
    }

    private void log(NMsg m) {
        String hostName = NWorkspace.of().getHostName();
        NOut.println(NMsg.ofC("[%s] %s", hostName, m));
    }

}
