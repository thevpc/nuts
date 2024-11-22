package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDoclet;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDocletConfig;

import java.util.ArrayList;
import java.util.List;

public class NDocMain implements NApplication, NCmdLineRunner {
    private List<String> src = new ArrayList<>();
    private List<String> pck = new ArrayList<>();
    private String target;
    private String backend;

    public static void main(String[] args) {
        NApplication.main(NDocMain.class, args);
    }

    @Override
    public void run() {
        NSession session = NSession.of().get();
        NApp.of().processCmdLine(this);
    }

    @Override
    public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
        switch (option.key()) {
            case "-s":
            case "--source": {
                cmdLine.withNextEntry((v, r) -> src.add(v));
                return true;
            }
            case "-t":
            case "--target": {
                cmdLine.withNextEntry((v, r) -> target=v);
                return true;
            }
            case "-p":
            case "--package": {
                cmdLine.withNextEntry((v, r) -> pck.add(v));
                return true;
            }
            case "-b":
            case "--backend": {
                cmdLine.withNextEntry((v, r) -> backend=v);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
        return false;
    }

    @Override
    public void run(NCmdLine cmdLine, NCmdLineContext context) {
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
