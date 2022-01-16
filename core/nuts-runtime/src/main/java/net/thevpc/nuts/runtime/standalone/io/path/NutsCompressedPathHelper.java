package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;

public interface NutsCompressedPathHelper {
    NutsString toCompressedString(NutsPath path, NutsSession session);
}
