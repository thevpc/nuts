package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl;

import net.thevpc.nuts.NutsBlankable;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependency;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependenciesNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomDependencyNode;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomId;
import net.thevpc.nuts.util.NutsStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.*;

public class PomDependenciesObj
        extends DefaultNutsPomNode<List<NutsPomDependencyNode>>
        implements NutsPomDependenciesNode {
    public PomDependenciesObj(Element element, List<NutsPomDependencyNode> object, Document document) {
        super(element, object, document);
    }

    @Override
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

    @Override
    public void remove(NutsPomDependencyNode dependency) {
        for (NutsPomDependencyNode x : getObject().toArray(new NutsPomDependencyNode[0])) {
            if (Objects.equals(x, dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
                return;
            }
        }
    }

    @Override
    public void remove(NutsPomDependency dependency) {
        for (NutsPomDependencyNode x : getObject().toArray(new NutsPomDependencyNode[0])) {
            if (Objects.equals(x.getObject(), dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
                return;
            }
        }
    }

    @Override
    public void removeAllChildren(NutsPomDependencyNode dependency) {
        removeAllChildren(dependency.getObject());
    }

    @Override
    public void removeAllChildren(NutsPomDependency dependency) {
        for (NutsPomDependencyNode x : getObject().toArray(new NutsPomDependencyNode[0])) {
            if (Objects.equals(x.getObject(), dependency)) {
                getObject().remove(x);
                getXmlElement().removeChild(x.getXmlElement());
            }
        }
    }

    @Override
    public void appendChild(NutsPomDependency dependency) {
        Element d = getDocument().createElement("dependency");
        d.appendChild(createTextElement("groupId", dependency.getGroupId()));
        d.appendChild(createTextElement("artifactId", dependency.getArtifactId()));
        if (!NutsBlankable.isBlank(dependency.getVersion())) {
            d.appendChild(createTextElement("version", dependency.getVersion()));
        }
        if (!NutsBlankable.isBlank(dependency.getOptional())) {
            d.appendChild(createTextElement("optional", dependency.getOptional()));
        }
        if (!NutsBlankable.isBlank(dependency.getClassifier())) {
            d.appendChild(createTextElement("classifier", dependency.getClassifier()));
        }
        if (dependency.getExclusions().length > 0) {
            Element exclusions = getDocument().createElement("exclusions");
            for (NutsPomId exclusion : dependency.getExclusions()) {
                exclusions.appendChild(createTextElement("groupId", exclusion.getGroupId()));
                exclusions.appendChild(createTextElement("artifactId", exclusion.getArtifactId()));
                if (!NutsBlankable.isBlank(exclusion.getVersion())) {
                    exclusions.appendChild(createTextElement("version", exclusion.getVersion()));
                }
            }
            d.appendChild(exclusions);
        }
        getXmlElement().appendChild(d);
        getObject().add(new PomDependencyObj(d, dependency, getDocument()));
    }
}
