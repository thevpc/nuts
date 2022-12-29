package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api;

import org.w3c.dom.*;

public interface PomDomVisitor {

    default void visitStartDocument(Document document, PomDomVisitorContext context){}

    default void visitEndDocument(Document document, NPom pom, PomDomVisitorContext context){}

    default void visitStartDependency(Element dependencyElement, PomDomVisitorContext context){}

    default void visitEndDependency(NPomDependencyNode item, PomDomVisitorContext context){}

    default void visitStartDependencies(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndDependencies(NPomDependenciesNode dependencies, PomDomVisitorContext context){}

    default void visitStartDependencyManagement(Element dependencyElement, PomDomVisitorContext context){}

    default void visitEndDependencyManagement(NPomDependencyNode dependency, PomDomVisitorContext context){}

    default void visitStartDependenciesManagement(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndDependenciesManagement(NPomDependenciesNode dependencies, PomDomVisitorContext context){}

    default void visitStartRepository(Element dependencyElement, PomDomVisitorContext context){}

    default void visitEndRepository(NPomRepositoryNode repository, PomDomVisitorContext context){}

    default void visitStartPluginRepository(Element dependencyElement, PomDomVisitorContext context){}
    default void visitStartProfile(Element dependencyElement, PomDomVisitorContext context){}
    default void visitEndPluginRepository(NPomRepositoryNode dependency, PomDomVisitorContext context){}

    default void visitStartRepositories(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndRepositories(NPomRepositoriesNode repositories, PomDomVisitorContext context){}

    default void visitStartPluginRepositories(Element dependenciesElement, PomDomVisitorContext context){}
    default void visitStartProfiles(Element dependenciesElement, PomDomVisitorContext context){}

    default void visitEndPluginRepositories(NPomRepositoriesNode dependencies, PomDomVisitorContext context){}

    default void visitStartProperties(Element propertiesElement, PomDomVisitorContext context){}

    default void visitEndProperties(NPomPropertiesNode properties, PomDomVisitorContext context){}
    default void visitStartProperty(Element propertyElement, PomDomVisitorContext context){}
    default void visitEndProperty(NPomPropertyNode property, PomDomVisitorContext context){}

    default void visitEndProfile(NPomProfileNode p, PomDomVisitorContext context){}

    default void visitEndProfiles(NPomProfilesNode arr, PomDomVisitorContext context){}

}
