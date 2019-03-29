/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.vpc.app.nuts.app;

import java.io.PrintStream;
import net.vpc.app.nuts.Nuts;
import net.vpc.app.nuts.NutsExecutionException;
import net.vpc.app.nuts.NutsWorkspace;

/**
 *
 * @author vpc
 */
public class NutsApplications {

    private NutsApplications() {
    }

    /**
     *
     * @param ex exception
     * @param args app arguments to check from if a '--verbose' or '--debug'
     * option is armed
     *
     * @param out
     * @return
     */
    public static int processThrowable(Throwable ex, String[] args, PrintStream out) {
        if (ex == null) {
            return 0;
        }
        int errorCode = 204;
        boolean showTrace = false;

        if (args != null) {
            for (String arg : args) {
                if (arg.startsWith("-")) {
                    if (arg.equals("--verbose") || arg.equals("--debug")) {
                        showTrace = true;
                        break;
                    }
                } else {
                    break;
                }
            }
        }
        if (ex instanceof NutsExecutionException) {
            NutsExecutionException ex2 = (NutsExecutionException) ex;
            if (ex2.getExitCode() == 0) {
                return 0;
            } else {
                errorCode = ex2.getExitCode();
            }
        }
        String m = ex.getMessage();
        if (m == null || m.length() < 5) {
            m = ex.toString();
        }
        out.println(m);
        if (showTrace) {
            ex.printStackTrace(out);
        }
        return (errorCode);
    }

    public static void runApplication(String[] args, NutsWorkspace ws, NutsApplicationListener listener) {
        long startTimeMillis = System.currentTimeMillis();
        if (ws == null) {
            ws = Nuts.openInheritedWorkspace(args);
        }
        NutsApplicationContext applicationContext = null;
        applicationContext = listener.createApplicationContext(ws, ws.getConfigManager().getOptions().getApplicationArguments());
        applicationContext.setStartTimeMillis(startTimeMillis);
        switch (applicationContext.getMode()) {
            case "launch":
            case "auto-complete": {
//                    if(!applicationContext.getWorkspace().isInstalled(applicationContext.getAppId(),false,applicationContext.getSession())){
//                        int i = onInstallApplication(applicationContext);
//                        if(i!=0){
//                            throw new NutsExecutionException("Unable to install "+applicationContext.getAppId(),i);
//                        }
//                        return i;
//                    }
                listener.onRunApplication(applicationContext);
                return;
            }
            case "on-install": {
                listener.onInstallApplication(applicationContext);
                return;
            }
            case "on-update": {
                listener.onUpdateApplication(applicationContext);
                return;
            }
            case "on-uninstall": {
                listener.onUninstallApplication(applicationContext);
                return;
            }
        }
        throw new NutsExecutionException("Unsupported execution mode " + applicationContext.getMode(), 204);
    }
}
