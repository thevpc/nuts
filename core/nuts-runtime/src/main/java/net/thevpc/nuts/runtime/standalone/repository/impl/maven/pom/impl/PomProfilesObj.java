package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomProfileNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomProfilesNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;

public class PomProfilesObj extends DefaultNPomNode<List<NPomProfileNode>> implements NPomProfilesNode {
    public PomProfilesObj(Element element, List<NPomProfileNode> object, Document document) {
        super(element, object,document);
    }
}
