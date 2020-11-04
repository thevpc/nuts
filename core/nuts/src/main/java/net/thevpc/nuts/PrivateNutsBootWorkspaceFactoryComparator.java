package net.thevpc.nuts;

import java.util.Comparator;

/**
 * 
 * @author vpc
 * @category Internal
 */
final class PrivateNutsBootWorkspaceFactoryComparator implements Comparator<NutsBootWorkspaceFactory> {

    private final NutsWorkspaceOptions options;

    public PrivateNutsBootWorkspaceFactoryComparator(NutsWorkspaceOptions options) {
        this.options = options;
    }

    @Override
    public int compare(NutsBootWorkspaceFactory o1, NutsBootWorkspaceFactory o2) {
        //sort by reverse order!
        return Integer.compare(o2.getBootSupportLevel(options), o1.getBootSupportLevel(options));
    }
}
