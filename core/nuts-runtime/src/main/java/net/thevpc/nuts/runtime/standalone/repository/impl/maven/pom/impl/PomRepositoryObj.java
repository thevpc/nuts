package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomRepositoryNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomRepositoryObj extends DefaultNPomNode<NPomRepository> implements NPomRepositoryNode {
    public PomRepositoryObj(Element element, NPomRepository object, Document document) {
        super(element, object,document);
    }
}
