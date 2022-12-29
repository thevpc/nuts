package net.thevpc.nuts.toolbox.ndb.derby;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import net.thevpc.nuts.NApplicationContext;
import net.thevpc.nuts.io.NPs;
import net.thevpc.nuts.NSession;

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

    public static RunningDerby[] getRunningInstances(NApplicationContext context) {
        NSession session = context.getSession();
        return NPs.of(session)
                .type("java").getResultList()
                .stream().filter((p) -> p.getName().equals("org.apache.derby.drda.NetworkServerControl"))
                .map(x -> new RunningDerby(x, session)).toArray(RunningDerby[]::new);
    }

}
