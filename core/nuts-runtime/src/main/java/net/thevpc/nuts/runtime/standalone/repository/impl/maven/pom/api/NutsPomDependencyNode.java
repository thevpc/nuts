package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

public interface NutsPomDependencyNode extends NutsPomNode {
    NutsPomDependency getObject();

    void setVersion(String version);

    void setGroupId(String version);

    void setArtifactGroupId(String version);
}
