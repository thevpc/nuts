package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependency;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependencyNode;
import net.thevpc.nuts.util.NutsStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomDependencyObj extends DefaultNutsPomNode<NutsPomDependency> implements NutsPomDependencyNode {
    public PomDependencyObj(Element element, NutsPomDependency object, Document document) {
        super(element, object, document);
    }

    @Override
    public void setVersion(String value) {
        super.setTextElement("version", NutsStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setGroupId(String value) {
        super.setTextElement("groupId", NutsStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setArtifactId(String value) {
        super.setTextElement("artifactId", NutsStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setOptional(String value) {
        super.setTextElement("optional", NutsStringUtils.trimToNull(value), true,true);
    }

    @Override
    public void setClassifier(String value) {
        super.setTextElement("classifier", NutsStringUtils.trimToNull(value), true,true);
    }

}
