package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomProperty;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomPropertiesNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomPropertyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Objects;

public class PomPropertiesObj extends DefaultNPomNode<List<NPomPropertyNode>> implements NPomPropertiesNode {
    public PomPropertiesObj(Element element, List<NPomPropertyNode> object, Document document) {
        super(element, object,document);
    }

    public NPomPropertyNode getProperty(String name) {
        return getObject().stream().filter(x -> Objects.equals(x.getObject().getName(), name))
                .findAny().orElse(null);
    }


    public void removeChild(NPomPropertyNode property) {
        removeChild(property.getObject());
    }

    public void removeChild(NPomProperty property) {
        getObject().removeIf(x -> Objects.equals(x.getObject(), property));
    }

    public void removeChild(String propertyName) {
        getObject().removeIf(x -> Objects.equals(x.getObject().getName(), propertyName));
    }

    public void appendChild(NPomProperty property) {
        Element d = createTextElement(property.getName(), property.getValue());
        getXmlElement().appendChild(d);
        getObject().add(new PomPropertyObj(d, property,getDocument()));
    }

}
