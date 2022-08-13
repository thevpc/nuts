package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomProperty;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomPropertiesNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomPropertyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.List;
import java.util.Objects;

public class PomPropertiesObj extends DefaultNutsPomNode<List<NutsPomPropertyNode>> implements NutsPomPropertiesNode {
    public PomPropertiesObj(Element element, List<NutsPomPropertyNode> object, Document document) {
        super(element, object,document);
    }

    public NutsPomPropertyNode getProperty(String name) {
        return getObject().stream().filter(x -> Objects.equals(x.getObject().getName(), name))
                .findAny().orElse(null);
    }


    public void removeChild(NutsPomPropertyNode property) {
        removeChild(property.getObject());
    }

    public void removeChild(NutsPomProperty property) {
        getObject().removeIf(x -> Objects.equals(x.getObject(), property));
    }

    public void removeChild(String propertyName) {
        getObject().removeIf(x -> Objects.equals(x.getObject().getName(), propertyName));
    }

    public void appendChild(NutsPomProperty property) {
        Element d = createTextElement(property.getName(), property.getValue());
        getXmlElement().appendChild(d);
        getObject().add(new PomPropertyObj(d, property,getDocument()));
    }

}
