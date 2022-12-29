package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomDependency;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomDependencyNode;
import net.thevpc.nuts.util.NStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomDependencyObj extends DefaultNPomNode<NPomDependency> implements NPomDependencyNode {
    public PomDependencyObj(Element element, NPomDependency object, Document document) {
        super(element, object, document);
    }

    @Override
    public void setVersion(String value) {
        super.setTextElement("version", NStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setGroupId(String value) {
        super.setTextElement("groupId", NStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setArtifactId(String value) {
        super.setTextElement("artifactId", NStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setOptional(String value) {
        super.setTextElement("optional", NStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setClassifier(String value) {
        super.setTextElement("classifier", NStringUtils.trimToNull(value), true,true);
    }

}
