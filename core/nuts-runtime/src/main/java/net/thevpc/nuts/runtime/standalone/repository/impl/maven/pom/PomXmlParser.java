package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.runtime.standalone.util.XmlEscaper;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PomXmlParser {
    public static final Pattern NUTS_OS_ARCH_DEPS_PATTERN = Pattern.compile("^nuts([.](?<os>[a-zA-Z0-9-_]+)-os)?([.](?<arch>[a-zA-Z0-9-_]+)-arch)?-dependencies$");


    private final NutsSession session;

    public PomXmlParser(NutsSession session) {
        this.session = session;
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

    public static Map<String, String> parseProperties(Element properties) {
        Map<String, String> props = new HashMap<>();
        NodeList propsChildList = properties.getChildNodes();
        for (int j = 0; j < propsChildList.getLength(); j++) {
            Element parElem = toElement(propsChildList.item(j));
            if (parElem != null) {
                props.put(parElem.getTagName(), elemToStr(parElem));
            }
        }
        return props;
    }

    public static PomDependency parseDependency(Element dependency, OsAndArch props, NutsSession session) {
        NodeList dependencyChildList = dependency.getChildNodes();
        String d_groupId = "";
        String d_artifactId = "";
        String d_version = "";
        String d_classifier = "";
        String d_scope = "";
        String d_optional = "";
        String d_type = "";
        List<PomId> d_exclusions = new ArrayList<>();
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
                                    d_exclusions.add(new PomId(ex_groupId, ex_artifactId, null));
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
        NutsId id = new DefaultNutsIdBuilder().setGroupId(d_groupId).setArtifactId(d_artifactId).build();
        return new PomDependency(
                d_groupId, d_artifactId, d_classifier, d_version, d_scope, d_optional,
                props == null ? null : props.getOs(id),
                props == null ? null : props.getArch(id),
                d_type,
                d_exclusions.toArray(new PomId[0])
        );
    }

    public static PomRepositoryPolicy parseRepositoryPolicy(Element dependency) {
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
        return new PomRepositoryPolicy(
                enabled.isEmpty() || Boolean.parseBoolean(enabled), updatePolicy, checksumPolicy
        );
    }


    public static PomProfile parseProfile(Element profile, NutsSession session) {
        PomProfile pomProfile = new PomProfile();
        List<PomDependency> dependencies = new ArrayList<>();
        List<String> modules = new ArrayList<>();
        List<PomDependency> dependenciesManagement = new ArrayList<>();
        List<PomRepository> repositories = new ArrayList<>();
        List<PomRepository> pluginRepositories = new ArrayList<>();
        Map<String, String> properties = new HashMap<>();

        for (Element elem1 : getElementChildren(profile)) {
            switch (elem1.getTagName()) {
                case "id": {
                    pomProfile.setId(elemToStr(elem1));
                    break;
                }
                case "activation": {
                    PomProfileActivation a = new PomProfileActivation();
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
                                String s = NutsUtilStrings.trim(elemToStr(cc));
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
//                    if (visitor != null) {
//                        visitor.visitStartProperties(elem1);
//                    }
                    properties.putAll(parseProperties(elem1));
//                    if (visitor != null) {
//                        visitor.visitEndProperties(elem1, props);
//                    }
                    break;
                }
                case "dependencyManagement": {
//                    if (visitor != null) {
//                        visitor.visitStartDependenciesManagement(elem1);
//                    }
                    NodeList dependenciesChildList = elem1.getChildNodes();
                    for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                        Element dependenciesElem = toElement(dependenciesChildList.item(j), "dependencies");
                        if (dependenciesElem != null) {
                            NodeList dependenciesChildList2 = dependenciesElem.getChildNodes();
                            for (int k = 0; k < dependenciesChildList2.getLength(); k++) {
                                Element dependency2 = toElement(dependenciesChildList2.item(k), "dependency");
                                if (dependency2 != null) {
//                                    if (visitor != null) {
//                                        visitor.visitStartDependencyManagement(dependency2);
//                                    }
                                    HashMap<String, String> props = new HashMap<>();
                                    PomDependency dep = parseDependency(dependency2, new OsAndArch(props, session), session);
//                                    if (visitor != null) {
//                                        visitor.visitEndDependencyManagement(dependency2, dep);
//                                    }

                                    dependenciesManagement.add(dep);
                                }
                            }
                        }
                    }
//                    if (visitor != null) {
//                        visitor.visitEndDependenciesManagement(elem1, deps.toArray(new PomDependency[0]));
//                    }
                    break;
                }
                case "dependencies": {
//                    if (visitor != null) {
//                        visitor.visitStartDependencies(elem1);
//                    }
                    NodeList dependenciesChildList = elem1.getChildNodes();
                    for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                        Element dependencyElem = toElement(dependenciesChildList.item(j), "dependency");
                        if (dependencyElem != null) {
//                            if (visitor != null) {
//                                visitor.visitStartDependency(dependencyElem);
//                            }
                            HashMap<String, String> props = new HashMap<>();
                            PomDependency dep = parseDependency(dependencyElem, new OsAndArch(props, session), session);
//                            if (visitor != null) {
//                                visitor.visitEndDependency(dependencyElem, dep);
//                            }
                            dependencies.add(dep);
                        }
                    }
//                    if (visitor != null) {
//                        visitor.visitEndDependencies(elem1, deps.toArray(new PomDependency[0]));
//                    }
                    break;
                }
                case "repositories": {
//                    if (visitor != null) {
//                        visitor.visitStartRepositories(elem1);
//                    }
                    NodeList dependenciesChildList = elem1.getChildNodes();
                    for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                        Element repository = toElement(dependenciesChildList.item(j), "repository");
                        if (repository != null) {
//                            if (visitor != null) {
//                                visitor.visitStartRepository(repository);
//                            }
                            PomRepository repo = parseRepository(repository);
//                            if (visitor != null) {
//                                visitor.visitEndRepository(repository, repo);
//                            }
                            repositories.add(repo);
                        }
                    }
//                    if (visitor != null) {
//                        visitor.visitEndRepositories(elem1, repos.toArray(new PomRepository[0]));
//                    }
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
                            PomRepository repo = parseRepository(repository);
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

        pomProfile.setDependencies(dependencies.toArray(new PomDependency[0]));
        pomProfile.setDependenciesManagement(dependenciesManagement.toArray(new PomDependency[0]));
        pomProfile.setRepositories(repositories.toArray(new PomRepository[0]));
        pomProfile.setPluginRepositories(pluginRepositories.toArray(new PomRepository[0]));
        pomProfile.setProperties(properties);
        return pomProfile;
    }

    public static PomRepository parseRepository(Element repository) {
        NodeList childList = repository.getChildNodes();
        String id = "";
        String layout = "";
        String url = "";
        String name = "";
        PomRepositoryPolicy snapshots = null;
        PomRepositoryPolicy releases = null;
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
        return new PomRepository(
                id, layout, url, name, releases, snapshots
        );
    }

    public static Element createExclusionElement(Document doc, PomId exclusionId) {
        Element e = doc.createElement("exclusion");
        e.appendChild(createNameTextTag(doc, "groupId", exclusionId.getGroupId()));
        e.appendChild(createNameTextTag(doc, "artifactId", exclusionId.getArtifactId()));
        if (exclusionId.getVersion() != null && exclusionId.getVersion().trim().length() > 0) {
            e.appendChild(createNameTextTag(doc, "version", exclusionId.getVersion()));
        }
        return e;
    }

    public static Element createDependencyElement(Document doc, PomDependency dep) {
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
        PomId[] e = dep.getExclusions();
        if (e.length > 0) {
            Element exclusions = doc.createElement("exclusions");
            dependency.appendChild(exclusions);
            for (PomId pomId : e) {
                exclusions.appendChild(createExclusionElement(doc, pomId));
            }
        }
        return dependency;
    }

    public static Element createRepositoryElement(Document doc, PomRepository repo) {
        return createRepositoryElement(doc, repo, "repository");
    }

    public static Element createPluginRepositoryElement(Document doc, PomRepository repo) {
        return createRepositoryElement(doc, repo, "pluginRepository");
    }

    public static Element createRepositoryPolicy(Document doc, PomRepositoryPolicy repo, String name) {
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

    public static Element createRepositoryElement(Document doc, PomRepository repo, String name) {
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

    public static void writeDocument(Document doc, File result, NutsSession session) throws TransformerException {
        writeDocument(doc, new StreamResult(result), session);
    }

    public static void writeDocument(Document doc, Writer result, NutsSession session) throws TransformerException {
        writeDocument(doc, new StreamResult(result), session);
    }

    public static void writeDocument(Document doc, OutputStream result, NutsSession session) throws TransformerException {
        writeDocument(doc, new StreamResult(result), session);
    }

    public static void writeDocument(Document doc, StreamResult result, NutsSession session) throws TransformerException {
        XmlUtils.writeDocument(doc, result, false, true, session);
    }

    public static boolean appendOrReplaceDependency(PomDependency dependency, Element dependencyElement, Element dependenciesElement, Map<String, String> props, NutsSession session) {
        if (dependencyElement != null && dependenciesElement == null) {
            dependenciesElement = (Element) dependencyElement.getParentNode();
        }
        Document doc = dependenciesElement.getOwnerDocument();
        if (dependencyElement == null) {
            dependenciesElement.appendChild(createDependencyElement(doc, dependency));
            return true;
        } else {
            PomDependency old = parseDependency(dependencyElement, new OsAndArch(props, session), session);
            if (old == null || !old.equals(dependency)) {
                dependenciesElement.replaceChild(createDependencyElement(doc, dependency), dependencyElement);
                return true;
            }
            return false;
        }
    }

    public static boolean appendOrReplaceRepository(PomRepository repository, Element repositoryElement, Element repositoriesElement) {
        if (repositoryElement != null && repositoriesElement == null) {
            repositoriesElement = (Element) repositoryElement.getParentNode();
        }
        Document doc = repositoriesElement.getOwnerDocument();
        if (repositoryElement == null) {
            repositoriesElement.appendChild(createRepositoryElement(doc, repository));
            return true;
        } else {
            PomRepository old = parseRepository(repositoryElement);
            if (old == null || !old.equals(repository)) {
                repositoriesElement.replaceChild(createRepositoryElement(doc, repository), repositoryElement);
                return true;
            }
            return false;
        }
    }

    public Pom parse(URL url, NutsSession session) throws IOException, SAXException, ParserConfigurationException {
        return parse(url, null, session);
    }

    public Pom parse(URL url, PomDomVisitor visitor, NutsSession session) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;
        try {
            return parse((is = url.openStream()), visitor, session);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public Pom parse(URI uri, NutsSession session) throws IOException, SAXException, ParserConfigurationException {
        return parse(uri, null, session);
    }

    public Pom parse(URI uri, PomDomVisitor visitor, NutsSession session) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;
        try {
            return parse(is = uri.toURL().openStream(), session);
        } finally {
            if (is != null) {
                is.close();
            }
        }
//        Document doc = createDocumentBuilder().parse(uri.toString());
//        return parse(doc, visitor);
    }

    public Pom parse(File file, NutsSession session) throws IOException, SAXException, ParserConfigurationException {
        return parse(file, null, session);
    }

    public Pom parse(File file, PomDomVisitor visitor, NutsSession session) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;
        try {
            return parse(new FileInputStream(file), session);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        //Document doc = createDocumentBuilder().parse(file);
//        return parse(doc, visitor);
    }

    public Pom parse(InputStream stream, NutsSession session) {
        return parse(stream, null, session);
    }

    public Pom parse(InputStream stream, PomDomVisitor visitor, NutsSession session) {
        try {
            Document doc = XmlUtils.createDocumentBuilder(true, session).parse(preValidateStream(stream, session));
            return parse(doc, visitor, session);
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        } catch (SAXException ex) {
            throw new NutsParseException(session, NutsMessage.plain("parse problem"), ex);
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
            throw new NutsIOException(session, ex);
        }
    }

    private InputStream preValidateStream(InputStream in, NutsSession session) {
        byte[] bytes0 = loadAllBytes(in);
        int skip = 0;
        while (skip < bytes0.length && Character.isWhitespace(bytes0[skip])) {
            skip++;
        }
        String x = new String(bytes0, skip, bytes0.length - skip);
        return new ByteArrayInputStream(XmlEscaper.escapeToCode(x, session).getBytes());
    }

    public Pom parse(Document doc, NutsSession session) {
        return parse(doc, null, session);
    }

    public Pom parse(Document doc, PomDomVisitor visitor, NutsSession session) {
        List<PomDependency> deps = new ArrayList<>();
        List<PomDependency> depsMan = new ArrayList<>();
        List<PomRepository> repos = new ArrayList<>();
        List<PomRepository> pluginRepos = new ArrayList<>();
        List<PomProfile> profiles = new ArrayList<>();
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
        PomId parentId = null;
        if (visitor != null) {
            visitor.visitStartDocument(doc);
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
                            parentId = new PomId(p_groupId, p_artifactId, p_version);
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
                        if (visitor != null) {
                            visitor.visitStartProperties(elem1);
                        }
                        props = parseProperties(elem1);
                        if (visitor != null) {
                            visitor.visitEndProperties(elem1, props);
                        }
                        break;
                    }
                    case "dependencyManagement": {
                        if (visitor != null) {
                            visitor.visitStartDependenciesManagement(elem1);
                        }
                        NodeList dependenciesChildList = elem1.getChildNodes();
                        for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                            Element dependencies = toElement(dependenciesChildList.item(j), "dependencies");
                            if (dependencies != null) {
                                NodeList dependenciesChildList2 = dependencies.getChildNodes();
                                for (int k = 0; k < dependenciesChildList2.getLength(); k++) {
                                    Element dependency2 = toElement(dependenciesChildList2.item(k), "dependency");
                                    if (dependency2 != null) {
                                        if (visitor != null) {
                                            visitor.visitStartDependencyManagement(dependency2);
                                        }
                                        PomDependency dep = parseDependency(dependency2, new OsAndArch(props, session), session);
                                        if (visitor != null) {
                                            visitor.visitEndDependencyManagement(dependency2, dep);
                                        }
                                        depsMan.add(dep);
                                    }
                                }
                            }
                        }
                        if (visitor != null) {
                            visitor.visitEndDependenciesManagement(elem1, deps.toArray(new PomDependency[0]));
                        }
                        break;
                    }
                    case "dependencies": {
                        if (visitor != null) {
                            visitor.visitStartDependencies(elem1);
                        }
                        NodeList dependenciesChildList = elem1.getChildNodes();
                        for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                            Element dependency = toElement(dependenciesChildList.item(j), "dependency");
                            if (dependency != null) {
                                if (visitor != null) {
                                    visitor.visitStartDependency(dependency);
                                }
                                PomDependency dep = parseDependency(dependency, new OsAndArch(props, session), session);
                                if (visitor != null) {
                                    visitor.visitEndDependency(dependency, dep);
                                }
                                deps.add(dep);
                            }
                        }
                        if (visitor != null) {
                            visitor.visitEndDependencies(elem1, deps.toArray(new PomDependency[0]));
                        }
                        break;
                    }
                    case "repositories": {
                        if (visitor != null) {
                            visitor.visitStartRepositories(elem1);
                        }
                        NodeList dependenciesChildList = elem1.getChildNodes();
                        for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                            Element repository = toElement(dependenciesChildList.item(j), "repository");
                            if (repository != null) {
                                if (visitor != null) {
                                    visitor.visitStartRepository(repository);
                                }
                                PomRepository repo = parseRepository(repository);
                                if (visitor != null) {
                                    visitor.visitEndRepository(repository, repo);
                                }
                                repos.add(repo);
                            }
                        }
                        if (visitor != null) {
                            visitor.visitEndRepositories(elem1, repos.toArray(new PomRepository[0]));
                        }
                        break;
                    }
                    case "pluginRepositories": {
                        if (visitor != null) {
                            visitor.visitStartPluginRepositories(elem1);
                        }
                        NodeList dependenciesChildList = elem1.getChildNodes();
                        for (int j = 0; j < dependenciesChildList.getLength(); j++) {
                            Element repository = toElement(dependenciesChildList.item(j), "pluginRepository");
                            if (repository != null) {
                                if (visitor != null) {
                                    visitor.visitStartPluginRepository(repository);
                                }
                                PomRepository repo = parseRepository(repository);
                                if (visitor != null) {
                                    visitor.visitEndPluginRepository(repository, repo);
                                }
                                pluginRepos.add(repo);
                            }
                        }
                        if (visitor != null) {
                            visitor.visitEndPluginRepositories(elem1, pluginRepos.toArray(new PomRepository[0]));
                        }
                        break;
                    }
                    case "profiles": {
                        if (visitor != null) {
                            visitor.visitStartProfiles(elem1);
                        }
                        NodeList childList = elem1.getChildNodes();
                        for (int j = 0; j < childList.getLength(); j++) {
                            Element profile = toElement(childList.item(j), "profile");
                            if (profile != null) {
                                if (visitor != null) {
                                    visitor.visitStartProfile(profile);
                                }
                                PomProfile p = parseProfile(profile, session);
                                if (visitor != null) {
                                    visitor.visitEndProfile(profile, p);
                                }
                                profiles.add(p);
                            }
                        }
                        if (visitor != null) {
                            visitor.visitEndProfiles(elem1, profiles.toArray(new PomProfile[0]));
                        }
                        break;
                    }
                }
            }
        }
        Pom pom = new Pom(
                groupId, artifactId, version, packaging, parentId,
                name,
                description,
                url, inceptionYear,
                props,
                deps.toArray(new PomDependency[0]),
                depsMan.toArray(new PomDependency[0]),
                repos.toArray(new PomRepository[0]),
                pluginRepos.toArray(new PomRepository[0]),
                modules.toArray(new String[0]),
                profiles.toArray(new PomProfile[0]),
                doc
        );
        if (visitor != null) {
            visitor.visitEndDocument(doc, pom);
        }

        return pom;
    }

    private static class OsAndArch {
        Map<String, String> osMap = new HashMap<>();
        Map<String, String> archMap = new HashMap<>();

        public OsAndArch(Map<String, String> props, NutsSession session) {
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
                            NutsId id = NutsId.of(a).orNull();
                            if (id != null) {
                                if (!NutsBlankable.isBlank(os)) {
                                    osMap.put(id.getShortName(), os);
                                }
                                if (!NutsBlankable.isBlank(arch)) {
                                    archMap.put(id.getShortName(), arch);
                                }
                            }
                        }
                    }
                }
            }
        }

        public String getOs(NutsId id) {
            return osMap.get(id.getShortName());
        }

        public String getArch(NutsId id) {
            return archMap.get(id.getShortName());
        }
    }
}
