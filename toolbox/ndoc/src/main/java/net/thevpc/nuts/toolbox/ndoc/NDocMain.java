package net.thevpc.nuts.toolbox.ndoc;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NCmdLineContext;
import net.thevpc.nuts.cmdline.NCmdLineRunner;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.lib.doc.javadoc.MdDoclet;
import net.thevpc.nuts.lib.doc.javadoc.MdDocletConfig;

import java.util.ArrayList;
import java.util.List;

public class NDocMain implements NApplication, NCmdLineRunner {

    public static void main(String[] args) {
        NApplication.main(NDocMain.class, args);
    }

    @Override
    public void run() {
        NSession session = NSession.get().get();
        NApp.of().processCmdLine(this);
    }

    @Override
    public boolean nextOption(NArg option, NCmdLine cmdLine, NCmdLineContext context) {
        switch (option.key()) {

        }
        return false;
    }

    @Override
    public boolean nextNonOption(NArg nonOption, NCmdLine cmdLine, NCmdLineContext context) {
        return false;
    }

    @Override
    public void run(NCmdLine cmdLine, NCmdLineContext context) {

    }


}
