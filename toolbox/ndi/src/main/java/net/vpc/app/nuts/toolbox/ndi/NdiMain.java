package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.app.NutsApplication;
import net.vpc.app.nuts.app.NutsApplicationContext;

import java.io.IOException;

public class NdiMain extends NutsApplication {
    public static void main(String[] args) {
        new NdiMain().launch(args);
    }

    @Override
    public int launch(NutsApplicationContext appContext) {
        String[] args=appContext.getArgs();
        if (args.length > 0) {
            if (args[0].equals("in") || args[0].equals("install")) {
                LinuxNdi ndi = null;
                if (appContext.getWorkspace().getPlatformOs().getName().equals("linux")) {
                    ndi = new LinuxNdi(appContext);
                }
                if (ndi == null) {
                    throw new IllegalArgumentException("Platform not supported : " + appContext.getWorkspace().getPlatformOs());
                }
                boolean force = false;
                boolean forceAll = false;
                boolean fetch = false;
                for (int i = 1; i < args.length; i++) {
                    if (args[i].equals("-f")) {
                        force = true;
                    } else if (args[i].equals("-F")) {
                        forceAll = true;
                        force = true;
                    } else if (args[i].equals("-c")) {
                        fetch = true;
                    } else {
                        try {
                            ndi.createNutsScript(args[i], force, forceAll, fetch);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            return 0;
        } else {
            appContext.err().printf("Missing arguments\n");
            return 1;
        }
    }
}
