package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.util.NPathParts;

public class DefaultNCompressedPathHelper implements NCompressedPathHelper {
    @Override
    public NString toCompressedString(NPath base, NSession session) {
        return NPathParts.compressPath(base.toString(), session);
    }

}
