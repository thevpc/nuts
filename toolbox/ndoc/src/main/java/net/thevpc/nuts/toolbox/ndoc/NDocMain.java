package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
import net.thevpc.commons.md.doc.MdDoclet;
import net.thevpc.commons.md.doc.MdDocletConfig;

import java.util.ArrayList;
import java.util.List;

public class NDocMain extends NutsApplication {

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
            public boolean nextOption(NutsArgument option, NutsCommandLine commandline) {
                switch (option.getStringKey()) {
                    case "-s":
                    case "--source": {
                        src.add(commandline.nextString().getStringValue());
                        return true;
                    }
                    case "-t":
                    case "--target": {
                        target = commandline.nextString().getStringValue();
                        return true;
                    }
                    case "-p":
                    case "--package": {
                        pck.add(commandline.nextString().getStringValue());
                        return true;
                    }
                    case "-b":
                    case "--backend": {
                        backend= commandline.nextString().getStringValue();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine commandline) {
                return false;
            }

            @Override
            public void exec() {
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
