package net.thevpc.nuts.runtime.standalone.ntalk;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class NTalkUtils {
    public static void writeArray(byte[] msg, DataOutputStream s) throws IOException {
        s.writeInt(msg.length);
        s.write(msg);
    }

    public static byte[] readArray(DataInputStream s) throws IOException {
        int size = s.readInt();
        byte[] msg = new byte[size];
        s.readFully(msg);
        return msg;
    }
}
