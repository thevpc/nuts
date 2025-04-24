package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.io.NpsStatus;
import net.thevpc.nuts.io.NpsType;
import net.thevpc.nuts.util.NLiteral;

import java.time.Instant;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

public class DefaultNPsInfo implements NPsInfo {
    private String id;
    private String name;
    private String user;
    private String title;
    private String cmdLine;
    private NpsStatus status;
    private Set<String> statusFlags;
    private double percentCpu;
    private double percentMem;
    private long virtualMemorySize;
    private long residentSetSize;
    private String terminal;
    private String[] cmdLineArgs;
    private Instant startTime;
    private long time;
    private NpsType type;

    public DefaultNPsInfo(String id, String name, String user, String title, String cmdLine, String[] cmdLineArgs,
                          NpsStatus status, NpsType type,Set<String> statusFlags, double percentCpu, double percentMem, long virtualMemorySize, long residentSetSize, String terminal, Instant startTime, long time) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.title = title;
        this.cmdLine = cmdLine;
        this.percentCpu = percentCpu;
        this.percentMem = percentMem;
        this.virtualMemorySize = virtualMemorySize;
        this.residentSetSize = residentSetSize;
        this.terminal = terminal;
        this.cmdLineArgs = cmdLineArgs;
        this.startTime = startTime;
        this.time = time;
        this.status = status;
        this.statusFlags = statusFlags;
        this.type = type;
    }

    public NpsType getType() {
        return type;
    }

    @Override
    public NpsStatus getStatus() {
        return status;
    }

    @Override
    public Set<String> getStatusFlags() {
        return statusFlags;
    }

    @Override
    public String getPid() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getCmdLine() {
        return cmdLine;
    }

    @Override
    public String[] getCmdLineArgs() {
        return cmdLineArgs;
    }

    @Override
    public String getUser() {
        return user;
    }

    @Override
    public double getPercentCpu() {
        return percentCpu;
    }

    @Override
    public double getPercentMem() {
        return percentMem;
    }

    @Override
    public long getVirtualMemorySize() {
        return virtualMemorySize;
    }

    @Override
    public long getResidentSetSize() {
        return residentSetSize;
    }

    @Override
    public String getTerminal() {
        return terminal;
    }

    @Override
    public Instant getStartTime() {
        return startTime;
    }

    @Override
    public long getTime() {
        return time;
    }

    @Override
    public String toString() {
        return "NPsInfo{" +
                "id=" + NLiteral.of(id).toStringLiteral()  +
                ", name=" + NLiteral.of(name).toStringLiteral()  +
                ", user=" + NLiteral.of(user).toStringLiteral()  +
                ", title=" + NLiteral.of(title).toStringLiteral()  +
                ", cmdLine=" + NLiteral.of(cmdLine).toStringLiteral()  +
                ", type=" + type +
                ", status=" + status +
                ", flags=" + statusFlags +
                ", percentCpu=" + percentCpu +
                ", percentMem=" + percentMem +
                ", virtualMemorySize=" + virtualMemorySize +
                ", residentSetSize=" + residentSetSize +
                ", terminal=" + NLiteral.of(terminal).toStringLiteral()  +
                ", cmdLineArgs=" + Arrays.toString(cmdLineArgs) +
                ", startTime=" + startTime +
                ", time=" + time +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DefaultNPsInfo that = (DefaultNPsInfo) o;
        return Double.compare(percentCpu, that.percentCpu) == 0 && Double.compare(percentMem, that.percentMem) == 0 && virtualMemorySize == that.virtualMemorySize && residentSetSize == that.residentSetSize && time == that.time && Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(user, that.user) && Objects.equals(title, that.title) && Objects.equals(cmdLine, that.cmdLine) && status == that.status && Objects.equals(statusFlags, that.statusFlags) && Objects.equals(terminal, that.terminal) && Objects.deepEquals(cmdLineArgs, that.cmdLineArgs) && Objects.equals(startTime, that.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, user, title, cmdLine, status, statusFlags, percentCpu, percentMem, virtualMemorySize, residentSetSize, terminal, Arrays.hashCode(cmdLineArgs), startTime, time);
    }
}
