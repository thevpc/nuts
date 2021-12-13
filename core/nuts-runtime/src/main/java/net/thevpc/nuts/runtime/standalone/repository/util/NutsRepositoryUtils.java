package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.NutsAddRepositoryOptions;
import net.thevpc.nuts.NutsRepositoryRef;

public class NutsRepositoryUtils {
    public static NutsRepositoryRef optionsToRef(NutsAddRepositoryOptions options) {
        return new NutsRepositoryRef()
                .setEnabled(options.isEnabled())
                .setFailSafe(options.isFailSafe())
                .setName(options.getName())
                .setLocation(options.getLocation())
                .setDeployWeight(options.getDeployWeight());
    }

    public static NutsAddRepositoryOptions refToOptions(NutsRepositoryRef ref) {
        return new NutsAddRepositoryOptions()
                .setEnabled(ref.isEnabled())
                .setFailSafe(ref.isFailSafe())
                .setName(ref.getName())
                .setLocation(ref.getLocation())
                .setDeployWeight(ref.getDeployWeight())
                .setTemporary(false);
    }
}
