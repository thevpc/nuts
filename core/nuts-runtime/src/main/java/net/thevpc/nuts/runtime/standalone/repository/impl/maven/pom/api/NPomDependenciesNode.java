package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.List;

public interface NPomDependenciesNode extends NPomNode {
    List<NPomDependencyNode> getObject();

    void removeDuplicates();

    void remove(NPomDependencyNode dependency);

    void remove(NPomDependency dependency);

    void removeAllChildren(NPomDependencyNode dependency);

    void removeAllChildren(NPomDependency dependency);

    void appendChild(NPomDependency dependency);
}
