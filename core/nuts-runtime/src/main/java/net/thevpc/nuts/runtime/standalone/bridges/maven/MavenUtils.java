/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.bridges.maven;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.bundles.common.MapToFunction;
import net.thevpc.nuts.runtime.bundles.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.bundles.mvn.*;
import net.thevpc.nuts.runtime.bundles.parsers.StringTokenizerUtils;
import net.thevpc.nuts.runtime.core.model.DefaultNutsVersion;
import net.thevpc.nuts.runtime.core.repos.NutsRepositorySelector;
import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 2/20/17.
 */
public class MavenUtils {

    private final NutsLogger LOG;
    private NutsSession session;

    private MavenUtils(NutsSession session) {
        this.session = session;
        LOG = session.log().of(MavenUtils.class);
    }

    public static MavenUtils of(NutsSession session) {
        MavenUtils wp = (MavenUtils) session.env().getProperty(MavenUtils.class.getName()).getObject();
        if (wp == null) {
            wp = new MavenUtils(session);
            session.env().setProperty(MavenUtils.class.getName(), wp);
        }
        return wp;
    }

    public static PomIdResolver createPomIdResolver(NutsSession session) {
        PomIdResolver wp = (PomIdResolver) session.env().getProperty(PomIdResolver.class.getName()).getObject();
        if (wp == null) {
            wp = new PomIdResolver(new NutsPomUrlReader(session), new NutsPomLogger(session));
            session.env().setProperty(PomIdResolver.class.getName(), wp);
        }
        return wp;
    }

    public NutsId[] toNutsId(PomId[] ids) {
        NutsId[] a = new NutsId[ids.length];
        for (int i = 0; i < ids.length; i++) {
            a[i] = toNutsId(ids[i]);
        }
        return a;
    }

    public NutsDependency[] toNutsDependencies(PomDependency[] deps, NutsSession session, Pom pom, PomProfileActivation ac) {
        NutsDependency[] a = new NutsDependency[deps.length];
        for (int i = 0; i < deps.length; i++) {
            a[i] = toNutsDependency(deps[i], session, pom, ac);
        }
        return a;
    }

    public NutsId toNutsId(PomId d) {
        return session.id().builder().setGroupId(d.getGroupId()).setArtifactId(d.getArtifactId()).setVersion(toNutsVersion(d.getVersion())).build();
    }

    public NutsEnvCondition toCondition(NutsSession session, String os0, String arch0, PomProfileActivation a) {
        if (a == null) {
            return null;
        }
        NutsOsFamily os = NutsOsFamily.parseLenient(os0, null, null);
        NutsArchFamily arch = NutsArchFamily.parseLenient(arch0, null, null);
        String osVersion = null;
        String platform = null;
        if (a != null) {
            if (!NutsBlankable.isBlank(a.getOsVersion())) {
                osVersion = a.getOsVersion();
            }
            if (!NutsBlankable.isBlank(a.getOsArch())) {
                arch = NutsArchFamily.parseLenient(a.getOsArch(), null, null);
            }
            if (!NutsBlankable.isBlank(a.getOsName())) {
                NutsOsFamily os2 = NutsOsFamily.parseLenient(a.getOsName(), null, null);
                if (os2 != null) {
                    os = os2;
                }
            } else if (!NutsBlankable.isBlank(a.getOsFamily())) {
                NutsOsFamily os2 = NutsOsFamily.parseLenient(a.getOsFamily(), null, null);
                if (os2 != null) {
                    os = os2;
                }
            }
            if (!NutsBlankable.isBlank(a.getJdk())) {
                String jdk = a.getJdk();
                platform = "java#" + jdk;
            }
        }
        String oss = null;
        if (os != null) {
            oss = (osVersion == null ? os.id() : (os.id() + "#" + osVersion));
        }
        String ars = null;
        if (arch != null) {
            ars = arch.id();
        }
        return session.descriptor().envConditionBuilder()
                .setOs(oss == null ? new String[0] : new String[]{oss})
                .setArch(ars == null ? new String[0] : new String[]{ars})
                .setPlatform(platform == null ? new String[0] : new String[]{platform})
                .build();
    }

