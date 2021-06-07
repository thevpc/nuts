package net.thevpc.nuts.runtime.core.format.text;

import net.thevpc.nuts.NutsText;
import net.thevpc.nuts.NutsTextWriteConfiguration;

public interface NutsTextNodeWriter {
    NutsTextWriteConfiguration getWriteConfiguration();

    NutsTextNodeWriter setWriteConfiguration(NutsTextWriteConfiguration config);

    void writeNode(NutsText node);

    void writeRaw(byte[] buf, int off, int len);

    void writeRaw(char[] buf, int off, int len);

    boolean flush();

}
