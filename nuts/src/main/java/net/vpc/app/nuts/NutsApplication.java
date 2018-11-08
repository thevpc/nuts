package net.vpc.app.nuts;

public abstract class NutsApplication {

    public void launchAndExit(String[] args) {
        NutsWorkspace ws = null;
        try {
            ws = Nuts.openInheritedWorkspace(args);
            int r = launch(ws.getBootOptions().getApplicationArguments(), ws);
            System.exit(r);
        } catch (Exception ex) {
            boolean extraError = false;
            try {
                NutsSession s = ws.createSession();
                NutsFormattedPrintStream formattedErr = s.getTerminal().getFormattedErr();
                String m = ex.getMessage();
                if(m==null || m.isEmpty()){
                    m=ex.toString();
                }
                if(m==null || m.isEmpty()){
                    m=ex.getClass().getName();
                }
                formattedErr.printf("%s\n", m);
            } catch (Exception xex) {
                extraError = true;
            }
            if (extraError) {
                ex.printStackTrace();
            }
            System.exit(204);
        }
    }

    public int launch(String[] args) {
        NutsWorkspace ws = Nuts.openInheritedWorkspace(args);
        return launch(ws.getBootOptions().getApplicationArguments(), ws);
    }

    public abstract int launch(String[] args, NutsWorkspace ws);
}
