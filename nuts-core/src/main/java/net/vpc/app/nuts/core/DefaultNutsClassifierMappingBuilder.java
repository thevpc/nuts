package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsClassifierMapping;
import net.vpc.app.nuts.NutsClassifierMappingBuilder;

public class DefaultNutsClassifierMappingBuilder implements NutsClassifierMappingBuilder {

    private String classifier;
    private String[] arch;
    private String[] os;
    private String[] osdist;
    private String[] platform;

    public DefaultNutsClassifierMappingBuilder() {
    }

    public DefaultNutsClassifierMappingBuilder(NutsClassifierMapping value) {
        setClassifier(value.getClassifier());
        setPlatform(value.getPlatform());
        setOs(value.getOs());
        setOsdist(value.getOsdist());
        setPlatform(value.getPlatform());
    }

    @Override
    public String getClassifier() {
        return classifier;
    }

    @Override
    public DefaultNutsClassifierMappingBuilder setClassifier(String targetClassifier) {
        this.classifier = targetClassifier;
        return this;
    }

    @Override
    public String[] getArch() {
        return arch;
    }

    @Override
    public DefaultNutsClassifierMappingBuilder setArch(String... arch) {
        this.arch = arch;
        return this;
    }

    @Override
    public String[] getOs() {
        return os;
    }

    @Override
    public DefaultNutsClassifierMappingBuilder setOs(String... os) {
        this.os = os;
        return this;
    }

    @Override
    public String[] getOsdist() {
        return osdist;
    }

    @Override
    public DefaultNutsClassifierMappingBuilder setOsdist(String... osdist) {
        this.osdist = osdist;
        return this;
    }

    @Override
    public String[] getPlatform() {
        return platform;
    }

    @Override
    public DefaultNutsClassifierMappingBuilder setPlatform(String... platform) {
        this.platform = platform;
        return this;
    }

    @Override
    public NutsClassifierMappingBuilder classifier(String value) {
        return setClassifier(value);
    }

    @Override
    public NutsClassifierMappingBuilder arch(String... value) {
        return setArch(value);
    }

    @Override
    public NutsClassifierMappingBuilder os(String... value) {
        return setOs(value);
    }

    @Override
    public NutsClassifierMappingBuilder osdist(String... value) {
        return setOsdist(value);
    }

    @Override
    public NutsClassifierMappingBuilder platform(String... value) {
        return setPlatform(value);
    }

    @Override
    public NutsClassifierMappingBuilder set(NutsClassifierMappingBuilder value) {
        if(value==null){
            clear();
        }else{
            setClassifier(value.getClassifier());
            setOsdist(value.getOsdist());
            setOs(value.getOs());
            setPlatform(value.getPlatform());
            setArch(value.getArch());
        }
        return this;
    }

    @Override
    public NutsClassifierMappingBuilder set(NutsClassifierMapping value) {
        if(value==null){
            clear();
        }else{
            setClassifier(value.getClassifier());
            setOsdist(value.getOsdist());
            setOs(value.getOs());
            setPlatform(value.getPlatform());
            setArch(value.getArch());
        }
        return this;
    }

    @Override
    public NutsClassifierMappingBuilder clear() {
        setClassifier(null);
        setOsdist();
        setOs();
        setPlatform();
        setArch();
        return this;
    }

    @Override
    public NutsClassifierMapping build() {
        return new DefaultNutsClassifierMapping(classifier, arch, os, osdist, platform);
    }
}
