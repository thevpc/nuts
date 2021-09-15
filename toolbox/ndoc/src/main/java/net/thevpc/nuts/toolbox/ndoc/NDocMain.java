package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDoclet;
import net.thevpc.nuts.toolbox.ndoc.doc.MdDocletConfig;

import java.util.ArrayList;
import java.util.List;

public class NDocMain implements NutsApplication {

    public static void main(String[] args) {
        NutsApplication.main(NDocMain.class, args);
    }

    @Override
    public void run(NutsApplicationContext appContext) {
        String[] args = appContext.getArguments();
        appContext.processCommandLine(new NutsCommandLineProcessor() {
            List<String> src = new ArrayList<>();
            List<String> pck = new ArrayList<>();
            String target;
            String backend;

            @Override
            public boolean onNextOption(NutsArgument option, NutsCommandLine commandline) {
                switch (option.getKey().getString()) {
                    case "-s":
                    case "--source": {
                        src.add(commandline.nextString().getValue().getString());
                        return true;
                    }
                    case "-t":
                    case "--target": {
                        target = commandline.nextString().getValue().getString();
                        return true;
                    }
                    case "-p":
                    case "--package": {
                        pck.add(commandline.nextString().getValue().getString());
                        return true;
                    }
                    case "-b":
                    case "--backend": {
                        backend= commandline.nextString().getValue().getString();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onNextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
                return false;
            }

            @Override
            public void onExec() {
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
        });
    }

}
