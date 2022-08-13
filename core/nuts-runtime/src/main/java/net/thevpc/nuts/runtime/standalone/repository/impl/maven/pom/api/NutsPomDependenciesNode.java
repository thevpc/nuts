package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.Collection;
import java.util.List;

public interface NutsPomDependenciesNode extends NutsPomNode {
    List<NutsPomDependencyNode> getObject();
}
