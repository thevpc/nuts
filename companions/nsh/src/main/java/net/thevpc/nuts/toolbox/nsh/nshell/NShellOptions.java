package net.thevpc.nuts.toolbox.nsh.nshell;

import java.util.ArrayList;
import java.util.List;

public class NShellOptions {
    public boolean verbose = false;
    public boolean xtrace = false;
    public boolean errExit = false;
    public boolean restricted = false;
    public boolean interactive = false;
    public boolean effectiveInteractive = false;
    public boolean posix = false;
    public boolean exitAfterProcessingLines = false;

    /**
     * maximize compatibility with bash
     */
    public boolean bash = false;
    public boolean noRc = false;
    public boolean noProfile = false;
    public boolean noEditing = false;
    public boolean dumpPoStrings = false;
    public boolean dumpStrings = false;
    public boolean login = false;
    public boolean debugger = false;
    /**
     * -s option
     */
    public boolean stdInAndPos = false;
    public String rcFile;
    public boolean command;
    public List<String> commandArgs=new ArrayList<>();
    private String serviceName = null;
    private String startupScript;
    private String shutdownScript;
    private boolean version = false;
    private boolean help = false;
    private List<String> files = new ArrayList<>();

    public boolean isVerbose() {
        return verbose;
    }

    public NShellOptions setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isXtrace() {
        return xtrace;
    }

    public NShellOptions setXtrace(boolean xtrace) {
        this.xtrace = xtrace;
        return this;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public NShellOptions setRestricted(boolean restricted) {
        this.restricted = restricted;
        return this;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public NShellOptions setInteractive(boolean interactive) {
        this.interactive = interactive;
        return this;
    }

    public boolean isPosix() {
        return posix;
    }

    public NShellOptions setPosix(boolean posix) {
        this.posix = posix;
        return this;
    }

    public boolean isNoRc() {
        return noRc;
    }

    public NShellOptions setNoRc(boolean noRc) {
        this.noRc = noRc;
        return this;
    }

    public boolean isNoProfile() {
        return noProfile;
    }

    public NShellOptions setNoProfile(boolean noProfile) {
        this.noProfile = noProfile;
        return this;
    }

    public boolean isNoEditing() {
        return noEditing;
    }

    public NShellOptions setNoEditing(boolean noEditing) {
        this.noEditing = noEditing;
        return this;
    }

    public boolean isDumpPoStrings() {
        return dumpPoStrings;
    }

    public NShellOptions setDumpPoStrings(boolean dumpPoStrings) {
        this.dumpPoStrings = dumpPoStrings;
        return this;
    }

    public boolean isDumpStrings() {
        return dumpStrings;
    }

    public NShellOptions setDumpStrings(boolean dumpStrings) {
        this.dumpStrings = dumpStrings;
        return this;
    }

    public boolean isLogin() {
        return login;
    }

    public NShellOptions setLogin(boolean login) {
        this.login = login;
        return this;
    }

    public boolean isDebugger() {
        return debugger;
    }

    public NShellOptions setDebugger(boolean debugger) {
        this.debugger = debugger;
        return this;
    }

    public String getRcFile() {
        return rcFile;
    }

    public NShellOptions setRcFile(String rcFile) {
        this.rcFile = rcFile;
        return this;
    }

    public boolean isStdInAndPos() {
        return stdInAndPos;
    }

    public NShellOptions setStdInAndPos(boolean stdInAndPos) {
        this.stdInAndPos = stdInAndPos;
        return this;
    }

    public boolean isBash() {
        return bash;
    }

    public NShellOptions setBash(boolean bash) {
        this.bash = bash;
        return this;
    }

    public boolean isCommand() {
        return command;
    }

    public NShellOptions setCommand(boolean command) {
        this.command = command;
        return this;
    }

    public List<String> getCommandArgs() {
        return commandArgs;
    }

    public NShellOptions setCommandArgs(List<String> commandArgs) {
        this.commandArgs = commandArgs;
        return this;
    }

    public List<String> getFiles() {
        return files;
    }

    public NShellOptions setFiles(List<String> files) {
        this.files = files;
        return this;
    }

    public boolean isExitAfterProcessingLines() {
        return exitAfterProcessingLines;
    }

    public NShellOptions setExitAfterProcessingLines(boolean exitAfterProcessingLines) {
        this.exitAfterProcessingLines = exitAfterProcessingLines;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public NShellOptions setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getStartupScript() {
        return startupScript;
    }

    public NShellOptions setStartupScript(String startupScript) {
        this.startupScript = startupScript;
        return this;
    }

    public String getShutdownScript() {
        return shutdownScript;
    }

    public NShellOptions setShutdownScript(String shutdownScript) {
        this.shutdownScript = shutdownScript;
        return this;
    }

    public boolean isVersion() {
        return version;
    }

    public NShellOptions setVersion(boolean version) {
        this.version = version;
        return this;
    }

    public boolean isHelp() {
        return help;
    }

    public NShellOptions setHelp(boolean help) {
        this.help = help;
        return this;
    }

    public boolean isEffectiveInteractive() {
        return effectiveInteractive;
    }

    public NShellOptions setEffectiveInteractive(boolean effectiveInteractive) {
        this.effectiveInteractive = effectiveInteractive;
        return this;
    }

    public boolean isErrExit() {
        return errExit;
    }

    public NShellOptions setErrExit(boolean errExit) {
        this.errExit = errExit;
        return this;
    }
    public boolean isNsh() {
        return !bash && !posix;
    }
}
