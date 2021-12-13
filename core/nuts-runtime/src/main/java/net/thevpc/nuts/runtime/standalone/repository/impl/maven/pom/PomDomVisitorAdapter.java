package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class PomDomVisitorAdapter implements PomDomVisitor {

    @Override
    public void visitStartDocument(Document document) {

    }

    @Override
    public void visitEndDocument(Document document, Pom pom) {

    }

    @Override
    public void visitStartDependency(Element dependencyElement) {

    }

    @Override
    public void visitEndDependency(Element dependencyElement, PomDependency dependency) {

    }

    @Override
    public void visitStartDependencies(Element dependenciesElement) {

    }

    @Override
    public void visitEndDependencies(Element dependenciesElement, PomDependency[] dependencies) {

    }

    @Override
    public void visitStartProperties(Element propertiesElement) {

    }

    @Override
    public void visitEndProperties(Element propertiesElement, Map<String, String> properties) {

    }

    @Override
    public void visitStartRepository(Element dependencyElement) {

    }

    @Override
    public void visitEndRepository(Element dependencyElement, PomRepository repository) {

    }

    @Override
    public void visitStartPluginRepository(Element pluginRepositoryElement) {

    }

    @Override
    public void visitEndPluginRepository(Element pluginRepositoryElement, PomRepository pluginRepository) {

    }

    @Override
    public void visitStartRepositories(Element repositoriesElement) {

    }

    @Override
    public void visitEndRepositories(Element repositoriesElement, PomRepository[] repositories) {

    }

    @Override
    public void visitStartPluginRepositories(Element pluginRepositoriesElement) {

    }

    @Override
    public void visitEndPluginRepositories(Element pluginRepositoriesElement, PomRepository[] pluginRepositories) {

    }

    @Override
    public void visitStartDependencyManagement(Element dependencyElement) {

    }

    @Override
    public void visitEndDependencyManagement(Element dependencyElement, PomDependency dependency) {

    }

    @Override
    public void visitStartDependenciesManagement(Element dependenciesElement) {

    }

    @Override
    public void visitEndDependenciesManagement(Element dependenciesElement, PomDependency[] dependencies) {

    }

    @Override
    public void visitStartProfile(Element dependencyElement) {

    }

    @Override
    public void visitStartProfiles(Element dependenciesElement) {

    }

    @Override
    public void visitEndProfile(Element profile, PomProfile p) {

    }

    @Override
    public void visitEndProfiles(Element elem1, PomProfile[] arr) {

    }
}
