package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import java.util.List;

public interface NPomPropertiesNode extends NPomNode {
    List<NPomPropertyNode> getObject();
}
