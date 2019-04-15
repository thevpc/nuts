package net.vpc.app.nuts.core.util.mvn;

import org.w3c.dom.*;

import java.util.Map;

public interface PomDomVisitor {
    void visitStartDocument(Document document);

    void visitEndDocument(Document document, Pom pom);

    void visitStartDependency(Element dependencyElement);

    void visitEndDependency(Element dependencyElement, PomDependency dependency);

    void visitStartDependencies(Element dependenciesElement);

    void visitEndDependencies(Element dependenciesElement, PomDependency[] dependencies);


    void visitStartDependencyManagement(Element dependencyElement);

    void visitEndDependencyManagement(Element dependencyElement, PomDependency dependency);

    void visitStartDependenciesManagement(Element dependenciesElement);

    void visitEndDependenciesManagement(Element dependenciesElement, PomDependency[] dependencies);

    void visitStartRepository(Element dependencyElement);

    void visitEndRepository(Element dependencyElement, PomRepository dependency);

    void visitStartPluginRepository(Element dependencyElement);

    void visitEndPluginRepository(Element dependencyElement, PomRepository dependency);

    void visitStartRepositories(Element dependenciesElement);

    void visitEndRepositories(Element dependenciesElement, PomRepository[] dependencies);

    void visitStartPluginRepositories(Element dependenciesElement);

    void visitEndPluginRepositories(Element dependenciesElement, PomRepository[] dependencies);

    void visitStartProperties(Element propertiesElement);

    void visitEndProperties(Element propertiesElement, Map<String, String> properties);
}
