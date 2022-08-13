package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import org.w3c.dom.*;

public interface PomDomVisitor {

    default void visitStartDocument(Document document, PomDomVisitorContext context){}

    default void visitEndDocument(Document document, NutsPom pom, PomDomVisitorContext context){}

    default void visitStartDependency(Element dependencyElement, PomDomVisitorContext context){}

    default void visitEndDependency(NutsPomDependencyNode item, PomDomVisitorContext context){}

    default void visitStartDependencies(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndDependencies(NutsPomDependenciesNode dependencies, PomDomVisitorContext context){}

    default void visitStartDependencyManagement(Element dependencyElement, PomDomVisitorContext context){}

    default void visitEndDependencyManagement(NutsPomDependencyNode dependency, PomDomVisitorContext context){}

    default void visitStartDependenciesManagement(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndDependenciesManagement(NutsPomDependenciesNode dependencies, PomDomVisitorContext context){}

    default void visitStartRepository(Element dependencyElement, PomDomVisitorContext context){}

    default void visitEndRepository(NutsPomRepositoryNode repository, PomDomVisitorContext context){}

    default void visitStartPluginRepository(Element dependencyElement, PomDomVisitorContext context){}
    default void visitStartProfile(Element dependencyElement, PomDomVisitorContext context){}
    default void visitEndPluginRepository(NutsPomRepositoryNode dependency, PomDomVisitorContext context){}

    default void visitStartRepositories(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndRepositories(NutsPomRepositoriesNode repositories, PomDomVisitorContext context){}

    default void visitStartPluginRepositories(Element dependenciesElement, PomDomVisitorContext context){}
    default void visitStartProfiles(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndPluginRepositories(NutsPomRepositoriesNode dependencies, PomDomVisitorContext context){}

    default void visitStartProperties(Element propertiesElement, PomDomVisitorContext context){}

    default void visitEndProperties(NutsPomPropertiesNode properties, PomDomVisitorContext context){}
    default void visitStartProperty(Element propertyElement, PomDomVisitorContext context){}
    default void visitEndProperty(NutsPomPropertyNode property, PomDomVisitorContext context){}

    default void visitEndProfile(NutsPomProfileNode p, PomDomVisitorContext context){}

    default void visitEndProfiles(NutsPomProfilesNode arr, PomDomVisitorContext context){}

}
