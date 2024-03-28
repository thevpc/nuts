package net.thevpc.nuts.toolbox.ndb.sql.derby;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPsInfo;

import java.util.Objects;

public class RunningDerby {
    private String pid;
    private String home;
    private String argsLine;

    public RunningDerby(NPsInfo r, NSession session) {
        pid =r.getPid();
        argsLine=r.getCmdLine();
        NCmdLine cmdline = NCmdLine.parseSystem(r.getCmdLine(),session).get(session).setExpandSimpleOptions(false);
        NArg a=null;
        while(cmdline.hasNext()){
            if((a=cmdline.nextEntry("-Dderby.system.home").orNull())!=null) {
                home = a.getStringValue().get(session);
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
