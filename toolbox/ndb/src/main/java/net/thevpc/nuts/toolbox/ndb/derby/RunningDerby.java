package net.thevpc.nuts.toolbox.ndb.derby;

import net.thevpc.nuts.*;

import java.util.Objects;

public class RunningDerby {
    private String pid;
    private String home;
    private String argsLine;

    public RunningDerby(NutsProcessInfo r, NutsSession session) {
        pid =r.getPid();
        argsLine=r.getCommandLine();
        NutsCommandLine cmdline = NutsCommandLine.of(r.getCommandLine(),session).setExpandSimpleOptions(false);
        NutsArgument a=null;
        while(cmdline.hasNext()){
            if((a=cmdline.nextString("-Dderby.system.home"))!=null) {
                home = a.getValue().getString();
            }else{
                cmdline.skip();
            }
        }
    }


    public String getPid() {
        return pid;
    }

    public String getHome() {
        return home;
    }

    public String getArgsLine() {
        return argsLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningDerby that = (RunningDerby) o;
        return Objects.equals(pid, that.pid) &&
                Objects.equals(home, that.home) &&
                Objects.equals(argsLine, that.argsLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, home, argsLine);
    }

    @Override
    public String toString() {
        return "RunningDerby{" +
                "id='" + pid + '\'' +
                ", home='" + home + '\'' +
                ", argsLine='" + argsLine + '\'' +
                '}';
    }
}
