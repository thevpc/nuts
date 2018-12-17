package net.vpc.app.nuts.app;

import net.vpc.app.nuts.*;

import java.io.PrintStream;
import java.util.Arrays;

public abstract class NutsApplication {

    public void launchAndExit(String[] args) {
        long startTimeMillis = System.currentTimeMillis();
        NutsWorkspace ws = null;
        try {
            ws = Nuts.openInheritedWorkspace(args);
            NutsApplicationContext applicationContext = createApplicationContext(ws);
            applicationContext.setStartTimeMillis(startTimeMillis);
            int r = launch(applicationContext);
            System.exit(r);
        } catch (Exception ex) {
            int errorCode = 204;
            if (ex instanceof NutsExecutionException) {
                NutsExecutionException ex2 = (NutsExecutionException) ex;
                if (ex2.getExitCode() == 0) {
                    System.exit(0);
                    return;
                } else {
                    errorCode = ex2.getExitCode();
                }
            } else {
                ex.printStackTrace();
            }
            boolean extraError = false;
            try {
                NutsSession s = ws == null ? null : ws.createSession();
                PrintStream formattedErr = s == null ? System.err : s.getTerminal().getFormattedErr();
                String m = ex.getMessage();
                if (m == null || m.isEmpty()) {
                    m = ex.toString();
                }
                if (m == null || m.isEmpty()) {
                    m = ex.getClass().getName();
                }
                formattedErr.printf("%s\n", m);
            } catch (Exception xex) {
                extraError = true;
            }
            if (extraError) {
                ex.printStackTrace();
            }
            System.exit(errorCode);
        }
    }

    public int launch(String[] args) {
        long startTimeMillis = System.currentTimeMillis();
        NutsWorkspace ws = Nuts.openInheritedWorkspace(args);
        try {
            NutsApplicationContext applicationContext = createApplicationContext(ws);
            applicationContext.setStartTimeMillis(startTimeMillis);
            return launch(applicationContext);
        }catch (NutsExecutionException ex){
            if(ex.getExitCode()==0){
                return 0;
            }
            throw ex;
        }
    }

    protected NutsApplicationContext createApplicationContext(NutsWorkspace ws) {
        return new NutsApplicationContext(ws, getClass(), null);
    }

    public abstract int launch(NutsApplicationContext appContext);
}
