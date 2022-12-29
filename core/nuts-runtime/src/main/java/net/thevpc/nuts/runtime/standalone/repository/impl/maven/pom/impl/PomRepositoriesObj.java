package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomRepositoriesNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomRepositoryNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PomRepositoriesObj extends DefaultNPomNode<List<NPomRepositoryNode>> implements NPomRepositoriesNode {
    public PomRepositoriesObj(Element element, List<NPomRepositoryNode> object, Document document) {
        super(element, object,document);
    }
}
