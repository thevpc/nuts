package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsWorkspaceOptions;

class PrivateNutsUtilWorkspaceOptions {
    static boolean isBootOptional(String name, NutsWorkspaceOptions woptions) {
        for (String property : woptions.getProperties()) {
            PrivateNutsArgumentImpl a = new PrivateNutsArgumentImpl(property, '=');
            if (a.getKey().getString().equals("boot-" + name)) {
                return true;
            }
        }
        return false;
    }

    static boolean isBootOptional(NutsWorkspaceOptions woptions) {
        for (String property : woptions.getProperties()) {
            PrivateNutsArgumentImpl a = new PrivateNutsArgumentImpl(property, '=');
            if (a.getKey().getString().equals("boot-optional")) {
                return true;
            }
        }
        return false;
    }
}
