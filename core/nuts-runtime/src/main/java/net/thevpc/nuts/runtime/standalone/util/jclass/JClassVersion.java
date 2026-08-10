package net.thevpc.nuts.runtime.standalone.util.jclass;

import java.util.Objects;

public class JClassVersion implements Comparable<JClassVersion> {
    private final int classMajorVersion;
    private final int classMinorVersion;
    private final String sourceVersion;

    public static JClassVersion of(int major, int minor) {
        return new JClassVersion(major, minor);
    }

    public static JClassVersion of(String sourceVersion) {
        return new JClassVersion(sourceVersion);
    }

    public JClassVersion(int classMajorVersion, int classMinorVersion) {
        this.classMajorVersion = classMajorVersion;
        this.classMinorVersion = classMinorVersion;
        this.sourceVersion = JavaClassUtils.classVersionToSourceVersion(classMajorVersion, classMinorVersion);
    }

    public JClassVersion(String sourceVersion) {
        this.sourceVersion = sourceVersion;
        int[] i = JavaClassUtils.sourceVersionToClassVersion(sourceVersion);
        this.classMajorVersion = i[0];
        this.classMinorVersion = i[1];
    }

    public int classMajorVersion() {
        return classMajorVersion;
    }

    public int classMinorVersion() {
        return classMinorVersion;
    }

    public String sourceVersion() {
        return sourceVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        JClassVersion that = (JClassVersion) o;
        return classMajorVersion == that.classMajorVersion && classMinorVersion == that.classMinorVersion && Objects.equals(sourceVersion, that.sourceVersion);
    }

    @Override
    public int hashCode() {
        return Objects.hash(classMajorVersion, classMinorVersion, sourceVersion);
    }

    @Override
    public int compareTo(JClassVersion o) {
        if (o == null) {
            return 1;
        }
        int a = Integer.compare(classMajorVersion, o.classMajorVersion);
        if (a != 0) {
            return a;
        }
        a = Integer.compare(classMinorVersion, o.classMinorVersion);
        return a;
    }
    public boolean isPreview(){
        return classMinorVersion==65535;
    }

    @Override
    public String toString() {
        if(isPreview()){
            return "JClassVersion{" +
                    "class='" + classMajorVersion + "." + classMinorVersion + "' preview" +
                    ", source='" + sourceVersion + '\'' +
                    '}';
        }
        return "JClassVersion{" +
                "class='" + classMajorVersion + "." + classMinorVersion + "'" +
                ", source='" + sourceVersion + '\'' +
                '}';
    }
}
