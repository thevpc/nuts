package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependency;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependenciesNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependencyNode;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class PomDependenciesObj
        extends DefaultNutsPomNode<List<NutsPomDependencyNode>>
        implements NutsPomDependenciesNode {
    public PomDependenciesObj(Element element, List<NutsPomDependencyNode> object, Document document) {
        super(element, object, document);
    }

    public void removeDuplicates() {
        Map<String, NutsPomDependencyNode> visited = new LinkedHashMap<>();
        NutsPomDependencyNode[] pomDependencyObjs = getObject().toArray(new NutsPomDependencyNode[0]);
        for (NutsPomDependencyNode dependencyAndElement : pomDependencyObjs) {
            NutsPomDependency dependency = dependencyAndElement.getObject();
            NutsPomDependencyNode last = visited.get(dependency.getShortName());
            if (last != null) {
                getObject().remove(last);
            }
            visited.put(dependency.getShortName(), dependencyAndElement);
        }
    }

    public void remove(PomDependencyObj dependency) {
        for (NutsPomDependencyNode x : getObject().toArray(new NutsPomDependencyNode[0])) {
            if (Objects.equals(x, dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
                return;
            }
        }
    }

    public void remove(NutsPomDependency dependency) {
        for (NutsPomDependencyNode x : getObject().toArray(new NutsPomDependencyNode[0])) {
            if (Objects.equals(x.getObject(), dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
                return;
            }
        }
    }

    public void removeAllChildren(PomDependencyObj dependency) {
        removeAllChildren(dependency.getObject());
    }

    public void removeAllChildren(NutsPomDependency dependency) {
        for (NutsPomDependencyNode x : getObject().toArray(new NutsPomDependencyNode[0])) {
            if (Objects.equals(x.getObject(), dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
            }
        }
    }

    public void appendChild(NutsPomDependency dependency) {
        Element d = getDocument().createElement("dependency");
        d.appendChild(createTextElement("groupId", dependency.getGroupId()));
        d.appendChild(createTextElement("artifactId", dependency.getArtifactId()));
        d.appendChild(createTextElement("version", dependency.getVersion()));
        getXmlElement().appendChild(d);
        getObject().add(new PomDependencyObj(d, dependency, getDocument()));
    }
}