    public NutsDependency toNutsDependency(PomDependency d, NutsSession session, Pom pom, PomProfileActivation a) {
        String s = d.getScope();
        if (s == null) {
            s = "";
        }
        s = s.trim();
        NutsDependencyScope dependencyScope = NutsDependencyScope.API;
        switch (s) {
            case "":
            case "compile": {
                dependencyScope = NutsDependencyScope.API;
                break;
            }
            case "test": {
                dependencyScope = NutsDependencyScope.TEST_API;
                break;
            }
            case "system": {
                dependencyScope = NutsDependencyScope.SYSTEM;
                break;
            }
            case "runtime": {
                dependencyScope = NutsDependencyScope.RUNTIME;
                break;
            }
            case "provided": {
                dependencyScope = NutsDependencyScope.PROVIDED;
                break;
            }
            case "import": {
                dependencyScope = NutsDependencyScope.IMPORT;
                break;
            }
            default: {
                dependencyScope = NutsDependencyScope.parseLenient(s, NutsDependencyScope.API, NutsDependencyScope.API);
                if (dependencyScope == null) {
                    LOG.with().session(session).level(Level.FINER).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("unable to parse maven scope {0} for {1}", s, d));
                    dependencyScope = NutsDependencyScope.API;
                }
            }
        }
        return session.dependency().builder()
                .setGroupId(d.getGroupId())
                .setArtifactId(d.getArtifactId())
                .setClassifier(d.getClassifier())
                .setVersion(toNutsVersion((d.getVersion())))
                .setOptional(d.getOptional())
                .setScope(dependencyScope.id())
                .setCondition(toCondition(session, d.getOs(), d.getArch(), a))
                .setType(d.getType())
                .setExclusions(toNutsId(d.getExclusions()))
                .build();
    }

    private boolean testNode(Node n, Predicate<Node> tst) {
        if (tst.test(n)) {
            return true;
        }
        if (n instanceof Element) {
            Element e = (Element) n;
            final NodeList nl = e.getChildNodes();
            final int len = nl.getLength();
            for (int i = 0; i < len; i++) {
                if (testNode(nl.item(i), tst)) {
                    return true;
                }
            }
        }
        return false;
    }

    public NutsDescriptor parsePomXml0(InputStream stream, NutsFetchMode fetchMode, String urlDesc, NutsRepository repository) {
        long startTime = System.currentTimeMillis();
        try {
            if (stream == null) {
                return null;
            }
            byte[] bytes = CoreIOUtils.loadByteArray(stream);
            InputStream bytesStream = CoreIOUtils.createBytesStream(bytes, urlDesc == null ? null : NutsMessage.formatted(urlDesc), "text/xml", urlDesc, session);
            Pom pom = new PomXmlParser(new NutsPomLogger(session)).parse(bytesStream,session);
            boolean executable = false;// !"maven-archetype".equals(packaging.toString()); // default is true :)
            boolean application = false;// !"maven-archetype".equals(packaging.toString()); // default is true :)
            if ("true".equals(pom.getProperties().get("nuts.executable"))) {
                executable = true;
            } else {
                final Element ee = pom.getXml().getDocumentElement();
                if (testNode(ee, x -> {
                    if (x instanceof Element) {
                        Element e = (Element) x;
                        if (e.getNodeName().equals("mainClass")) {
                            return true;
                        }
                        if (e.getNodeName().equals("goal")) {
                            if (NutsUtilStrings.trim(e.getTextContent()).equals("exec-war-only")) {
                                return true;
                            }
                        }
                    }
                    return false;
                })) {
                    executable = true;
                }
            }
            if ("true".equals(pom.getProperties().get("nuts.application"))) {
                application = true;
            }
            if (application) {
                executable = true;
            }
            if (pom.getPackaging().isEmpty()) {
                pom.setPackaging("jar");
            }

            long time = System.currentTimeMillis() - startTime;
            String fetchString = "[" + CoreStringUtils.alignLeft(fetchMode.id(), 7) + "] ";
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS).time(time)
                    .log(NutsMessage.jstyle("{0}{1} parse pom    {2}", fetchString,
                            CoreStringUtils.alignLeft(repository == null ? "<no-repo>" : repository.getName(), 20),
                            session.text().ofStyled(urlDesc,NutsTextStyle.path())
                    ));

            String icons = pom.getProperties().get("nuts.icons");
            if (icons == null) {
                icons = "";
            }
            String categories = pom.getProperties().get("nuts.categories");
            if (categories == null) {
                categories = "";
            }
            PomProfile[] profiles = pom.getProfiles();//Arrays.stream(pom.getProfiles()).filter(x -> acceptRuntimeActivation(x.getActivation())).toArray(PomProfile[]::new);
            List<NutsDependency> deps = new ArrayList<>(
                    Arrays.asList(toNutsDependencies(pom.getDependencies(), session, pom, null)));
            for (PomProfile profile : profiles) {
                deps.addAll(Arrays.asList(toNutsDependencies(profile.getDependencies(), session, pom, profile.getActivation())));
            }
            List<NutsDependency> depsM = new ArrayList<>(
                    Arrays.asList(toNutsDependencies(pom.getDependenciesManagement(), session, pom, null)));
            for (PomProfile profile : profiles) {
                depsM.addAll(Arrays.asList(toNutsDependencies(profile.getDependenciesManagement(), session, pom, profile.getActivation())));
            }
            List<NutsDescriptorProperty> props = new ArrayList<>();
            for (Map.Entry<String, String> e : pom.getProperties().entrySet()) {
                props.add(session.descriptor().propertyBuilder().setName(e.getKey())
                        .setValue(e.getValue()).build());
            }
            for (PomProfile profile : profiles) {
                for (Map.Entry<String, String> e : profile.getProperties().entrySet()) {
                    props.add(session.descriptor().propertyBuilder()
                            .setName(e.getKey())
                            .setValue(e.getValue())
                            .setCondition(toCondition(session, null, null, profile.getActivation()))
                            .build());
                }
            }
            String mavenCompilerTarget = pom.getProperties().get("maven.compiler.target");
            if(!NutsBlankable.isBlank(mavenCompilerTarget)){
                mavenCompilerTarget="#"+mavenCompilerTarget.trim();
            }else{
                mavenCompilerTarget="";
            }
            return session.descriptor().descriptorBuilder()
                    .setId(toNutsId(pom.getPomId()))
                    .setParents(pom.getParent() == null ? new NutsId[0] : new NutsId[]{toNutsId(pom.getParent())})
                    .setPackaging(pom.getPackaging())
                    .setExecutable(executable)
                    .setApplication(application)
                    .setName(pom.getName())
                    .setDescription(pom.getDescription())
                    .setCondition(NutsEnvConditionBuilder.of(session).setPlatform("java"
                            +mavenCompilerTarget))
                    .setDependencies(deps.toArray(new NutsDependency[0]))
                    .setStandardDependencies(depsM.toArray(new NutsDependency[0]))
                    .setCategories(
                            Arrays.stream(categories.split("[\n\r;,]"))
                                    .map(String::trim)
                                    .filter(x -> !x.isEmpty())
                                    .collect(Collectors.toList())
                    )
                    .setIcons(
                            Arrays.stream(icons.split("[\n\r]"))
                                    .map(String::trim)
                                    .filter(x -> !x.isEmpty())
                                    .collect(Collectors.toList())
                    )
                    .setGenericName(pom.getProperties().get("nuts.genericName"))
                    .setProperties(props.toArray(new NutsDescriptorProperty[0]))
                    .build();
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).time(time)
                    .log(NutsMessage.jstyle("caching pom file {0}", urlDesc));
            throw new NutsParseException(session, NutsMessage.cstyle("error parsing %s", urlDesc), e);
        }
    }

