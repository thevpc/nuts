package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.List;

public interface NPomRepositoriesNode extends NPomNode {
    List<NPomRepositoryNode> getObject();
}
