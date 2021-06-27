package net.thevpc.nuts.toolbox.ncode.bundles;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class _Utils {

    public static byte[] toByteArray(InputStream in) throws IOException {

        ByteArrayOutputStream os = new ByteArrayOutputStream();

        byte[] buffer = new byte[1024];
        int len;

        // read bytes from the input stream and store them in buffer
        while ((len = in.read(buffer)) != -1) {
            // write bytes from the buffer into output stream
            os.write(buffer, 0, len);
        }

        return os.toByteArray();
    }
}
