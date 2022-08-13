package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomProperty;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomPropertyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomPropertyObj extends DefaultNutsPomNode<NutsPomProperty> implements NutsPomPropertyNode {
    public PomPropertyObj(Element element, NutsPomProperty object, Document document) {
        super(element, object,document);
    }
}
