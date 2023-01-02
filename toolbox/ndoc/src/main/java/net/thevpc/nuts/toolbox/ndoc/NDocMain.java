package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCommandLineContext;
import net.thevpc.nuts.cmdline.NCommandLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDoclet;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDocletConfig;

import java.util.ArrayList;
import java.util.List;

public class NDocMain implements NApplication, NCommandLineProcessor {
    private List<String> src = new ArrayList<>();
    private List<String> pck = new ArrayList<>();
    private String target;
    private String backend;

    public static void main(String[] args) {
        NApplication.main(NDocMain.class, args);
    }

    @Override
    public void run(NApplicationContext appContext) {
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NArg option, NCommandLine commandLine, NCommandLineContext context) {
        NSession session = commandLine.getSession();
        switch (option.key()) {
            case "-s":
            case "--source": {
                commandLine.withNextString((v, r, s) -> src.add(v));
                return true;
            }
            case "-t":
            case "--target": {
                commandLine.withNextString((v, r, s) -> target=v);
                return true;
            }
            case "-p":
            case "--package": {
                commandLine.withNextString((v, r, s) -> pck.add(v));
                return true;
            }
            case "-b":
            case "--backend": {
                commandLine.withNextString((v, r, s) -> backend=v);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NArg nonOption, NCommandLine commandLine, NCommandLineContext context) {
        return false;
    }

    @Override
    public void onCmdExec(NCommandLine commandLine, NCommandLineContext context) {
        if (src.isEmpty()) {
            src.add(".");
        }
        if (target == null) {
            this.target = ".";
        }
        new MdDoclet().start(new MdDocletConfig()
                .addSources(src)
                .addPackages(pck)
                .setTarget(target)
                .setBackend(backend)
        );
    }


}
