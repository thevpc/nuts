package net.thevpc.nuts.toolbox.derby;

import net.thevpc.nuts.NutsApplicationContext;

public class DerbyUtils {
    public static RunningDerby[] getRunningInstances(NutsApplicationContext context) {
        return context.getWorkspace().io().ps().type("java").getResultList()
                .stream().filter((p) -> p.getName().equals("org.apache.derby.drda.NetworkServerControl"))
                .map(x -> new RunningDerby(x, context.getWorkspace())).toArray(RunningDerby[]::new);
    }

}
