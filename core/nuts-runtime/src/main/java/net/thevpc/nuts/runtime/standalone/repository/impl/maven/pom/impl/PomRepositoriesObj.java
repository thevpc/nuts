package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomRepositoriesNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomRepositoryNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl.DefaultNutsPomNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PomRepositoriesObj extends DefaultNutsPomNode<List<NutsPomRepositoryNode>> implements NutsPomRepositoriesNode {
    public PomRepositoriesObj(Element element, List<NutsPomRepositoryNode> object, Document document) {
        super(element, object,document);
    }
}
