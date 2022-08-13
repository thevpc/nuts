package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomRepository;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomRepositoryNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomRepositoryObj extends DefaultNutsPomNode<NutsPomRepository> implements NutsPomRepositoryNode {
    public PomRepositoryObj(Element element, NutsPomRepository object, Document document) {
        super(element, object,document);
    }
}
