package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomDependency;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomDependenciesNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomDependencyNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomId;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class PomDependenciesObj
        extends DefaultNPomNode<List<NPomDependencyNode>>
        implements NPomDependenciesNode {
    public PomDependenciesObj(Element element, List<NPomDependencyNode> object, Document document) {
        super(element, object, document);
    }

    @Override
    public void removeDuplicates() {
        Map<String, NPomDependencyNode> visited = new LinkedHashMap<>();
        NPomDependencyNode[] pomDependencyObjs = getObject().toArray(new NPomDependencyNode[0]);
        for (NPomDependencyNode dependencyAndElement : pomDependencyObjs) {
            NPomDependency dependency = dependencyAndElement.getObject();
            NPomDependencyNode last = visited.get(dependency.getShortName());
            if (last != null) {
                getObject().remove(last);
            }
            visited.put(dependency.getShortName(), dependencyAndElement);
        }
    }

    @Override
    public void remove(NPomDependencyNode dependency) {
        for (NPomDependencyNode x : getObject().toArray(new NPomDependencyNode[0])) {
            if (Objects.equals(x, dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
                return;
            }
        }
    }

    @Override
    public void remove(NPomDependency dependency) {
        for (NPomDependencyNode x : getObject().toArray(new NPomDependencyNode[0])) {
            if (Objects.equals(x.getObject(), dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
                return;
            }
        }
    }

    @Override
    public void removeAllChildren(NPomDependencyNode dependency) {
        removeAllChildren(dependency.getObject());
    }

    @Override
    public void removeAllChildren(NPomDependency dependency) {
        for (NPomDependencyNode x : getObject().toArray(new NPomDependencyNode[0])) {
            if (Objects.equals(x.getObject(), dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
            }
        }
    }

    @Override
    public void appendChild(NPomDependency dependency) {
        Element d = getDocument().createElement("dependency");
        d.appendChild(createTextElement("groupId", dependency.getGroupId()));
        d.appendChild(createTextElement("artifactId", dependency.getArtifactId()));
        if (!NBlankable.isBlank(dependency.getVersion())) {
            d.appendChild(createTextElement("version", dependency.getVersion()));
        }
        if (!NBlankable.isBlank(dependency.getOptional())) {
            d.appendChild(createTextElement("optional", dependency.getOptional()));
        }
        if (!NBlankable.isBlank(dependency.getClassifier())) {
            d.appendChild(createTextElement("classifier", dependency.getClassifier()));
        }
        if (dependency.getExclusions().length > 0) {
            Element exclusions = getDocument().createElement("exclusions");
            for (NPomId exclusion : dependency.getExclusions()) {
                exclusions.appendChild(createTextElement("groupId", exclusion.getGroupId()));
                exclusions.appendChild(createTextElement("artifactId", exclusion.getArtifactId()));
                if (!NBlankable.isBlank(exclusion.getVersion())) {
                    exclusions.appendChild(createTextElement("version", exclusion.getVersion()));
                }
            }
            d.appendChild(exclusions);
        }
        getXmlElement().appendChild(d);
        getObject().add(new PomDependencyObj(d, dependency, getDocument()));
    }
}
