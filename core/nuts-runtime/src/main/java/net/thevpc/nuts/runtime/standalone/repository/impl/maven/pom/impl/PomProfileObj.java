package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomProfile;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomProfileNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomProfileObj extends DefaultNPomNode<NPomProfile> implements NPomProfileNode {
    public PomProfileObj(Element element, NPomProfile object, Document document) {
        super(element, object, document);
    }
}
