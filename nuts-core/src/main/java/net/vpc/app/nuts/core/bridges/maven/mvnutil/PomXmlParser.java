package net.vpc.app.nuts.core.bridges.maven.mvnutil;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
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
import net.vpc.app.nuts.core.format.xml.NutsXmlUtils;

public class PomXmlParser {

    private static Map<String, String> map = new HashMap<>();
    private static Pattern ENTITY_PATTERN = Pattern.compile("&[a-zA-Z]+;");

    static {
        map.put("&Oslash;", "&#216;");
        map.put("&oslash;", "&#248;");
        map.put("&AElig;", "&#198;");
        map.put("&aelig;", "&#230;");
        map.put("&Auml;", "&#196;");
        map.put("&auml;", "&#228;");
        map.put("&OElig;", "&#338;");
        map.put("&oelig;", "&#339;");
        map.put("&lt;", "&#60;");
        map.put("&gt;", "&#62;");
        map.put("&amp;", "&#38;");
        map.put("&quot;", "&#34;");
        map.put("&euro;", "&#8364;");
        map.put("&circ;", "&#710;");
        map.put("&tilde;", "&#732;");
        map.put("&ndash;", "&#45;");
        map.put("&copy;", "&#169;");
        map.put("&nbsp;", "&#32;");
        map.put("&apos;", "&#39;");
    }

//    public static void main(String[] args) {
//        try {
////            URL u = new URL("http://repo.maven.apache.org/maven2/qdox/qdox/1.6.1/qdox-1.6.1.pom");
//            URL u = new File("/home/vpc/.m2/repository/org/apache/commons/commons-parent/43/commons-parent-43.pom").toURI().toURL();
//            Pom parse = new PomXmlParser().parse(u);
//            System.out.println(parse);
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
    public static final ErrorHandler EH = null;
//            new ErrorHandler() {
//        @Override
//        public void warning(SAXParseException exception) throws SAXException {
//            System.out.println(exception);
//        }
//
//        @Override
//        public void error(SAXParseException exception) throws SAXException {
//            System.out.println(exception);
//
//        }
//
//        @Override
//        public void fatalError(SAXParseException exception) throws SAXException {
//            System.out.println(exception);
//
//        }
//    };

    public Pom parse(URL url) throws IOException, SAXException, ParserConfigurationException {
        return parse(url, null);
    }

    public Pom parse(URL url, PomDomVisitor visitor) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;
        try {
            return parse((is = url.openStream()), visitor);
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    public Pom parse(URI uri) throws IOException, SAXException, ParserConfigurationException {
        return parse(uri, null);
    }

    public Pom parse(URI uri, PomDomVisitor visitor) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;
        try {
            return parse(is = uri.toURL().openStream());
        } finally {
            if (is != null) {
                is.close();
            }
        }
//        Document doc = createDocumentBuilder().parse(uri.toString());
//        return parse(doc, visitor);
    }

    public Pom parse(File file) throws IOException, SAXException, ParserConfigurationException {
        return parse(file, null);
    }

    public Pom parse(File file, PomDomVisitor visitor) throws IOException, SAXException, ParserConfigurationException {
        InputStream is = null;
        try {
            return parse(new FileInputStream(file));
        } finally {
            if (is != null) {
                is.close();
            }
        }
        //Document doc = createDocumentBuilder().parse(file);
//        return parse(doc, visitor);
    }

    public Pom parse(InputStream stream) throws IOException, SAXException, ParserConfigurationException {
        return parse(stream, null);
    }

    public Pom parse(InputStream stream, PomDomVisitor visitor) throws IOException, SAXException, ParserConfigurationException {
        Document doc = NutsXmlUtils.createDocumentBuilder(true).parse(preValidateStream(stream));
        return parse(doc, visitor);
    }

    private byte[] loadAllBytes(InputStream in) throws IOException {
        ByteArrayOutputStream o = new ByteArrayOutputStream();
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
    }

    private InputStream preValidateStream(InputStream in) throws IOException {
        byte[] bytes0 = loadAllBytes(in);
        int skip = 0;
        while (skip < bytes0.length && Character.isWhitespace(bytes0[skip])) {
            skip++;
        }
        String x = new String(bytes0, skip, bytes0.length - skip);
        StringBuffer sb = new StringBuffer();
        Matcher m = ENTITY_PATTERN.matcher(x);
        while (m.find()) {
            String key = m.group();
            String v = map.get(key);
            if (v != null) {
                m.appendReplacement(sb, v);
            } else {
                System.err.println("[PomXmlParser] Unsupported  xml entity declaration : " + key);
                m.appendReplacement(sb, key);
            }
        }
        m.appendTail(sb);

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    public Pom parse(Document doc) {
        return parse(doc, null);
    }

    public Pom parse(Document doc, PomDomVisitor visitor) {
        List<PomDependency> deps = new ArrayList<>();
        List<PomDependency> depsMan = new ArrayList<>();
        List<PomRepository> repos = new ArrayList<>();
        List<PomRepository> pluginRepos = new ArrayList<>();
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
                                        PomDependency dep = parseDependency(dependency2);
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
                                PomDependency dep = parseDependency(dependency);
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
                modules.toArray(new String[0]), doc
        );
        if (visitor != null) {
            visitor.visitEndDocument(doc, pom);
        }

        return pom;
    }

    private static String elemToStr(Element ex) {
        return ex.getTextContent() == null ? "" : ex.getTextContent().trim();
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

    public static PomDependency parseDependency(Element dependency) {
        NodeList dependencyChildList = dependency.getChildNodes();
        String d_groupId = "";
        String d_artifactId = "";
        String d_version = "";
        String d_classifier = "";
        String d_scope = "";
        String d_optional = "";
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
        return new PomDependency(
                d_groupId, d_artifactId, d_classifier, d_version, d_scope, d_optional, d_exclusions.toArray(new PomId[0])
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

    public static void writeDocument(Document doc, File result) throws TransformerException {
        writeDocument(doc, new StreamResult(result));
    }

    public static void writeDocument(Document doc, Writer result) throws TransformerException {
        writeDocument(doc, new StreamResult(result));
    }

    public static void writeDocument(Document doc, OutputStream result) throws TransformerException {
        writeDocument(doc, new StreamResult(result));
    }

    public static void writeDocument(Document doc, StreamResult result) throws TransformerException {
        NutsXmlUtils.writeDocument(doc, result, false);
    }

    public static boolean appendOrReplaceDependency(PomDependency dependency, Element dependencyElement, Element dependenciesElement) {
        if (dependencyElement != null && dependenciesElement == null) {
            dependenciesElement = (Element) dependencyElement.getParentNode();
        }
        Document doc = dependenciesElement.getOwnerDocument();
        if (dependencyElement == null) {
            dependenciesElement.appendChild(createDependencyElement(doc, dependency));
            return true;
        } else {
            PomDependency old = parseDependency(dependencyElement);
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
}
