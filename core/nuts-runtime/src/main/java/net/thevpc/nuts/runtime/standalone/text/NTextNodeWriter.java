package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextTransformConfig;

public interface NTextNodeWriter {
    NTextTransformConfig getWriteConfiguration();

    NTextNodeWriter setWriteConfiguration(NTextTransformConfig config);

    void writeNode(NText node);

    void writeRaw(byte[] buf, int off, int len);

    void writeRaw(char[] buf, int off, int len);

    boolean flush();

}
