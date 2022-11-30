package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NutsArgument;
import net.thevpc.nuts.cmdline.NutsCommandLine;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDoclet;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDocletConfig;

import java.util.ArrayList;
import java.util.List;

public class NDocMain implements NutsApplication, NutsAppCmdProcessor {
    private List<String> src = new ArrayList<>();
    private List<String> pck = new ArrayList<>();
    private String target;
    private String backend;

    public static void main(String[] args) {
        NutsApplication.main(NDocMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        appContext.processCommandLine(this);
    }

    @Override
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandLine, NutsApplicationContext context) {
        NutsSession session = context.getSession();
        switch (option.getKey().asString().get(session)) {
            case "-s":
            case "--source": {
                commandLine.withNextString((v, r, s) -> src.add(v), session);
                return true;
            }
            case "-t":
            case "--target": {
                commandLine.withNextString((v, r, s) -> target=v, session);
                return true;
            }
            case "-p":
            case "--package": {
                commandLine.withNextString((v, r, s) -> pck.add(v), session);
                return true;
            }
            case "-b":
            case "--backend": {
                commandLine.withNextString((v, r, s) -> backend=v, session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandLine, NutsApplicationContext context) {
        return false;
    }

    @Override
    public void onCmdExec(NutsCommandLine commandLine, NutsApplicationContext context) {
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
