package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

import java.util.Objects;

public class PomProfileActivation {
    private Boolean activeByDefault;
    private String jdk;
    private String propertyName;
    private String propertyValue;
    private String file;
    private String osName;
    private String osArch;
    private String osFamily;
    private String osVersion;

    public PomProfileActivation() {
    }

    public Boolean getActiveByDefault() {
        return activeByDefault;
    }

    public PomProfileActivation setActiveByDefault(Boolean activeByDefault) {
        this.activeByDefault = activeByDefault;
        return this;
    }

    public String getJdk() {
        return jdk;
    }

    public PomProfileActivation setJdk(String jdk) {
        this.jdk = jdk;
        return this;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public PomProfileActivation setPropertyName(String propertyName) {
        this.propertyName = propertyName;
        return this;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public PomProfileActivation setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
        return this;
    }

    public String getOsName() {
        return osName;
    }

    public PomProfileActivation setOsName(String osName) {
        this.osName = osName;
        return this;
    }

    public String getOsArch() {
        return osArch;
    }

    public PomProfileActivation setOsArch(String osArch) {
        this.osArch = osArch;
        return this;
    }

    public String getOsFamily() {
        return osFamily;
    }

    public PomProfileActivation setOsFamily(String osFamily) {
        this.osFamily = osFamily;
        return this;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public PomProfileActivation setOsVersion(String osVersion) {
        this.osVersion = osVersion;
        return this;
    }

    public String getFile() {
        return file;
    }

    public PomProfileActivation setFile(String file) {
        this.file = file;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PomProfileActivation that = (PomProfileActivation) o;
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
