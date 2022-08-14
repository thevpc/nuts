package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependency;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependencyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomDependencyObj extends DefaultNutsPomNode<NutsPomDependency> implements NutsPomDependencyNode {
    public PomDependencyObj(Element element, NutsPomDependency object, Document document) {
        super(element, object, document);
    }

    @Override
    public void setVersion(String version) {
        super.setTextElement("version", version, true);
    }

    @Override
    public void setGroupId(String version) {
        super.setTextElement("groupId", version, true);
    }

    @Override
    public void setArtifactGroupId(String version) {
        super.setTextElement("artifactId", version, true);
    }

}
