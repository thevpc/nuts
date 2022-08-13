package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomProfileNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomProfilesNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PomProfilesObj extends DefaultNutsPomNode<List<NutsPomProfileNode>> implements NutsPomProfilesNode {
    public PomProfilesObj(Element element, List<NutsPomProfileNode> object, Document document) {
        super(element, object,document);
    }
}
