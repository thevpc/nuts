package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsClassifierMapping;

public class DefaultNutsClassifierMapping implements NutsClassifierMapping {
    private String targetClassifier;
    private String[] arch;
    private String[] os;
    private String[] osdist;
    private String[] platform;

    public DefaultNutsClassifierMapping(String targetClassifier, String[] arch, String[] os, String[] osdist, String[] platform) {
        this.targetClassifier = targetClassifier;
        this.arch = arch;
        this.os = os;
        this.osdist = osdist;
        this.platform = platform;
    }

    public String getClassifier() {
        return targetClassifier;
    }

    public String[] getArch() {
        return arch;
    }

    public String[] getOs() {
        return os;
    }

    public String[] getOsdist() {
        return osdist;
    }

    public String[] getPlatform() {
        return platform;
    }
}
