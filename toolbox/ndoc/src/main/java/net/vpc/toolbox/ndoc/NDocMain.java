package net.vpc.toolbox.ndoc;

import net.vpc.app.nuts.*;
import net.vpc.commons.md.doc.MdDoclet;
import net.vpc.commons.md.doc.MdDocletConfig;

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
            public boolean nextOption(NutsArgument option, NutsCommandLine cmdLine) {
                switch (option.getStringKey()) {
                    case "-s":
                    case "--source": {
                        src.add(cmdLine.nextString().getStringValue());
                        return true;
                    }
                    case "-t":
                    case "--target": {
                        target = cmdLine.nextString().getStringValue();
                        return true;
                    }
                    case "-p":
                    case "--package": {
                        pck.add(cmdLine.nextString().getStringValue());
                        return true;
                    }
                    case "-b":
                    case "--backend": {
                        backend=cmdLine.nextString().getStringValue();
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean nextNonOption(NutsArgument nonOption, NutsCommandLine cmdLine) {
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
