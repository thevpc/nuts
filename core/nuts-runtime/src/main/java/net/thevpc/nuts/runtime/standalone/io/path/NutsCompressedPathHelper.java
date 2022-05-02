package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;

public interface NutsCompressedPathHelper {
    NutsString toCompressedString(NutsPath path, NutsSession session);
}
