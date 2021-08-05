package net.thevpc.nuts.toolbox.nsh.bundles.jshell;

import java.util.ArrayList;
import java.util.List;

public class JShellOptions {
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

    public JShellOptions setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public boolean isXtrace() {
        return xtrace;
    }

    public JShellOptions setXtrace(boolean xtrace) {
        this.xtrace = xtrace;
        return this;
    }

    public boolean isRestricted() {
        return restricted;
    }

    public JShellOptions setRestricted(boolean restricted) {
        this.restricted = restricted;
        return this;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public JShellOptions setInteractive(boolean interactive) {
        this.interactive = interactive;
        return this;
    }

    public boolean isPosix() {
        return posix;
    }

    public JShellOptions setPosix(boolean posix) {
        this.posix = posix;
        return this;
    }

    public boolean isNoRc() {
        return noRc;
    }

    public JShellOptions setNoRc(boolean noRc) {
        this.noRc = noRc;
        return this;
    }

    public boolean isNoProfile() {
        return noProfile;
    }

    public JShellOptions setNoProfile(boolean noProfile) {
        this.noProfile = noProfile;
        return this;
    }

    public boolean isNoEditing() {
        return noEditing;
    }

    public JShellOptions setNoEditing(boolean noEditing) {
        this.noEditing = noEditing;
        return this;
    }

    public boolean isDumpPoStrings() {
        return dumpPoStrings;
    }

    public JShellOptions setDumpPoStrings(boolean dumpPoStrings) {
        this.dumpPoStrings = dumpPoStrings;
        return this;
    }

    public boolean isDumpStrings() {
        return dumpStrings;
    }

    public JShellOptions setDumpStrings(boolean dumpStrings) {
        this.dumpStrings = dumpStrings;
        return this;
    }

    public boolean isLogin() {
        return login;
    }

    public JShellOptions setLogin(boolean login) {
        this.login = login;
        return this;
    }

    public boolean isDebugger() {
        return debugger;
    }

    public JShellOptions setDebugger(boolean debugger) {
        this.debugger = debugger;
        return this;
    }

    public String getRcFile() {
        return rcFile;
    }

    public JShellOptions setRcFile(String rcFile) {
        this.rcFile = rcFile;
        return this;
    }

    public boolean isStdInAndPos() {
        return stdInAndPos;
    }

    public JShellOptions setStdInAndPos(boolean stdInAndPos) {
        this.stdInAndPos = stdInAndPos;
        return this;
    }

    public boolean isBash() {
        return bash;
    }

    public JShellOptions setBash(boolean bash) {
        this.bash = bash;
        return this;
    }

    public boolean isCommand() {
        return command;
    }

    public JShellOptions setCommand(boolean command) {
        this.command = command;
        return this;
    }

    public List<String> getCommandArgs() {
        return commandArgs;
    }

    public JShellOptions setCommandArgs(List<String> commandArgs) {
        this.commandArgs = commandArgs;
        return this;
    }

    public List<String> getFiles() {
        return files;
    }

    public JShellOptions setFiles(List<String> files) {
        this.files = files;
        return this;
    }

    public boolean isExitAfterProcessingLines() {
        return exitAfterProcessingLines;
    }

    public JShellOptions setExitAfterProcessingLines(boolean exitAfterProcessingLines) {
        this.exitAfterProcessingLines = exitAfterProcessingLines;
        return this;
    }

    public String getServiceName() {
        return serviceName;
    }

    public JShellOptions setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public String getStartupScript() {
        return startupScript;
    }

    public JShellOptions setStartupScript(String startupScript) {
        this.startupScript = startupScript;
        return this;
    }

    public String getShutdownScript() {
        return shutdownScript;
    }

    public JShellOptions setShutdownScript(String shutdownScript) {
        this.shutdownScript = shutdownScript;
        return this;
    }

    public boolean isVersion() {
        return version;
    }

    public JShellOptions setVersion(boolean version) {
        this.version = version;
        return this;
    }

    public boolean isHelp() {
        return help;
    }

    public JShellOptions setHelp(boolean help) {
        this.help = help;
        return this;
    }

    public boolean isEffectiveInteractive() {
        return effectiveInteractive;
    }

    public JShellOptions setEffectiveInteractive(boolean effectiveInteractive) {
        this.effectiveInteractive = effectiveInteractive;
        return this;
    }

    public boolean isErrExit() {
        return errExit;
    }

    public JShellOptions setErrExit(boolean errExit) {
        this.errExit = errExit;
        return this;
    }
    public boolean isNsh() {
        return !bash && !posix;
    }
}
