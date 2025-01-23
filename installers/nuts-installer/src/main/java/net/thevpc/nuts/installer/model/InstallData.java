package net.thevpc.nuts.installer.model;

import net.thevpc.nuts.installer.InstallerContext;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class InstallData {
    public boolean installStable;
    public boolean darkMode;
    public boolean optionZ;
    public boolean optionS;
    public boolean optionVerbose;
    public boolean optionVerboseFile;
    public boolean optionk;
    public boolean optionSwitch;
    public List<String> otherOptions=new ArrayList<>();
    private VerInfo installVersion;
    public String workspace;
    public String java;
    public Set<App> recommendedIds=new LinkedHashSet<>();

    public String[] cmdline;
    public String defaultWorkspace;
    public String defaultNutsOptions;
    public String defaultJavaHome;
    public boolean defaultAcceptTerms;
    public boolean defaultVerbose;
    public boolean defaultVerboseFile;
    public boolean defaultSwitch;
    public boolean defaultStandalone;
    public boolean defaultReset;

    public static InstallData of(InstallerContext context){
        InstallData c=(InstallData) context.getVars().get(InstallData.class.getName());
        if(c==null){
            c=new InstallData();
            context.getVars().put(InstallData.class.getName(),c);
        }
        return c;
    }

    public boolean isOptionVerboseFile() {
        return optionVerboseFile;
    }

    public void setOptionVerboseFile(boolean optionVerboseFile) {
        this.optionVerboseFile = optionVerboseFile;
    }

    public boolean isDefaultVerboseFile() {
        return defaultVerboseFile;
    }

    public void setDefaultVerboseFile(boolean defaultVerboseFile) {
        this.defaultVerboseFile = defaultVerboseFile;
    }

    public String getDefaultJavaHome() {
        return defaultJavaHome;
    }

    public void setDefaultJavaHome(String defaultJavaHome) {
        this.defaultJavaHome = defaultJavaHome;
    }

    public boolean isDefaultAcceptTerms() {
        return defaultAcceptTerms;
    }

    public void setDefaultAcceptTerms(boolean defaultAcceptTerms) {
        this.defaultAcceptTerms = defaultAcceptTerms;
    }

    public String getDefaultNutsOptions() {
        return defaultNutsOptions;
    }

    public void setDefaultNutsOptions(String defaultNutsOptions) {
        this.defaultNutsOptions = defaultNutsOptions;
    }

    public boolean isDefaultSwitch() {
        return defaultSwitch;
    }

    public void setDefaultSwitch(boolean defaultSwitch) {
        this.defaultSwitch = defaultSwitch;
    }

    public boolean isDefaultStandalone() {
        return defaultStandalone;
    }

    public void setDefaultStandalone(boolean defaultStandalone) {
        this.defaultStandalone = defaultStandalone;
    }

    public boolean isDefaultReset() {
        return defaultReset;
    }

    public void setDefaultReset(boolean defaultReset) {
        this.defaultReset = defaultReset;
    }

    public boolean isDefaultVerbose() {
        return defaultVerbose;
    }

    public void setDefaultVerbose(boolean defaultVerbose) {
        this.defaultVerbose = defaultVerbose;
    }

    public String[] getCmdline() {
        return cmdline;
    }

    public void setCmdline(String[] cmdline) {
        this.cmdline = cmdline;
    }

    public String getDefaultWorkspace() {
        return defaultWorkspace;
    }

    public void setDefaultWorkspace(String defaultWorkspace) {
        this.defaultWorkspace = defaultWorkspace;
    }

    public boolean isInstallStable() {
        return installStable;
    }

    public InstallData setInstallStable(boolean installStable) {
        this.installStable = installStable;
        return this;
    }

    public boolean isDarkMode() {
        return darkMode;
    }

    public InstallData setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
        return this;
    }

    public boolean isOptionZ() {
        return optionZ;
    }

    public InstallData setOptionZ(boolean optionZ) {
        this.optionZ = optionZ;
        return this;
    }

    public boolean isOptionS() {
        return optionS;
    }

    public InstallData setOptionS(boolean optionS) {
        this.optionS = optionS;
        return this;
    }

    public boolean isOptionVerbose() {
        return optionVerbose;
    }

    public InstallData setOptionVerbose(boolean optionVerbose) {
        this.optionVerbose = optionVerbose;
        return this;
    }

    public boolean isOptionk() {
        return optionk;
    }

    public InstallData setOptionk(boolean optionk) {
        this.optionk = optionk;
        return this;
    }

    public boolean isOptionSwitch() {
        return optionSwitch;
    }

    public InstallData setOptionSwitch(boolean optionSwitch) {
        this.optionSwitch = optionSwitch;
        return this;
    }

    public List<String> getOtherOptions() {
        return otherOptions;
    }

    public InstallData setOtherOptions(List<String> otherOptions) {
        this.otherOptions = otherOptions;
        return this;
    }

    public VerInfo getInstallVersion() {
        return installVersion;
    }

    public InstallData setInstallVersion(VerInfo installVersion) {
        this.installVersion = installVersion;
        return this;
    }

    public String getWorkspace() {
        return workspace;
    }

    public InstallData setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public String getJava() {
        return java;
    }

    public InstallData setJava(String java) {
        this.java = java;
        return this;
    }

    public Set<App> getRecommendedIds() {
        return recommendedIds;
    }

    public InstallData setRecommendedIds(Set<App> recommendedIds) {
        this.recommendedIds = recommendedIds;
        return this;
    }
}
