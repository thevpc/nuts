package net.vpc.app.nuts.runtime;

import net.vpc.app.nuts.NutsClassifierMapping;

public class DefaultNutsClassifierMapping implements NutsClassifierMapping {
    private final String classifier;
    private final String packaging;
    private final String[] arch;
    private final String[] os;
    private final String[] osdist;
    private final String[] platform;

    public DefaultNutsClassifierMapping(String classifier, String packaging,String[] arch, String[] os, String[] osdist, String[] platform) {
        this.classifier = classifier;
        this.packaging = packaging;
        this.arch = arch;
        this.os = os;
        this.osdist = osdist;
        this.platform = platform;
    }

    @Override
    public String getPackaging() {
        return packaging;
    }

    public String getClassifier() {
        return classifier;
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
