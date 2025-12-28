package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.util.NOptional;

public interface JavaProvider {
    String getName();
    NOptional<NPath> resolveAndInstall(
            String product,
            int version,
            NOsFamily os,
            NArchFamily arch
    );
}
