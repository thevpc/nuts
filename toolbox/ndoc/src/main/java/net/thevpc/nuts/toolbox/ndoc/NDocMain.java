package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineProcessor;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDoclet;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDocletConfig;

import java.util.ArrayList;
import java.util.List;

public class NDocMain implements NApplication, NCmdLineProcessor {
    private List<String> src = new ArrayList<>();
    private List<String> pck = new ArrayList<>();
    private String target;
    private String backend;

    public static void main(String[] args) {
        NApplication.main(NDocMain.class, args);
    }

    @Override
    public void run(NSession session) {
        session.processAppCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NArg option, NCmdLine commandLine, NCmdLineContext context) {
        NSession session = commandLine.getSession();
        switch (option.key()) {
            case "-s":
            case "--source": {
                commandLine.withNextEntry((v, r, s) -> src.add(v));
                return true;
            }
            case "-t":
            case "--target": {
                commandLine.withNextEntry((v, r, s) -> target=v);
                return true;
            }
            case "-p":
            case "--package": {
                commandLine.withNextEntry((v, r, s) -> pck.add(v));
                return true;
            }
            case "-b":
            case "--backend": {
                commandLine.withNextEntry((v, r, s) -> backend=v);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NArg nonOption, NCmdLine commandLine, NCmdLineContext context) {
        return false;
    }

    @Override
    public void onCmdExec(NCmdLine commandLine, NCmdLineContext context) {
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
