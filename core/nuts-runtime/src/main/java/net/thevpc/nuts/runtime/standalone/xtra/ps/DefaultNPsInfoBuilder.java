package net.thevpc.nuts.runtime.standalone.xtra.ps;

import net.thevpc.nuts.io.NPsInfo;
import net.thevpc.nuts.io.NpsStatus;
import net.thevpc.nuts.io.NpsType;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringUtils;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultNPsInfoBuilder implements NBlankable {
    private String id;
    private String name;
    private String user;
    private String title;
    private String cmdLine;
    private double percentCpu;
    private double percentMem;
    private long virtualMemorySize;
    private long residentSetSize;
    private String terminal;
    private String[] cmdLineArgs;
    private Instant startTime;
    private long time;
    private NpsStatus status;
    private NpsType type;
    private Set<String> statusFlags;


    @Override
    public boolean isBlank() {
        if(!NBlankable.isBlank(id)){
            return false;
        }
        if(!NBlankable.isBlank(name)){
            return false;
        }
        if(!NBlankable.isBlank(user)){
            return false;
        }
        if(!NBlankable.isBlank(title)){
            return false;
        }
        if(!NBlankable.isBlank(cmdLine)){
            return false;
        }
        if(percentCpu!=0){
            return false;
        }
        if(percentMem!=0){
            return false;
        }
        if(residentSetSize!=0){
            return false;
        }
        if(virtualMemorySize!=0){
            return false;
        }
        if(time!=0){
            return false;
        }
        if(!NBlankable.isBlank(terminal)){
            return false;
        }
        if(cmdLineArgs!=null){
            return false;
        }
        if(startTime!=null){
            return false;
        }
        if(status!=null){
            return false;
        }
        if(type!=null){
            return false;
        }
        if(statusFlags!=null){
            return false;
        }
        return true;
    }

    public String getPid() {
        return id;
    }

    public NpsType getType() {
        return type;
    }

    public DefaultNPsInfoBuilder setType(NpsType type) {
        this.type = type;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }

    public String getCmdLine() {
        return cmdLine;
    }

    public String[] getCmdLineArgs() {
        return cmdLineArgs;
    }

    public String getId() {
        return id;
    }

    public NpsStatus getStatus() {
        return status;
    }

    public DefaultNPsInfoBuilder setStatus(NpsStatus status) {
        this.status = status;
        return this;
    }

    public Set<String> getStatusFlags() {
        return statusFlags;
    }

    public DefaultNPsInfoBuilder setStatusFlags(Set<String> statusFlags) {
        this.statusFlags = statusFlags;
        return this;
    }

    public DefaultNPsInfoBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public DefaultNPsInfoBuilder setName(String name) {
        this.name = name;
        return this;
    }

    public String getUser() {
        return user;
    }

    public DefaultNPsInfoBuilder setUser(String user) {
        this.user = user;
        return this;
    }

    public DefaultNPsInfoBuilder setTitle(String title) {
        this.title = title;
        return this;
    }

    public DefaultNPsInfoBuilder setCmdLine(String cmdLine) {
        this.cmdLine = cmdLine;
        return this;
    }

    public double getPercentCpu() {
        return percentCpu;
    }

    public DefaultNPsInfoBuilder setPercentCpu(double percentCpu) {
        this.percentCpu = percentCpu;
        return this;
    }

    public double getPercentMem() {
        return percentMem;
    }

    public DefaultNPsInfoBuilder setPercentMem(double percentMem) {
        this.percentMem = percentMem;
        return this;
    }

    public long getVirtualMemorySize() {
        return virtualMemorySize;
    }

    public DefaultNPsInfoBuilder setVirtualMemorySize(long virtualMemorySize) {
        this.virtualMemorySize = virtualMemorySize;
        return this;
    }

    public long getResidentSetSize() {
        return residentSetSize;
    }

    public DefaultNPsInfoBuilder setResidentSetSize(long residentSetSize) {
        this.residentSetSize = residentSetSize;
        return this;
    }

    public String getTerminal() {
        return terminal;
    }

    public DefaultNPsInfoBuilder setTerminal(String terminal) {
        this.terminal = terminal;
        return this;
    }

    public DefaultNPsInfoBuilder setCmdLineArgs(String[] cmdLineArgs) {
        this.cmdLineArgs = cmdLineArgs;
        return this;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public DefaultNPsInfoBuilder setStartTime(Instant startTime) {
        this.startTime = startTime;
        return this;
    }

    public long getTime() {
        return time;
    }

    public DefaultNPsInfoBuilder setTime(long time) {
        this.time = time;
        return this;
    }

    public NPsInfo build() {
        return new DefaultNPsInfo(id, NStringUtils.trimToNull(name), user, title, cmdLine, cmdLineArgs,
                status == null ? NpsStatus.UNKNOWN : status,
                type == null ? NpsType.UNKNOWN : type,
                statusFlags == null ? Collections.emptySet() : Collections.unmodifiableSet(new TreeSet<>(statusFlags.stream().filter(Objects::nonNull).map(String::trim).collect(Collectors.toSet()))),
                percentCpu, percentMem, virtualMemorySize, residentSetSize, terminal, startTime, time);
    }

    @Override
    public String toString() {
        return "DefaultNPsInfoBuilder{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", user='" + user + '\'' +
                ", title='" + title + '\'' +
                ", cmdLine='" + cmdLine + '\'' +
                ", percentCpu=" + percentCpu +
                ", percentMem=" + percentMem +
                ", virtualMemorySize=" + virtualMemorySize +
                ", residentSetSize=" + residentSetSize +
                ", terminal='" + terminal + '\'' +
                ", cmdLineArgs=" + Arrays.toString(cmdLineArgs) +
                ", startTime=" + startTime +
                ", time=" + time +
                ", status=" + status +
                ", flags=" + statusFlags +
                '}';
    }
}
