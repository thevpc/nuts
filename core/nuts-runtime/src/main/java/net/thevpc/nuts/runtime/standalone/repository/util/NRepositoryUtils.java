package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NRepositoryLocation;

public class NRepositoryUtils {
    public static NRepositoryRef optionsToRef(NAddRepositoryOptions options) {
        return new NRepositoryRef()
                .setEnabled(options.isEnabled())
                .setFailSafe(options.isFailSafe())
                .setName(options.getName())
                .setLocation(options.getLocation())
                .setDeployWeight(options.getDeployWeight());
    }

    public static NAddRepositoryOptions refToOptions(NRepositoryRef ref) {
        return new NAddRepositoryOptions()
                .setEnabled(ref.isEnabled())
                .setFailSafe(ref.isFailSafe())
                .setName(ref.getName())
                .setLocation(ref.getLocation())
                .setDeployWeight(ref.getDeployWeight())
                .setTemporary(false);
    }

    public static String getRepoType(NRepositoryConfig conf) {
        if(conf!=null){
            NRepositoryLocation loc = conf.getLocation();
            if(loc!=null) {
                if (!NBlankable.isBlank(loc.getLocationType())) {
                    return loc.getLocationType();
                }
            }
        }
        return null;
    }
}
