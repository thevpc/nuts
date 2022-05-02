package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.io.util.NutsPathParts;

public class DefaultNutsCompressedPathHelper implements NutsCompressedPathHelper {
    @Override
    public NutsString toCompressedString(NutsPath base, NutsSession session) {
        return NutsPathParts.compressPath(base.toString(), session);
    }

}
