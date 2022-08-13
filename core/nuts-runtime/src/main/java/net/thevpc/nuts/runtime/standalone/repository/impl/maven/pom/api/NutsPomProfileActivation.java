package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Objects;

public class NutsPomProfileActivation {
    private Boolean activeByDefault;
    private String jdk;
    private String propertyName;
    private String propertyValue;
    private String file;
    private String osName;
    private String osArch;
    private String osFamily;
    private String osVersion;

    public NutsPomProfileActivation() {
    }

    public Boolean getActiveByDefault() {
        return activeByDefault;
    }

    public NutsPomProfileActivation setActiveByDefault(Boolean activeByDefault) {
        this.activeByDefault = activeByDefault;
        return this;
    }

    public String getJdk() {
        return jdk;
    }

    public NutsPomProfileActivation setJdk(String jdk) {
        this.jdk = jdk;
        return this;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public NutsPomProfileActivation setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public NutsPomProfileActivation setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public NutsPomProfileActivation setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public String getOsArch() {
        return osArch;
    }

    public NutsPomProfileActivation setOsArch(String osArch) {
        this.osArch = osArch;
        return this;
    }

    public String getOsFamily() {
        return osFamily;
    }

    public NutsPomProfileActivation setOsFamily(String osFamily) {
        this.osFamily = osFamily;
        return this;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public NutsPomProfileActivation setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public String getFile() {
        return file;
    }

    public NutsPomProfileActivation setFile(String file) {
        this.file = file;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsPomProfileActivation that = (NutsPomProfileActivation) o;
        return Objects.equals(activeByDefault, that.activeByDefault) && Objects.equals(jdk, that.jdk)
                && Objects.equals(propertyName, that.propertyName)
                && Objects.equals(propertyValue, that.propertyValue)
                && Objects.equals(file, that.file) && Objects.equals(osName, that.osName) && Objects.equals(osArch, that.osArch) && Objects.equals(osFamily, that.osFamily) && Objects.equals(osVersion, that.osVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(activeByDefault, jdk, propertyName,propertyValue, file, osName, osArch, osFamily, osVersion);
    }

    @Override
    public String toString() {
        return "PomProfileActivation{" +
                "activeByDefault=" + activeByDefault +
                ", jdk='" + jdk + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue='" + propertyValue + '\'' +
                ", file='" + file + '\'' +
                ", osName='" + osName + '\'' +
                ", osArch='" + osArch + '\'' +
                ", osFamily='" + osFamily + '\'' +
                ", osVersion='" + osVersion + '\'' +
                '}';
    }
}
