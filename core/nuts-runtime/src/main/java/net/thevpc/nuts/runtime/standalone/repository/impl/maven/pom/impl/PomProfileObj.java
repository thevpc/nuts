package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomProfile;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomProfileNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomProfileObj extends DefaultNutsPomNode<NutsPomProfile> implements NutsPomProfileNode {
    public PomProfileObj(Element element, NutsPomProfile object, Document document) {
        super(element, object, document);
    }
}
