package net.thevpc.nuts.tutorial.lib;

import net.thevpc.nuts.*;

public class Examples {

    public static void main(String[] args) {
        Nuts.openWorkspace("-ZyS");
        new ExamplesOfCp().executeAll();
        new ExamplesOfZip().executeAll();
        new ExamplesOfExec().executeAll();
        new ExamplesOfSearch().executeAll();
    }
}
