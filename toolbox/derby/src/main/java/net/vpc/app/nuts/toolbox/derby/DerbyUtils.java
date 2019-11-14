package net.vpc.app.nuts.toolbox.derby;

import net.vpc.app.nuts.NutsApplicationContext;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

public class DerbyUtils {
    public static RunningDerby[] getRunningInstances(NutsApplicationContext context) {
        try {
            return Arrays.stream(JpsUtils.getRunningJava(context, "org.apache.derby.drda.NetworkServerControl"))
                    .map(x -> new RunningDerby(x, context.workspace())).toArray(RunningDerby[]::new);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw new UncheckedIOException(ex);
        }
    }

}
