package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomProperty;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomPropertyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class PomPropertyObj extends DefaultNPomNode<NPomProperty> implements NPomPropertyNode {
    public PomPropertyObj(Element element, NPomProperty object, Document document) {
        super(element, object,document);
    }
}
