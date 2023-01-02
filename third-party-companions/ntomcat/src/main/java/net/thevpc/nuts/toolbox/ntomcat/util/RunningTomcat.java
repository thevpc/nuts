package net.thevpc.nuts.toolbox.ntomcat.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCommandLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPsInfo;

import java.util.Objects;

public class RunningTomcat {
    private String pid;
    private NPath home;
    private String base;
    private String argsLine;
    private NVersion version;

    public RunningTomcat(NPsInfo r, NSession session) {
        pid =r.getPid();
        argsLine=r.getCommandLine();
        NCommandLine cmdline = NCommandLine.parseSystem(r.getCommandLine(),session)
                .get(session).setExpandSimpleOptions(false);
        NArg a=null;
        while(cmdline.hasNext()){
            if((a=cmdline.nextString("-Dcatalina.home").orNull())!=null) {
                home = NPath.of(a.getStringValue().get(session),session);
            }else if((a=cmdline.nextString("-Dcatalina.base").orNull())!=null){
                base=a.getStringValue().get(session);
            }else{
                cmdline.skip();
            }
        }
        String b = TomcatUtils.getFolderCatalinaHomeVersion(home);
        this.version= b==null?null: NVersion.of(b).get();
    }

    public NVersion getVersion() {
        return version;
    }

    public String getPid() {
        return pid;
    }

    public NPath getHome() {
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
