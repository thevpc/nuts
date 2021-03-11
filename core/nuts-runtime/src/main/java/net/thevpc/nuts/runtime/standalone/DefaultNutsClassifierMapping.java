package net.thevpc.nuts.runtime.standalone;

import net.thevpc.nuts.NutsClassifierMapping;

public class DefaultNutsClassifierMapping implements NutsClassifierMapping {
    private final String classifier;
    private final String packaging;
    private final String[] arch;
    private final String[] os;
    private final String[] osdist;
    private final String[] platform;

    public DefaultNutsClassifierMapping(NutsClassifierMapping other) {
        this.classifier = other.getClassifier();
        this.packaging = other.getPackaging();
        this.arch = other.getArch();
        this.os = other.getOs();
        this.osdist = other.getOsdist();
        this.platform = other.getPlatform();
    }
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