//    private boolean acceptRuntimeActivation(PomProfileActivation activation) {
//        if (activation == null) {
//            return false;
//        }
//        return true;
//    }

    public String toNutsVersion(String version) {
        /// maven : [cc] [co) (oc] (oo)
        /// nuts  : [cc] [co[ ]oc] ]oo[
        return version == null ? null : version.replace("(", "]").replace(")", "[");
    }

    public NutsDescriptor parsePomXml(Path path, NutsFetchMode fetchMode, NutsRepository repository) throws IOException {
        try {
            session.getTerminal().printProgress("%-8s %s", "parse", session.io().path(path.toString()).toCompressedForm());
            try (InputStream is = Files.newInputStream(path)) {
                NutsDescriptor nutsDescriptor = parsePomXml(is, fetchMode, path.toString(), repository);
                if (nutsDescriptor.getId().getArtifactId() == null) {
                    //why name is null ? should checkout!
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.FAIL)
                                .log(NutsMessage.jstyle("unable to fetch Valid Nuts from {0} : resolved id was {1}",path, nutsDescriptor.getId()));
                    }
                    return null;
                }
                return nutsDescriptor;
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public NutsDescriptor parsePomXml(InputStream stream, NutsFetchMode fetchMode, String urlDesc, NutsRepository repository) {
        NutsDescriptor nutsDescriptor = null;
//        if (session == null) {
//            session = ws.createSession();
//        }
        try {
            try {
//            bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = parsePomXml0(stream, fetchMode, urlDesc, repository);
                HashMap<String, String> properties = new HashMap<>();
                NutsId parentId = null;
                for (NutsId nutsId : nutsDescriptor.getParents()) {
                    parentId = nutsId;
                }
                NutsDescriptor parentDescriptor = null;
                if (parentId != null) {
                    if (!CoreNutsUtils.isEffectiveId(parentId)) {
                        try {
                            parentDescriptor = session.fetch().setId(parentId).setEffective(true)
                                    .setSession(
                                            session.copy().setTransitive(true)
                                                    .setFetchStrategy(
                                                            fetchMode == NutsFetchMode.REMOTE ? NutsFetchStrategy.ONLINE
                                                                    : NutsFetchStrategy.OFFLINE
                                                    )
                                    )
                                    .getResultDescriptor();
                        } catch (NutsException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            throw new NutsNotFoundException(session, nutsDescriptor.getId(), NutsMessage.cstyle("unable to resolve %s parent %s", nutsDescriptor.getId(), parentId, ex));
                        }
                        parentId = parentDescriptor.getId();
                    }
                }
                if (parentId != null) {
                    properties.put("parent.groupId", parentId.getGroupId());
                    properties.put("parent.artifactId", parentId.getArtifactId());
                    properties.put("parent.version", parentId.getVersion().getValue());

                    properties.put("project.parent.groupId", parentId.getGroupId());
                    properties.put("project.parent.artifactId", parentId.getArtifactId());
                    properties.put("project.parent.version", parentId.getVersion().getValue());
                    nutsDescriptor = nutsDescriptor/*.setProperties(properties, true)*/.builder().applyProperties(properties).build();
                }
                NutsId thisId = nutsDescriptor.getId();
                if (!CoreNutsUtils.isEffectiveId(thisId)) {
                    if (parentId != null) {
                        if (NutsBlankable.isBlank(thisId.getGroupId())) {
                            thisId = thisId.builder().setGroupId(parentId.getGroupId()).build();
                        }
                        if (NutsBlankable.isBlank(thisId.getVersion().getValue())) {
                            thisId = thisId.builder().setVersion(parentId.getVersion().getValue()).build();
                        }
                    }
                    HashMap<NutsId, NutsDescriptor> cache = new HashMap<>();
                    Set<String> done = new HashSet<>();
                    Stack<NutsId> todo = new Stack<>();
                    todo.push(nutsDescriptor.getId());
                    cache.put(nutsDescriptor.getId(), nutsDescriptor);
                    while (todo.isEmpty()) {
                        NutsId pid = todo.pop();
                        NutsDescriptor d = cache.get(pid);
                        if (d == null) {
                            try {
                                d = session.fetch().setId(pid).setEffective(true).setSession(session).getResultDescriptor();
                            } catch (NutsException ex) {
                                throw ex;
                            } catch (Exception ex) {
                                throw new NutsNotFoundException(session, nutsDescriptor.getId(), NutsMessage.cstyle("unable to resolve %s parent %s", nutsDescriptor.getId(), pid, ex));
                            }
                        }
                        done.add(pid.getShortName());
                        if (CoreNutsUtils.containsVars(thisId)) {
                            thisId.builder().apply(new MapToFunction<>(
                                    CoreNutsUtils.getPropertiesMap(d.getProperties())
                            )).build();
                        } else {
                            break;
                        }
                        for (NutsId nutsId : d.getParents()) {
                            if (!done.contains(nutsId.getShortName())) {
                                todo.push(nutsId);
                            }
                        }
                    }
                    if (CoreNutsUtils.containsVars(thisId)) {
                        throw new NutsNotFoundException(session, nutsDescriptor.getId(), NutsMessage.cstyle("unable to resolve %s parent %s", nutsDescriptor.getId(), parentId));
                    }
                    nutsDescriptor = nutsDescriptor.builder().setId(thisId).build();
                }
                NutsDescriptorProperty nutsPackaging = nutsDescriptor.getProperty("nuts-packaging");
                if (nutsPackaging!=null && !NutsBlankable.isBlank(nutsPackaging.getValue())) {
                    nutsDescriptor = nutsDescriptor.builder().setPackaging(nutsDescriptor.getProperty("nuts-packaging").getValue())
                            .build();
                }
                properties.put("pom.groupId", thisId.getGroupId());
                properties.put("pom.version", thisId.getVersion().getValue());
                properties.put("pom.artifactId", thisId.getArtifactId());
                properties.put("project.groupId", thisId.getGroupId());
                properties.put("project.artifactId", thisId.getArtifactId());
                properties.put("project.version", thisId.getVersion().getValue());
                properties.put("version", thisId.getVersion().getValue());
                nutsDescriptor = nutsDescriptor/*.setProperties(properties, true)*/.builder().applyProperties(properties).build();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        } catch (Exception ex) {
            throw new NutsParseException(session, NutsMessage.cstyle("error Parsing %s", urlDesc), ex);
        }
        return nutsDescriptor;
    }

    public Iterator<NutsId> createArchetypeCatalogIterator(InputStream stream, NutsIdFilter filter, boolean autoClose, NutsSession session) {
        Iterator<PomId> it = ArchetypeCatalogParser.createArchetypeCatalogIterator(stream, filter == null ? null : new PomIdFilter() {
            @Override
            public boolean accept(PomId id) {
                return filter.acceptId(toNutsId(id), session);
            }
        }, autoClose);
        return IteratorBuilder.of(it).convert(pomId -> toNutsId(pomId), "PomId->NutsId").build();
    }

    public MavenMetadata parseMavenMetaData(InputStream metadataStream, NutsSession session) {
        MavenMetadata s = new MavenMetadataParser(session).parseMavenMetaData(metadataStream);
        if (s == null) {
            return s;
        }
        for (Iterator<String> iterator = s.getVersions().iterator(); iterator.hasNext(); ) {
            String version = iterator.next();
            if (s.getLatest().length() > 0 && DefaultNutsVersion.compareVersions(version, s.getLatest()) > 0) {
                iterator.remove();
            }
        }
        return s;
    }

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomPath(NutsId rid, NutsRepositorySelector.Selection[] bootRepositories, NutsSession session) {
        String urlPath = CoreNutsUtils.idToPath(rid) + "/" + rid.getArtifactId() + "-" + rid.getVersion() + ".pom";
        return loadDependenciesAndRepositoriesFromPomPath(urlPath, bootRepositories, session);
    }

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomPath(String urlPath, NutsRepositorySelector.Selection[] bootRepositories, NutsSession session) {
        NutsWorkspaceUtils.checkSession(this.session.getWorkspace(), session);
        DepsAndRepos depsAndRepos = null;
//        if (!NO_M2) {
        File mavenNutsCorePom = new File(System.getProperty("user.home"), (".m2/repository/" + urlPath).replace("/", File.separator));
        if (mavenNutsCorePom.isFile()) {
            depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(mavenNutsCorePom.getPath(), session);
        }
//        }
        if (depsAndRepos == null || depsAndRepos.deps.isEmpty()) {
            for (NutsRepositorySelector.Selection baseUrl : bootRepositories) {
                NutsAddRepositoryOptions opt = NutsRepositorySelector.createRepositoryOptions(baseUrl, false, session);
                String location = opt.getConfig() == null ? opt.getLocation() : opt.getConfig().getLocation();
                depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(location + "/" + urlPath, session);
                if (!depsAndRepos.deps.isEmpty()) {
                    break;
                }
            }
        }
        return depsAndRepos;
    }

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomUrl(String url, NutsSession session) {
        session.getTerminal().printProgress("%-8s %s", "load", session.io().path(url).toCompressedForm());
        DepsAndRepos depsAndRepos = new DepsAndRepos();
//        String repositories = null;
//        String dependencies = null;
        InputStream xml = null;
        try {
            if (CoreIOUtils.isPathHttp(url)) {
                xml = NutsWorkspaceUtils.of(session).openURL(url);
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsAndRepos;
                }
            }
//            List<String> dependenciesList = new ArrayList<>();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
            Element c = doc.getDocumentElement();
            for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("dependencies")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("dependency")) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            String groupId = null;
                            String artifactId = null;
                            String version = null;
                            String scope = null;
                            for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                if (c3.getChildNodes().item(k) instanceof Element) {
                                    Element c4 = (Element) c3.getChildNodes().item(k);
                                    switch (c4.getNodeName()) {
                                        case "groupId": {
                                            groupId = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "artifactId": {
                                            artifactId = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "version": {
                                            version = c4.getTextContent().trim();
                                            break;
                                        }
                                        case "scope": {
                                            scope = c4.getTextContent().trim();
                                            break;
                                        }
                                    }
                                }
                            }
                            if (NutsBlankable.isBlank(groupId)) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected empty groupId"));
                            } else if (groupId.contains("$")) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected maven variable in groupId=%s", groupId));
                            }
                            if (NutsBlankable.isBlank(artifactId)) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected empty artifactId"));
                            } else if (artifactId.contains("$")) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected maven variable in artifactId=%s", artifactId));
                            }
                            if (NutsBlankable.isBlank(version)) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected empty artifactId"));
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected maven variable in artifactId=%s", version));
                            }
                            //this is maven dependency, using "compile"
                            if (NutsBlankable.isBlank(scope) || scope.equals("compile")) {
                                depsAndRepos.deps.add(groupId + ":" + artifactId + "#" + version);
                            } else if (version.contains("$")) {
                                throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected maven variable in artifactId=%s", version));
                            }
                        }
                    }
                } else if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("properties")) {
                    Element c2 = (Element) c.getChildNodes().item(i);
                    for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                        if (c2.getChildNodes().item(j) instanceof Element) {
                            Element c3 = (Element) c2.getChildNodes().item(j);
                            switch (c3.getNodeName()) {
                                case "nuts-runtime-repositories": {
                                    String t = c3.getTextContent().trim();
                                    if (t.length() > 0) {
                                        depsAndRepos.deps.addAll(StringTokenizerUtils.split(t, ";", true));
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            LOG.with().session(session).level(Level.SEVERE).error(ex)
                    .log(NutsMessage.jstyle("failed to loadDependenciesAndRepositoriesFromPomUrl {0} : {1}", url, ex));
            //ignore
        } finally {
            if (xml != null) {
                try {
                    xml.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }

        return depsAndRepos;
    }

    /**
     * find latest maven package
     *
     * @param zId     id
     * @param filter  filter
     * @param session session
     * @return latest runtime version
     */
    public NutsId resolveLatestMavenId(NutsId zId, Predicate<String> filter, NutsSession session) {
        String path = zId.getGroupId().replace('.', '/') + '/' + zId.getArtifactId();
        String bestVersion = null;
//        if (!NO_M2) {
        File mavenNutsCoreFolder = new File(System.getProperty("user.home"), ".m2/repository/" + path + "/".replace("/", File.separator));
        if (mavenNutsCoreFolder.isDirectory()) {
            File[] children = mavenNutsCoreFolder.listFiles();
            if (children != null) {
                for (File file : children) {
                    if (file.isDirectory()) {
                        String[] goodChildren = file.list(new FilenameFilter() {
                            @Override
                            public boolean accept(File dir, String name) {
                                return name.endsWith(".pom");
                            }
                        });
                        if (goodChildren != null && goodChildren.length > 0) {
                            String p = file.getName();
                            if (filter == null || filter.test(p)) {
                                if (bestVersion == null || DefaultNutsVersion.compareVersions(bestVersion, p) < 0) {
                                    bestVersion = p;
                                }
                            }
                        }
                    }
                }
            }
        }
//        }
        for (String repoUrl : new String[]{
                //                NutsConstants.BootstrapURLs.REMOTE_MAVEN_GIT,
                NutsConstants.BootstrapURLs.REMOTE_MAVEN_CENTRAL
        }) {
            if (!repoUrl.endsWith("/")) {
                repoUrl = repoUrl + "/";
            }
            boolean found = false;
            String mavenMetadataXml = repoUrl + path + "/maven-metadata.xml";
            try {
                URL runtimeMetadata = new URL(mavenMetadataXml);
                found = true;
                DocumentBuilderFactory factory
                        = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(NutsWorkspaceUtils.of(session).openURL(runtimeMetadata));
                Element c = doc.getDocumentElement();
                for (int i = 0; i < c.getChildNodes().getLength(); i++) {
                    if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("versioning")) {
                        Element c2 = (Element) c.getChildNodes().item(i);
                        for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                            if (c2.getChildNodes().item(j) instanceof Element && c2.getChildNodes().item(j).getNodeName().equals("versions")) {
                                Element c3 = (Element) c2.getChildNodes().item(j);
                                for (int k = 0; k < c3.getChildNodes().getLength(); k++) {
                                    if (c3.getChildNodes().item(k) instanceof Element && c3.getChildNodes().item(k).getNodeName().equals("version")) {
                                        Element c4 = (Element) c3.getChildNodes().item(k);
                                        String p = c4.getTextContent();
                                        if (filter == null || filter.test(p)) {
                                            if (bestVersion == null || DefaultNutsVersion.compareVersions(bestVersion, p) < 0) {
                                                bestVersion = p;
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                    //NutsConstants.Ids.NUTS_RUNTIME.replaceAll("[.:]", "/")
                }
            } catch (Exception ex) {
                LOG.with().session(session).level(Level.SEVERE).error(ex)
                        .log(NutsMessage.jstyle("failed to load and parse {0} : {1}", mavenMetadataXml, ex));
                // ignore any error
            }
            if (found) {
                break;
            }
        }
        if (bestVersion == null) {
            return null;
        }
        return zId.builder().setVersion(bestVersion).build();
    }

    public static class DepsAndRepos {

        public LinkedHashSet<String> deps = new LinkedHashSet<>();
        public LinkedHashSet<String> repos = new LinkedHashSet<>();
    }

    private static class NutsPomLogger implements PomLogger {

        private final NutsSession session;
        NutsLogger LOG;

        public NutsPomLogger(NutsSession session) {
            this.session = session;
            LOG = session.log().of(PomIdResolver.class);
        }

        @Override
        public void log(Level level, String msg, Object... params) {
            LOG.with().session(session)
                    .level(Level.FINE)
                    .verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle(msg, params));
        }

        @Override
        public void log(Level level, String msg, Throwable throwable) {
            LOG.with().session(session)
                    .level(Level.FINE)
                    .verb(NutsLogVerb.FAIL)
                    .log(NutsMessage.jstyle("{0}", msg));
        }
    }

    private static class NutsPomUrlReader implements PomUrlReader {

        private final NutsSession session;

        public NutsPomUrlReader(NutsSession session) {
            this.session = session;
        }

        @Override
        public InputStream openStream(URL url) {
            return NutsWorkspaceUtils.of(session).openURL(url);
        }
    }
}
