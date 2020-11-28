package net.thevpc.nuts.runtime.format.text;

import net.thevpc.nuts.NutsTextNode;
import net.thevpc.nuts.NutsTextNodeWriteConfiguration;

public interface NutsTextNodeWriter {
    NutsTextNodeWriteConfiguration getWriteConfiguration();

    NutsTextNodeWriter setWriteConfiguration(NutsTextNodeWriteConfiguration config);

    void writeNode(NutsTextNode node);

    void writeRaw(byte[] buf, int off, int len);

    boolean flush();

}
