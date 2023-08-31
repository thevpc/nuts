package net.thevpc.nuts.runtime.standalone.io.path;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.text.NString;

public interface NCompressedPathHelper {
    NString toCompressedString(NPath path, NSession session);
}
