package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTextWriteConfiguration;
import net.thevpc.nuts.NutsText;

public interface NutsTextNodeWriter {
    NutsTextWriteConfiguration getWriteConfiguration();

    NutsTextNodeWriter setWriteConfiguration(NutsTextWriteConfiguration config);

    void writeNode(NutsText node);

    void writeRaw(byte[] buf, int off, int len);

    boolean flush();

}
