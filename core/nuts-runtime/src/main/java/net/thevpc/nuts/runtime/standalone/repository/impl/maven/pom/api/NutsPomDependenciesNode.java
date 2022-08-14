package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Collection;
import java.util.List;

public interface NutsPomDependenciesNode extends NutsPomNode {
    List<NutsPomDependencyNode> getObject();

    void removeDuplicates();

    void remove(NutsPomDependencyNode dependency);

    void remove(NutsPomDependency dependency);

    void removeAllChildren(NutsPomDependencyNode dependency);

    void removeAllChildren(NutsPomDependency dependency);

    void appendChild(NutsPomDependency dependency);
}
