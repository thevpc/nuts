package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsTextNodeWriteConfiguration;
import net.thevpc.nuts.NutsText;

public interface NutsTextNodeWriter {
    NutsTextNodeWriteConfiguration getWriteConfiguration();

    NutsTextNodeWriter setWriteConfiguration(NutsTextNodeWriteConfiguration config);

    void writeNode(NutsText node);

    void writeRaw(byte[] buf, int off, int len);

    boolean flush();

}
