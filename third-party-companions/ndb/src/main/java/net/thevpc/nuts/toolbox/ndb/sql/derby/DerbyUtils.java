package net.thevpc.nuts.toolbox.ndb.sql.derby;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.io.NPs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

public class DerbyUtils {

    public static byte[] loadByteArray(InputStream is) {
        try {
            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[1024];
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }

            buffer.flush();
            return buffer.toByteArray();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    public static RunningDerby[] getRunningInstances(NSession session) {
        return NPs.of()
                .type("java").getResultList()
                .stream().filter((p) -> p.getName().equals("org.apache.derby.drda.NetworkServerControl"))
                .map(x -> new RunningDerby(x, session)).toArray(RunningDerby[]::new);
    }

}
