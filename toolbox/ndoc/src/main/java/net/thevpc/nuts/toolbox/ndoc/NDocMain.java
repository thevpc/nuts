package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
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
    public boolean onCmdNextOption(NutsArgument option, NutsCommandLine commandline,NutsApplicationContext context) {
        NutsSession session = context.getSession();
        switch (option.getKey().asString().get(session)) {
            case "-s":
            case "--source": {
                src.add(commandline.nextStringValueLiteral().get(session));
                return true;
            }
            case "-t":
            case "--target": {
                target = commandline.nextStringValueLiteral().get(session);
                return true;
            }
            case "-p":
            case "--package": {
                pck.add(commandline.nextStringValueLiteral().get(session));
                return true;
            }
            case "-b":
            case "--backend": {
                backend= commandline.nextStringValueLiteral().get(session);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onCmdNextNonOption(NutsArgument nonOption, NutsCommandLine commandline,NutsApplicationContext context) {
        return false;
    }

    @Override
    public void onCmdExec(NutsCommandLine commandline, NutsApplicationContext context) {
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
