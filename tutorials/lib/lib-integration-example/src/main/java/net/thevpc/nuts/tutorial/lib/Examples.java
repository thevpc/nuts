package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;

public class Examples {

    public static void main(String[] args) {
        NSession session = Nuts.openWorkspace("-ZyS");
        new ExamplesOfCp().executeAll(session);
        new ExamplesOfZip().executeAll(session);
        new ExamplesOfExec().executeAll(session);
        new ExamplesOfSearch().executeAll(session);
    }
}
