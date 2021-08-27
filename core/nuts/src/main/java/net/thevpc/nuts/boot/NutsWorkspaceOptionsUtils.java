package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsWorkspaceOptions;

class NutsWorkspaceOptionsUtils {
    static boolean isBootOptional(String name, NutsWorkspaceOptions woptions) {
        for (String property : woptions.getProperties()) {
            PrivateNutsCommandLine.ArgumentImpl a = new PrivateNutsCommandLine.ArgumentImpl(property, '=');
            if (a.getStringKey().equals("boot-" + name)) {
                return true;
            }
        }
        return false;
    }

    static boolean isBootOptional(NutsWorkspaceOptions woptions) {
        for (String property : woptions.getProperties()) {
            PrivateNutsCommandLine.ArgumentImpl a = new PrivateNutsCommandLine.ArgumentImpl(property, '=');
            if (a.getStringKey().equals("boot-optional")) {
                return true;
            }
        }
        return false;
    }
}
