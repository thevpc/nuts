package net.vpc.toolbox.tomcat.util;

import net.vpc.app.nuts.NutsArgument;
import net.vpc.app.nuts.NutsCommandLine;
import net.vpc.app.nuts.NutsProcessInfo;
import net.vpc.app.nuts.NutsWorkspace;

import java.util.Objects;

public class RunningTomcat {
    private String pid;
    private String home;
    private String base;
    private String argsLine;

    public RunningTomcat(NutsProcessInfo r, NutsWorkspace ws) {
        pid =r.getPid();
        argsLine=r.getCommandLine();
        NutsCommandLine cmdline = ws.commandLine().parse(r.getCommandLine()).setExpandSimpleOptions(false);
        NutsArgument a=null;
        while(cmdline.hasNext()){
            if((a=cmdline.nextString("-Dcatalina.home"))!=null) {
                home = a.getStringValue();
            }else if((a=cmdline.nextString("-Dcatalina.base"))!=null){
                base=a.getStringValue();
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

    public String getBase() {
        return base;
    }

    public String getArgsLine() {
        return argsLine;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        RunningTomcat that = (RunningTomcat) o;
        return Objects.equals(pid, that.pid) &&
                Objects.equals(home, that.home) &&
                Objects.equals(base, that.base) &&
                Objects.equals(argsLine, that.argsLine);
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid, home, base, argsLine);
    }

    @Override
    public String toString() {
        return "RunningTomcat{" +
                "id='" + pid + '\'' +
                ", home='" + home + '\'' +
                ", base='" + base + '\'' +
                ", argsLine='" + argsLine + '\'' +
                '}';
    }
}
