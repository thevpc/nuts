package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.impl.*;
import net.thevpc.nuts.runtime.standalone.format.xml.XmlUtils;
import net.thevpc.nuts.runtime.standalone.util.XmlEscaper;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NPomXmlParser {
    public static final Pattern NUTS_OS_ARCH_DEPS_PATTERN = Pattern.compile("^nuts([.](?<os>[a-zA-Z0-9-_]+)-os)?([.](?<arch>[a-zA-Z0-9-_]+)-arch)?-dependencies$");



    public NPomXmlParser() {
    }

    private static String elemToStr(Element ex) {
        return ex.getTextContent() == null ? "" : ex.getTextContent().trim();
    }

    private static List<Element> getElementChildren(Node profile) {
        NodeList childList = profile.getChildNodes();
        List<Element> a = new ArrayList<>();
        for (int k = 0; k < childList.getLength(); k++) {
            Element c = toElement(childList.item(k));
            if (c != null) {
                a.add(c);
            }
        }
        return a;
    }

    private static List<Element> toElements(NodeList parentChildList) {
        List<Element> a = new ArrayList<>();
        for (int j = 0; j < parentChildList.getLength(); j++) {
            Element parElem = toElement(parentChildList.item(j));
            if (parElem != null) {
                a.add(parElem);
            }
        }
        return a;
    }

    private static Element toElement(Node n) {
        if (n instanceof Element) {
            return (Element) n;
        }
        return null;
    }

    private static Element toElement(Node n, String name) {
        if (n instanceof Element) {
            if (((Element) n).getTagName().equals(name)) {
                return (Element) n;
            }
        }
        return null;
    }

    public static NPomPropertyNode parseProperty(Element property, PomDomVisitor visitor, PomDomVisitorContext context) {
        if (visitor != null) {
            visitor.visitStartProperty(property, context);
        }
        NPomProperty p = new NPomProperty(property.getTagName(), elemToStr(property));
        PomPropertyObj o = new PomPropertyObj(property, p, context.getDocument());
        if (visitor != null) {
            visitor.visitEndProperty(o, context);
        }
        return o;
    }

    public static NPomPropertiesNode parseProperties(Element properties, PomDomVisitor visitor, PomDomVisitorContext context, PomMode mode) {
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitStartProperties(properties, context);
            }
        }
        List<NPomPropertyNode> props = new ArrayList<>();
        NodeList propsChildList = properties.getChildNodes();
        for (int j = 0; j < propsChildList.getLength(); j++) {
            Element parElem = toElement(propsChildList.item(j));
            if (parElem != null) {
                props.add(parseProperty(parElem, visitor, context));
            }
        }
        PomPropertiesObj o = new PomPropertiesObj(properties, props, context.getDocument());
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitEndProperties(o, context);
            }
        }
        return o;
    }

    public static NPomRepositoriesNode parseRepositories(Element elem1, PomDomVisitor visitor, PomDomVisitorContext context, PomMode mode) {
        List<NPomRepositoryNode> repositories = new ArrayList<>();
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitStartRepositories(elem1, context);
            } else if (mode == PomMode.ROOT_PLUGIN) {
                visitor.visitStartRepositories(elem1, context);
            }
        }
        NodeList dependenciesChildList = elem1.getChildNodes();
        String childName = "repository";
        if (mode == PomMode.ROOT_PLUGIN) {
            childName = "pluginRepository";
        }
        for (int j = 0; j < dependenciesChildList.getLength(); j++) {
            Element repository = toElement(dependenciesChildList.item(j), childName);
            if (repository != null) {
                NPomRepositoryNode repo = parseRepository(repository, visitor, context, mode);
                repositories.add(repo);
            }
        }
        PomRepositoriesObj d = new PomRepositoriesObj(elem1, repositories, context.getDocument());
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitEndRepositories(d, context);
            } else if (mode == PomMode.ROOT_PLUGIN) {
                visitor.visitEndPluginRepositories(d, context);
            }
        }
        return d;
    }

    public static NPomDependenciesNode parseDependencies(Element elem1, PomDomVisitor visitor, PomDomVisitorContext context, OsAndArch osAndArch, PomMode mode) {
        List<NPomDependencyNode> dependencies = new ArrayList<>();
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitStartDependencies(elem1, context);
            } else if (mode == PomMode.ROOT_DEPENDENCY_MANAGEMENT) {
                visitor.visitStartDependenciesManagement(elem1, context);
            }
        }
        NodeList dependenciesChildList = elem1.getChildNodes();
        for (int j = 0; j < dependenciesChildList.getLength(); j++) {
            Element dependencyElem = toElement(dependenciesChildList.item(j), "dependency");
            if (dependencyElem != null) {
                NPomDependencyNode dep = parseDependency(dependencyElem, osAndArch, visitor, context, mode);
                dependencies.add(dep);
            }
        }
        PomDependenciesObj d = new PomDependenciesObj(elem1, dependencies, context.getDocument());
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitEndDependencies(d, context);
            } else if (mode == PomMode.ROOT_DEPENDENCY_MANAGEMENT) {
                visitor.visitEndDependenciesManagement(d, context);
            }
        }
        return d;
    }

    public static NPomDependencyNode parseDependency(Element dependency, OsAndArch props, PomDomVisitor visitor, PomDomVisitorContext context, PomMode mode) {
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitStartDependency(dependency, context);
            }
        }

        NodeList dependencyChildList = dependency.getChildNodes();
        String d_groupId = "";
        String d_artifactId = "";
        String d_version = "";
        String d_classifier = "";
        String d_scope = "";
        String d_optional = "";
        String d_type = "";
        List<NPomId> d_exclusions = new ArrayList<>();
        for (int k = 0; k < dependencyChildList.getLength(); k++) {
            Element c = toElement(dependencyChildList.item(k));
            if (c != null) {
                switch (c.getTagName()) {
                    case "groupId": {
                        d_groupId = elemToStr(c);
                        break;
                    }
                    case "artifactId": {
                        d_artifactId = elemToStr(c);
                        break;
                    }
                    case "classifier": {
                        d_classifier = elemToStr(c);
                        break;
                    }
                    case "version": {
                        d_version = elemToStr(c);
                        break;
                    }
                    case "scope": {
                        d_scope = elemToStr(c);
                        break;
                    }
                    case "optional": {
                        d_optional = elemToStr(c);
                        break;
                    }
                    case "type": {
                        d_type = elemToStr(c);
                        break;
                    }
                    case "exclusions": {
                        NodeList exclusionsList = c.getChildNodes();
                        for (int l = 0; l < exclusionsList.getLength(); l++) {
                            Element ex = toElement(exclusionsList.item(l), "exclusion");
                            if (ex != null) {
                                String ex_groupId = "";
                                String ex_artifactId = "";
                                NodeList exclusionsList2 = ex.getChildNodes();
                                for (int m = 0; m < exclusionsList2.getLength(); m++) {
                                    Element ex2 = toElement(exclusionsList2.item(m));
                                    if (ex2 != null) {
                                        switch (ex2.getTagName()) {
                                            case "groupId": {
                                                ex_groupId = elemToStr(ex2);
                                                break;
                                            }
                                            case "artifactId": {
                                                ex_artifactId = elemToStr(ex2);
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (!ex_groupId.isEmpty()) {
                                    d_exclusions.add(new NPomId(ex_groupId, ex_artifactId, null));
                                }
                            }
                        }

                        break;
                    }
                }
            }
        }
        if (d_scope.isEmpty()) {
            d_scope = "compile";
        }
        NId id = NIdBuilder.of(d_groupId, d_artifactId).build();
        NPomDependency pomDependency = new NPomDependency(
                d_groupId, d_artifactId, d_classifier, d_version, d_scope, d_optional,
                props == null ? null : props.getOs(id),
                props == null ? null : props.getArch(id),
                d_type,
                d_exclusions.toArray(new NPomId[0])
        );
        NPomDependencyNode d = new PomDependencyObj(dependency, pomDependency, context.getDocument());
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitEndDependency(d, context);
            }
        }
        return d;
    }

    public static NPomRepositoryPolicy parseRepositoryPolicy(Element dependency) {
        NodeList childList = dependency.getChildNodes();
        String enabled = "";
        String updatePolicy = "";
        String checksumPolicy = "";
        for (int k = 0; k < childList.getLength(); k++) {
            Element c = toElement(childList.item(k));
            if (c != null) {
                switch (c.getTagName()) {
                    case "enabled": {
                        enabled = elemToStr(c);
                        break;
                    }
                    case "updatePolicy": {
                        updatePolicy = elemToStr(c);
                        break;
                    }
                    case "checksumPolicy": {
                        checksumPolicy = elemToStr(c);
                        break;
                    }
                }
            }
        }
        return new NPomRepositoryPolicy(
                enabled.isEmpty() || Boolean.parseBoolean(enabled), updatePolicy, checksumPolicy
        );
    }


    public static NPomProfileNode parseProfile(Element profile, PomDomVisitor visitor, PomDomVisitorContext context, OsAndArch osAndArch, PomMode mode) {
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitStartProfile(profile, context);
            }
        }
        NPomProfile pomProfile = new NPomProfile();
        List<NPomDependencyNode> dependencies = new ArrayList<>();
        List<String> modules = new ArrayList<>();
        List<NPomDependencyNode> dependenciesManagement = new ArrayList<>();
        List<NPomRepositoryNode> repositories = new ArrayList<>();
        List<NPomRepositoryNode> pluginRepositories = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        for (Element elem1 : getElementChildren(profile)) {
            switch (elem1.getTagName()) {
                case "id": {
                    pomProfile.setId(elemToStr(elem1));
                    break;
                }
                case "activation": {
                    NPomProfileActivation a = new NPomProfileActivation();
                    pomProfile.setActivation(a);
                    for (Element cc : getElementChildren(elem1)) {
                        switch (cc.getTagName()) {
                            case "jdk": {
                                a.setJdk(elemToStr(cc));
                                break;
                            }
                            case "property": {
                                for (Element ccc : getElementChildren(cc)) {
                                    switch (ccc.getTagName()) {
                                        case "name": {
                                            a.setPropertyName(elemToStr(ccc));
                                            break;
                                        }
                                        case "value": {
                                            a.setPropertyValue(elemToStr(ccc));
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                            case "file": {
                                a.setFile(elemToStr(cc));
                                break;
                            }
                            case "activeByDefault": {
                                String s = NStringUtils.trim(elemToStr(cc));
                                a.setActiveByDefault(s.equalsIgnoreCase("true"));
                                break;
                            }
                            case "os": {
                                for (Element ccc : getElementChildren(cc)) {
                                    switch (ccc.getTagName()) {
                                        case "name": {
                                            a.setOsName(elemToStr(ccc));
                                            break;
                                        }
                                        case "arch": {
                                            a.setOsArch(elemToStr(ccc));
                                            break;
                                        }
                                        case "version": {
                                            a.setOsVersion(elemToStr(ccc));
                                            break;
                                        }
                                        case "family": {
                                            a.setOsFamily(elemToStr(ccc));
                                            break;
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                    break;
                }
                case "modules": {
                    NodeList childList = elem1.getChildNodes();
                    for (int j = 0; j < childList.getLength(); j++) {
                        Element parElem = toElement(childList.item(j), "module");
                        if (parElem != null) {
                            String s = elemToStr(parElem);
                            if (!s.isEmpty()) {
                                modules.add(s);
                            }
                        }
                    }
                    break;
                }
                case "properties": {
                    for (NPomPropertyNode p : parseProperties(elem1, visitor, context, PomMode.PROFILE).getObject()) {
                        properties.put(p.getObject().getName(), p.getObject().getValue());
                    }
                    break;
                }
                case "dependencyManagement": {
                    NodeList dependenciesChildList = elem1.getChildNodes();
                    for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                        Element dependenciesElem = toElement(dependenciesChildList.item(j), "dependencies");
                        if (dependenciesElem != null) {
                            dependenciesManagement.addAll(parseDependencies(dependenciesElem, visitor, context, osAndArch, PomMode.PROFILE_DEPENDENCY_MANAGEMENT).getObject());
                        }
                    }
                    break;
                }
                case "dependencies": {
                    dependencies.addAll(parseDependencies(elem1, visitor, context, osAndArch, PomMode.PROFILE).getObject());
                    break;
                }
                case "repositories": {
                    repositories.addAll(parseRepositories(elem1, visitor, context, PomMode.PROFILE).getObject());
                    break;
                }
                case "pluginRepositories": {
//                    if (visitor != null) {
//                        visitor.visitStartPluginRepositories(elem1);
//                    }
                    NodeList dependenciesChildList = elem1.getChildNodes();
                    for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                        Element repository = toElement(dependenciesChildList.item(j), "pluginRepository");
                        if (repository != null) {
//                            if (visitor != null) {
//                                visitor.visitStartPluginRepository(repository);
//                            }
                            NPomRepositoryNode repo = parseRepository(repository, visitor, context, mode);
//                            if (visitor != null) {
//                                visitor.visitEndPluginRepository(repository, repo);
//                            }
                            pluginRepositories.add(repo);
                        }
                    }
//                    if (visitor != null) {
//                        visitor.visitEndPluginRepositories(elem1, pluginRepos.toArray(new PomRepository[0]));
//                    }
                    break;
                }
            }
        }

        pomProfile.setDependencies(dependencies.stream().map(x -> x.getObject()).toArray(NPomDependency[]::new));
        pomProfile.setDependenciesManagement(dependenciesManagement.stream().map(x -> x.getObject()).toArray(NPomDependency[]::new));
        pomProfile.setRepositories(repositories.stream().map(x -> x.getObject()).toArray(NPomRepository[]::new));
        pomProfile.setPluginRepositories(pluginRepositories.stream().map(x -> x.getObject()).toArray(NPomRepository[]::new));
        pomProfile.setProperties(properties);
        NPomProfileNode d = new PomProfileObj(profile, pomProfile, context.getDocument());
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitEndProfile(d, context);
            }
        }
        return d;
    }

    public static NPomRepositoryNode parseRepository(Element repository, PomDomVisitor visitor, PomDomVisitorContext context, PomMode mode) {
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitStartRepositories(repository, context);
            }
        }
        NodeList childList = repository.getChildNodes();
        String id = "";
        String layout = "";
        String url = "";
        String name = "";
        NPomRepositoryPolicy snapshots = null;
        NPomRepositoryPolicy releases = null;
        for (int k = 0; k < childList.getLength(); k++) {
            Element c = toElement(childList.item(k));
            if (c != null) {
                switch (c.getTagName()) {
                    case "id": {
                        id = elemToStr(c);
                        break;
                    }
                    case "layout": {
                        layout = elemToStr(c);
                        break;
                    }
                    case "url": {
                        url = elemToStr(c);
                        break;
                    }
                    case "name": {
                        name = elemToStr(c);
                        break;
                    }
                    case "snapshots": {
                        snapshots = parseRepositoryPolicy(c);
                        break;
                    }
                    case "releases": {
                        releases = parseRepositoryPolicy(c);
                        break;
                    }
                }
            }
        }
        if (name.isEmpty()) {
            name = "compile";
        }
        NPomRepository r = new NPomRepository(id, layout, url, name, releases, snapshots);
        PomRepositoryObj d = new PomRepositoryObj(repository, r, context.getDocument());
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitEndRepository(d, context);
            }
        }
        return d;
    }

    public static Element createExclusionElement(Document doc, NPomId exclusionId) {
        Element e = doc.createElement("exclusion");
        e.appendChild(createNameTextTag(doc, "groupId", exclusionId.getGroupId()));
        e.appendChild(createNameTextTag(doc, "artifactId", exclusionId.getArtifactId()));
        if (exclusionId.getVersion() != null && exclusionId.getVersion().trim().length() > 0) {
            e.appendChild(createNameTextTag(doc, "version", exclusionId.getVersion()));
        }
        return e;
    }

    public static Element createDependencyElement(Document doc, NPomDependency dep) {
        Element dependency = doc.createElement("dependency");
        dependency.appendChild(createNameTextTag(doc, "groupId", dep.getGroupId()));
        dependency.appendChild(createNameTextTag(doc, "artifactId", dep.getArtifactId()));
        if (dep.getVersion() != null && dep.getVersion().trim().length() > 0) {
            dependency.appendChild(createNameTextTag(doc, "version", dep.getVersion()));
        }
        if (dep.getOptional() != null && dep.getOptional().trim().length() > 0) {
            dependency.appendChild(createNameTextTag(doc, "optional", dep.getOptional()));
        }
        if (dep.getType() != null && dep.getType().trim().length() > 0) {
            dependency.appendChild(createNameTextTag(doc, "type", dep.getType()));
        }
        NPomId[] e = dep.getExclusions();
        if (e.length > 0) {
            Element exclusions = doc.createElement("exclusions");
            dependency.appendChild(exclusions);
            for (NPomId pomId : e) {
                exclusions.appendChild(createExclusionElement(doc, pomId));
            }
        }
        return dependency;
    }

    public static Element createRepositoryElement(Document doc, NPomRepository repo) {
        return createRepositoryElement(doc, repo, "repository");
    }

    public static Element createPluginRepositoryElement(Document doc, NPomRepository repo) {
        return createRepositoryElement(doc, repo, "pluginRepository");
    }

    public static Element createRepositoryPolicy(Document doc, NPomRepositoryPolicy repo, String name) {
        Element snapshots = doc.createElement(name);
        snapshots.appendChild(createNameTextTag(doc, "enabled", String.valueOf(repo.isEnabled())));
        if (repo.getUpdatePolicy() != null && repo.getUpdatePolicy().trim().length() > 0) {
            snapshots.appendChild(createNameTextTag(doc, "updatePolicy", repo.getUpdatePolicy()));
        }
        if (repo.getChecksumPolicy() != null && repo.getChecksumPolicy().trim().length() > 0) {
            snapshots.appendChild(createNameTextTag(doc, "checksumPolicy", repo.getChecksumPolicy()));
        }
        return snapshots;
    }

    public static Element createNameTextTag(Document doc, String name, String value) {
        Element elem = doc.createElement(name);
        elem.appendChild(doc.createTextNode(value));
        return elem;
    }

    public static Element createRepositoryElement(Document doc, NPomRepository repo, String name) {
        Element repository = doc.createElement(name);
        repository.appendChild(createNameTextTag(doc, "id", repo.getId()));
        repository.appendChild(createNameTextTag(doc, "url", repo.getUrl()));
        if (repo.getLayout() != null && repo.getLayout().trim().length() > 0) {
            repository.appendChild(createNameTextTag(doc, "layout", repo.getLayout()));
        }
        if (repo.getName() != null && repo.getName().trim().length() > 0) {
            repository.appendChild(createNameTextTag(doc, "name", repo.getName()));
        }
        if (repo.getSnapshots() != null) {
            repository.appendChild(createRepositoryPolicy(doc, repo.getSnapshots(), "snapshots"));
        }
        if (repo.getReleases() != null) {
            repository.appendChild(createRepositoryPolicy(doc, repo.getReleases(), "releases"));
        }
        return repository;
    }

    public static void writeDocument(Document doc, File result)  {
        writeDocument(doc, new StreamResult(result));
    }

    public static void writeDocument(Document doc, Writer result)  {
        writeDocument(doc, new StreamResult(result));
    }

    public static void writeDocument(Document doc, OutputStream result)  {
        writeDocument(doc, new StreamResult(result));
    }

    public static void writeDocument(Document doc, StreamResult result)  {
        XmlUtils.writeDocument(doc, result, false, true);
    }

    public static boolean appendOrReplaceDependency(NPomDependency dependency, Element dependencyElement, Element dependenciesElement, Map<String, String> props, PomDomVisitor visitor, PomDomVisitorContext context, PomMode mode) {
        if (dependencyElement != null && dependenciesElement == null) {
            dependenciesElement = (Element) dependencyElement.getParentNode();
        }
        Document doc = dependenciesElement.getOwnerDocument();
        if (dependencyElement == null) {
            dependenciesElement.appendChild(createDependencyElement(doc, dependency));
            return true;
        } else {
            NPomDependencyNode old = parseDependency(dependencyElement, new OsAndArch(props), visitor, context, mode);
            if (old == null || !old.getObject().equals(dependency)) {
                dependenciesElement.replaceChild(createDependencyElement(doc, dependency), dependencyElement);
                return true;
            }
            return false;
        }
    }

    public static boolean appendOrReplaceRepository(NPomRepository repository, Element repositoryElement, Element repositoriesElement, PomDomVisitor visitor, PomDomVisitorContext context, PomMode mode) {
        if (repositoryElement != null && repositoriesElement == null) {
            repositoriesElement = (Element) repositoryElement.getParentNode();
        }
        Document doc = repositoriesElement.getOwnerDocument();
        if (repositoryElement == null) {
            repositoriesElement.appendChild(createRepositoryElement(doc, repository));
            return true;
        } else {
            NPomRepositoryNode old = parseRepository(repositoryElement, visitor, context, mode);
            if (old == null || !old.getObject().equals(repository)) {
                repositoriesElement.replaceChild(createRepositoryElement(doc, repository), repositoryElement);
                return true;
            }
            return false;
        }
    }

    public NPom parse(URL url) throws IOException, SAXException, ParserConfigurationException {
        return parse(url, null);
    }

    public NPom parse(URL url, PomDomVisitor visitor) {
        try {
            try (InputStream is = DefaultNWebCli.prepareGlobalOpenStream(url)) {
                return parse(is, visitor);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public NPom parse(URI uri) {
        return parse(uri, null);
    }

    public NPom parse(URI uri, PomDomVisitor visitor) {
        try {
            try (InputStream is = DefaultNWebCli.prepareGlobalOpenStream(uri.toURL())) {
                return parse(is, visitor);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public NPom parse(File file) {
        return parse(file, null);
    }

    public NPom parse(NPath file, PomDomVisitor visitor) {
        try {
            try (InputStream is = file.getInputStream()) {
                return parse(is, visitor);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }

    }

    public NPom parse(File file, PomDomVisitor visitor) {
        try {
            try (InputStream is = new FileInputStream(file)) {
                return parse(is, visitor);
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public NPom parse(InputStream stream) {
        return parse(stream, null);
    }

    public NPom parse(InputStream stream, PomDomVisitor visitor) {
        try {
            Document doc = XmlUtils.createDocumentBuilder(true).parse(preValidateStream(stream));
            return parse(doc, visitor);
        } catch (IOException ex) {
            throw new NIOException(ex);
        } catch (SAXException ex) {
            throw new NParseException(NMsg.ofC("error parsing %s", stream), ex);
        }
    }

    private byte[] loadAllBytes(InputStream in) {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
        try {
            int size = in.available();
            if (size <= 4096) {
                size = 4096;
            }
            byte[] b = new byte[size];
            int count;
            while ((count = in.read(b)) > 0) {
                o.write(b, 0, count);
            }
            return o.toByteArray();
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    private InputStream preValidateStream(InputStream in) {
        byte[] bytes0 = loadAllBytes(in);
        int skip = 0;
        while (skip < bytes0.length && Character.isWhitespace(bytes0[skip])) {
            skip++;
        }
        String x = new String(bytes0, skip, bytes0.length - skip);
        return new ByteArrayInputStream(XmlEscaper.escapeToCode(x).getBytes());
    }

    public NPom parse(Document doc) {
        return parse(doc, null);
    }

    public NPom parse(Document doc, PomDomVisitor visitor) {
        List<NPomDependencyNode> deps = new ArrayList<>();
        List<NPomDependencyNode> depsMan = new ArrayList<>();
        List<NPomRepositoryNode> repos = new ArrayList<>();
        List<NPomRepositoryNode> pluginRepos = new ArrayList<>();
        List<NPomProfileNode> profiles = new ArrayList<>();
        List<NPomContributor> contributors = new ArrayList<>();
        List<NPomContributor> developers = new ArrayList<>();
        List<NPomLicense> licenses = new ArrayList<>();
        //optional, but recommended
        //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        doc.getDocumentElement().normalize();
        NodeList rootChildList = doc.getDocumentElement().getChildNodes();
        String groupId = "";
        String inceptionYear = "";
        String url = "";
        String artifactId = "";
        String description = "";
        String name = "";
        List<String> modules = new ArrayList<>();
        String version = "";
        String packaging = "";
        NPomId parentId = null;
        DefaultPomDomVisitorContext context = new DefaultPomDomVisitorContext().setDocument(doc);
        if (visitor != null) {
            visitor.visitStartDocument(doc, context);
        }
        Map<String, String> props = new LinkedHashMap<>();
        for (int i = 0; i < rootChildList.getLength(); i++) {
            Element elem1 = toElement(rootChildList.item(i));
            if (elem1 != null) {
                switch (elem1.getTagName()) {
                    case "groupId": {
                        groupId = elemToStr(elem1);
                        break;
                    }
                    case "artifactId": {
                        artifactId = elemToStr(elem1);
                        break;
                    }
                    case "version": {
                        version = elemToStr(elem1);
                        break;
                    }
                    case "packaging": {
                        packaging = elemToStr(elem1);
                        break;
                    }
                    case "description": {
                        description = elemToStr(elem1);
                        break;
                    }
                    case "name": {
                        name = elemToStr(elem1);
                        break;
                    }
                    case "inceptionYear": {
                        inceptionYear = elemToStr(elem1);
                        break;
                    }
                    case "url": {
                        url = elemToStr(elem1);
                        break;
                    }
                    case "contributors": {
                        for (Element contributor : toElements(elem1.getChildNodes())) {
                            if ("contributor".equals(contributor.getTagName())) {
                                contributors.add(parseContributor(contributor));
                            }
                        }
                        break;
                    }
                    case "developers": {
                        for (Element contributor : toElements(elem1.getChildNodes())) {
                            if ("developer".equals(contributor.getTagName())) {
                                developers.add(parseContributor(contributor));
                            }
                        }
                        break;
                    }
                    case "licenses": {
                        for (Element contributor : toElements(elem1.getChildNodes())) {
                            if ("license".equals(contributor.getTagName())) {
                                NPomLicense cc = new NPomLicense();
                                for (Element parElem : toElements(contributor.getChildNodes())) {
                                    switch (parElem.getTagName()) {
                                        case "name": {
                                            cc.setName(elemToStr(parElem));
                                            break;
                                        }
                                        case "url": {
                                            cc.setUrl(elemToStr(parElem));
                                            break;
                                        }
                                        case "distribution": {
                                            cc.setDistribution(elemToStr(parElem));
                                            break;
                                        }
                                        case "comments": {
                                            cc.setComments(elemToStr(parElem));
                                            break;
                                        }
                                    }
                                }
                                licenses.add(cc);
                            }
                        }
                        break;
                    }
                    case "parent": {
                        NodeList parentChildList = elem1.getChildNodes();
                        String p_groupId = "";
                        String p_artifactId = "";
                        String p_version = "";
                        for (int j = 0; j < parentChildList.getLength(); j++) {
                            Element parElem = toElement(parentChildList.item(j));
                            if (parElem != null) {
                                switch (parElem.getTagName()) {
                                    case "groupId": {
                                        p_groupId = elemToStr(parElem);
                                        break;
                                    }
                                    case "artifactId": {
                                        p_artifactId = elemToStr(parElem);
                                        break;
                                    }
                                    case "version": {
                                        p_version = elemToStr(parElem);
                                        break;
                                    }
                                }
                            }
                        }
                        if (p_groupId.length() > 0 || p_artifactId.length() > 0 || p_version.length() > 0) {
                            parentId = new NPomId(p_groupId, p_artifactId, p_version);
                        }
                        break;
                    }
                    case "modules": {
                        NodeList childList = elem1.getChildNodes();
                        for (int j = 0; j < childList.getLength(); j++) {
                            Element parElem = toElement(childList.item(j), "module");
                            if (parElem != null) {
                                String s = elemToStr(parElem);
                                if (!s.isEmpty()) {
                                    modules.add(s);
                                }
                            }
                        }
                        break;
                    }
                    case "properties": {
                        for (NPomPropertyNode pomPropertyObj : parseProperties(elem1, visitor, context, PomMode.ROOT).getObject()) {
                            props.put(pomPropertyObj.getObject().getName(), pomPropertyObj.getObject().getValue());
                        }
                        break;
                    }
                    case "dependencyManagement": {
                        OsAndArch osAndArch = new OsAndArch(props);
                        NodeList dependenciesChildList = elem1.getChildNodes();
                        for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                            Element dependenciesElem = toElement(dependenciesChildList.item(j), "dependencies");
                            if (dependenciesElem != null) {
                                depsMan.addAll(parseDependencies(dependenciesElem, visitor, context, osAndArch, PomMode.ROOT_DEPENDENCY_MANAGEMENT).getObject());
                            }
                        }


                        break;
                    }
                    case "dependencies": {
                        OsAndArch osAndArch = new OsAndArch(props);
                        deps.addAll(parseDependencies(elem1, visitor, context, osAndArch, PomMode.ROOT).getObject());
                        break;
                    }
                    case "repositories": {
                        repos.addAll(parseRepositories(elem1, visitor, context, PomMode.ROOT).getObject());
                        break;
                    }
                    case "pluginRepositories": {
                        pluginRepos.addAll(parseRepositories(elem1, visitor, context, PomMode.ROOT_PLUGIN).getObject());
                        break;
                    }
                    case "profiles": {
                        OsAndArch osAndArch = new OsAndArch(props);
                        profiles.addAll(parseProfiles(elem1, visitor, context, osAndArch, PomMode.ROOT).getObject());
                        break;
                    }
                }
            }
        }
        NPom pom = new NPom(
                groupId, artifactId, version, packaging, parentId,
                name,
                description,
                url, inceptionYear,
                props,
                deps.stream().map(x -> x.getObject()).toArray(NPomDependency[]::new),
                depsMan.stream().map(x -> x.getObject()).toArray(NPomDependency[]::new),
                repos.stream().map(x -> x.getObject()).toArray(NPomRepository[]::new),
                pluginRepos.stream().map(x -> x.getObject()).toArray(NPomRepository[]::new),
                modules.toArray(new String[0]),
                profiles.stream().map(x -> x.getObject()).toArray(NPomProfile[]::new),
                contributors.toArray(new NPomContributor[0]),
                developers.toArray(new NPomContributor[0]),
                licenses.toArray(new NPomLicense[0]),
                doc
        );
        if (visitor != null) {
            visitor.visitEndDocument(doc, pom, context);
        }

        return pom;
    }

    private NPomContributor parseContributor(Element contributor) {
        NPomContributor cc = new NPomContributor();
        for (Element parElem : toElements(contributor.getChildNodes())) {
            switch (parElem.getTagName()) {
                case "name": {
                    cc.setName(elemToStr(parElem));
                    break;
                }
                case "email": {
                    cc.setEmail(elemToStr(parElem));
                    break;
                }
                case "organization": {
                    cc.setOrganization(elemToStr(parElem));
                    break;
                }
                case "organizationUrl": {
                    cc.setOrganizationUrl(elemToStr(parElem));
                    break;
                }
                case "url": {
                    cc.setUrl(elemToStr(parElem));
                    break;
                }
                case "timezone": {
                    cc.setTimeZone(elemToStr(parElem));
                    break;
                }
                case "roles": {
                    for (Element r : toElements(parElem.getChildNodes())) {
                        if ("role".equals(r.getTagName())) {
                            String v = elemToStr(parElem);
                            if (cc.getRoles() == null) {
                                cc.setRoles(new ArrayList<>());
                            }
                            cc.getRoles().add(v);
                        }
                    }
                    break;
                }
                case "properties": {
                    for (Element r : toElements(parElem.getChildNodes())) {
                        String n = r.getTagName();
                        String v = elemToStr(parElem);
                        if (cc.getProperties() == null) {
                            cc.setProperties(new LinkedHashMap<>());
                        }
                        cc.getProperties().put(n, v);
                    }
                    break;
                }
            }
        }
        return cc;
    }

    private NPomProfilesNode parseProfiles(Element elem1, PomDomVisitor visitor, DefaultPomDomVisitorContext context, OsAndArch osAndArch, PomMode mode) {
        List<NPomProfileNode> profiles = new ArrayList<>();
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitStartProfiles(elem1, context);
            }
        }
        NodeList childList = elem1.getChildNodes();
        for (int j = 0; j < childList.getLength(); j++) {
            Element profile = toElement(childList.item(j), "profile");
            if (profile != null) {
                NPomProfileNode pomProfileObj = parseProfile(profile, visitor, context, osAndArch, mode);
                profiles.add(pomProfileObj);
            }
        }
        PomProfilesObj d = new PomProfilesObj(elem1, profiles, context.getDocument());
        if (visitor != null) {
            if (mode == PomMode.ROOT) {
                visitor.visitEndProfiles(d, context);
            }
        }
        return d;
    }

    private static class OsAndArch {
        Map<String, String> osMap = new HashMap<>();
        Map<String, String> archMap = new HashMap<>();

        public OsAndArch(Map<String, String> props) {
            for (Map.Entry<String, String> entry : props.entrySet()) {
                Matcher m = NUTS_OS_ARCH_DEPS_PATTERN.matcher(entry.getKey());
                if (m.find()) {
                    String os = m.group("os");
                    String arch = m.group("arch");
                    String txt = entry.getValue().trim();
                    for (String a : StringTokenizerUtils.splitDefault(txt.trim())) {
                        a = a.trim();
                        if (a.startsWith("#")) {
                            //ignore!
                        } else {
                            NId id = NId.get(a).orNull();
                            if (id != null) {
                                if (!NBlankable.isBlank(os)) {
                                    osMap.put(id.getShortName(), os);
                                }
                                if (!NBlankable.isBlank(arch)) {
                                    archMap.put(id.getShortName(), arch);
                                }
                            }
                        }
                    }
                }
            }
        }

        public String getOs(NId id) {
            return osMap.get(id.getShortName());
        }

        public String getArch(NId id) {
            return archMap.get(id.getShortName());
        }
    }

    private enum PomMode {
        ROOT,
        PROFILE_REPOSITORIES,
        PROFILE_DEPENDENCY_MANAGEMENT,
        ROOT_DEPENDENCY_MANAGEMENT,
        PROFILE_DEPENDENCIES, PROFILE, ROOT_PLUGIN,
    }
}
