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
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.DefaultNutsArtifactCall;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NutsDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.xml.XmlUtils;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
import net.thevpc.nuts.runtime.standalone.util.MapToFunction;
import net.thevpc.nuts.runtime.standalone.repository.NutsRepositorySelectorHelper;
import net.thevpc.nuts.runtime.standalone.util.iter.IteratorBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.DefaultNutsVersion;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreStringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 2/20/17.
 */
public class MavenUtils {

    private final NutsLogger LOG;
    private final NutsSession session;

    private MavenUtils(NutsSession session) {
        this.session = session;
        LOG = NutsLogger.of(MavenUtils.class, session);
    }

    public static MavenUtils of(NutsSession session) {
        MavenUtils wp = (MavenUtils) session.env().getProperties().get(MavenUtils.class.getName());
        if (wp == null) {
            wp = new MavenUtils(session);
            session.env().setProperty(MavenUtils.class.getName(), wp);
        }
        return wp;
    }

    public static PomIdResolver createPomIdResolver(NutsSession session) {
        PomIdResolver wp = (PomIdResolver) session.env().getProperties().get(PomIdResolver.class.getName());
        if (wp == null) {
            wp = new PomIdResolver(session);
            session.env().setProperty(PomIdResolver.class.getName(), wp);
        }
        return wp;
    }

    public List<NutsId> toNutsId(List<PomId> ids) {
        return ids.stream().map(this::toNutsId).collect(Collectors.toList());
    }

    public NutsDependency[] toNutsDependencies(PomDependency[] deps, NutsSession session, Pom pom, PomProfileActivation ac, String profile) {
        NutsDependency[] a = new NutsDependency[deps.length];
        for (int i = 0; i < deps.length; i++) {
            a[i] = toNutsDependency(deps[i], session, pom, ac, profile);
        }
        return a;
    }

    public NutsId toNutsId(PomId d) {
        return new DefaultNutsIdBuilder().setGroupId(d.getGroupId()).setArtifactId(d.getArtifactId()).setVersion(toNutsVersion(d.getVersion())).build();
    }

    public NutsEnvCondition toCondition(NutsSession session, String os0, String arch0, PomProfileActivation a, String profile) {
//        if (a == null) {
//            return null;
//        }
        NutsOsFamily os = NutsOsFamily.parse(os0).orElse(null);
        NutsArchFamily arch = NutsArchFamily.parse(arch0).orElse(null);
        String osVersion = null;
        String platform = null;
        Map<String,String> props=new LinkedHashMap<>();
        if (a != null) {
            if (!NutsBlankable.isBlank(a.getOsVersion())) {
                osVersion = a.getOsVersion();
            }
            if (!NutsBlankable.isBlank(a.getOsArch())) {
                arch = NutsArchFamily.parse(a.getOsArch()).orElse(null);
            }
            if (!NutsBlankable.isBlank(a.getOsName())) {
                NutsOsFamily os2 = NutsOsFamily.parse(a.getOsName()).orElse(null);
                if (os2 != null) {
                    os = os2;
                }
            } else if (!NutsBlankable.isBlank(a.getOsFamily())) {
                NutsOsFamily os2 = NutsOsFamily.parse(a.getOsFamily()).orElse(null);
                if (os2 != null) {
                    os = os2;
                }
            }
            if (!NutsBlankable.isBlank(a.getJdk())) {
                platform = "java#" + toNutsVersion(a.getJdk());
            }
            if(a.getPropertyName()!=null){
                props.put(a.getPropertyName(),a.getPropertyValue());
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
        NutsEnvConditionBuilder bb = new DefaultNutsEnvConditionBuilder()
                .setOs(oss == null ? null : Arrays.asList(oss))
                .setArch(ars == null ? null : Arrays.asList(ars))
                .setPlatform(platform == null ? null : Arrays.asList(platform))
                .setProfile(profile == null ? null : Arrays.asList(profile));
        bb.setProperties(props);
        return bb.build();
    }

    public NutsDependency toNutsDependency(PomDependency d, NutsSession session, Pom pom, PomProfileActivation a, String profile) {
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
                dependencyScope = NutsDependencyScope.parse(s).orElse( NutsDependencyScope.API);
                if (dependencyScope == null) {
                    LOG.with().session(session).level(Level.FINER).verb(NutsLogVerb.FAIL)
                            .log(NutsMessage.jstyle("unable to parse maven scope {0} for {1}", s, d));
                    dependencyScope = NutsDependencyScope.API;
                }
            }
        }
        return new DefaultNutsDependencyBuilder()
                .setGroupId(d.getGroupId())
                .setArtifactId(d.getArtifactId())
                .setClassifier(d.getClassifier())
                .setVersion(toNutsVersion((d.getVersion())))
                .setOptional(d.getOptional())
                .setScope(dependencyScope.id())
                .setCondition(toCondition(session, d.getOs(), d.getArch(), a, profile))
                .setType(d.getType())
                .setExclusions(toNutsId(Arrays.asList(d.getExclusions())))
                .build();
    }

    public NutsDescriptor parsePomXml(InputStream stream, NutsFetchMode fetchMode, String urlDesc, NutsRepository repository) {
        long startTime = System.currentTimeMillis();
        try {
            if (stream == null) {
                return null;
            }
            byte[] bytes = CoreIOUtils.loadByteArray(stream, session);
            InputStream bytesStream = CoreIOUtils.createBytesStream(bytes,
                    urlDesc == null ? NutsMessage.formatted("pom.xml") : NutsMessage.formatted(urlDesc), "text/xml",
                    urlDesc == null ? "pom.xml" : urlDesc, session);
            Pom pom = new PomXmlParser(session).parse(bytesStream, session);
            LinkedHashSet<NutsDescriptorFlag> flags = new LinkedHashSet<>();
            if (NutsUtilStrings.parseBoolean(pom.getProperties().get("nuts.executable"), false, false)) {
                flags.add(NutsDescriptorFlag.EXEC);
            } else {
                final Element ee = pom.getXml().getDocumentElement();
                if (XmlUtils.testNode(ee, x -> {
                    if (x instanceof Element) {
                        Element e = (Element) x;
                        if (XmlUtils.isNode(e, "build", "plugins", "plugin", "configuration", "archive", "manifest", "mainClass")) {
                            return true;
                        }
                        if (NutsUtilStrings.trim(e.getTextContent()).equals("exec-war-only") &&
                                XmlUtils.isNode(e, "build", "plugins", "plugin", "executions", "execution", "goals", "goal")) {
                            return true;
                        }
                    }
                    return false;
                })) {
                    flags.add(NutsDescriptorFlag.EXEC);
                }
            }
            if (NutsUtilStrings.parseBoolean(pom.getProperties().get("nuts.application"), false, false)) {
                flags.add(NutsDescriptorFlag.APP);
                flags.add(NutsDescriptorFlag.EXEC);
            }
            if (NutsUtilStrings.parseBoolean(pom.getProperties().get("nuts.gui"), false, false)) {
                flags.add(NutsDescriptorFlag.GUI);
                flags.add(NutsDescriptorFlag.EXEC);
            }
            if (NutsUtilStrings.parseBoolean(pom.getProperties().get("nuts.term"), false, false)) {
                flags.add(NutsDescriptorFlag.TERM);
                flags.add(NutsDescriptorFlag.EXEC);
            }
            if (pom.getPackaging().isEmpty()) {
                pom.setPackaging("jar");
            }

            long time = System.currentTimeMillis() - startTime;
            if (fetchMode == null) {
                fetchMode = NutsFetchMode.REMOTE;
            }
            String fetchString = "[" + CoreStringUtils.alignLeft(fetchMode.id(), 7) + "] ";
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.SUCCESS).time(time)
                    .log(NutsMessage.jstyle("{0}{1} parse pom    {2}", fetchString,
                            CoreStringUtils.alignLeft(repository == null ? "<no-repo>" : repository.getName(), 20),
                            NutsTexts.of(session).ofStyled(urlDesc, NutsTextStyle.path())
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
                    Arrays.asList(toNutsDependencies(pom.getDependencies(), session, pom, null, null)));
            for (PomProfile profile : profiles) {
                deps.addAll(Arrays.asList(toNutsDependencies(profile.getDependencies(), session, pom, profile.getActivation(), profile.getId())));
            }
            List<NutsDependency> depsM = new ArrayList<>(
                    Arrays.asList(toNutsDependencies(pom.getDependenciesManagement(), session, pom, null, null)));
            for (PomProfile profile : profiles) {
                depsM.addAll(Arrays.asList(toNutsDependencies(profile.getDependenciesManagement(), session, pom, profile.getActivation(), profile.getId())));
            }
            List<NutsDescriptorProperty> props = new ArrayList<>();
            for (Map.Entry<String, String> e : pom.getProperties().entrySet()) {
                props.add(new DefaultNutsDescriptorPropertyBuilder().setName(e.getKey())
                        .setValue(e.getValue()).build());
            }
            for (PomProfile profile : profiles) {
                for (Map.Entry<String, String> e : profile.getProperties().entrySet()) {
                    props.add(new DefaultNutsDescriptorPropertyBuilder()
                            .setName(e.getKey())
                            .setValue(e.getValue())
                            .setCondition(toCondition(session, null, null, profile.getActivation(), profile.getId()))
                            .build());
                }
            }
            NutsVersion mavenCompilerTarget=null;
            for (String v : new String[]{"maven.compiler.target", "project.target.level"}) {
                String vv = pom.getProperties().get(v);
                if (!NutsBlankable.isBlank(vv)) {
                    if(mavenCompilerTarget==null || mavenCompilerTarget.compareTo(vv)<0){
                        mavenCompilerTarget=NutsVersion.of(vv).get();
                    }
                }
            }

            Set<String> toRemoveProps=new LinkedHashSet<>();
            NutsArtifactCall installerCall=parseCall(pom.getProperties().get("nuts.installer"),session);
            NutsArtifactCall executorCall=parseCall(pom.getProperties().get("nuts.executor"),session);
            LinkedHashSet<NutsIdLocation> idLocations=new LinkedHashSet<>();
            NutsIdLocation idLocation=parseLocation(pom.getProperties(),"nuts.location",toRemoveProps,session);
            if(idLocation!=null){
                idLocations.add(idLocation);
            }
            String genericName = pom.getProperties().get("nuts.genericName");
            idLocation=parseLocation(pom.getProperties(),"nuts.location.0",toRemoveProps,session);
            if(idLocation!=null){
                idLocations.add(idLocation);
            }
            for (int i = 0; i < 32; i++) {
                idLocation=parseLocation(pom.getProperties(),"nuts.location."+i,toRemoveProps,session);
                if(idLocation!=null){
                    idLocations.add(idLocation);
                }else{
                    break;
                }
            }

            //delete special properties
            for (Iterator<NutsDescriptorProperty> iterator = props.iterator(); iterator.hasNext(); ) {
                NutsDescriptorProperty prop = iterator.next();
                if(prop.getCondition().isBlank()){
                    String n = prop.getName();
                    switch (n){
                        case "nuts.installer":
                        case "nuts.executor":
                        case "nuts.categories":
                        case "nuts.icons":
                        case "nuts.term":
                        case "nuts.gui":
                        case "nuts.application":
                        case "nuts.executable":
                        case "nuts.genericName":
                        {
                            iterator.remove();
                            break;
                        }
                        default:{
                            if(toRemoveProps.contains(n)){
                                iterator.remove();
                            }
                        }
                    }
                }
            }
            return new DefaultNutsDescriptorBuilder()
                    .setId(toNutsId(pom.getPomId()))
                    .setParents(pom.getParent() == null ? null : Arrays.asList(toNutsId(pom.getParent())))
                    .setPackaging(pom.getPackaging())
                    .setFlags(flags)
                    .setName(pom.getName())
                    .setDescription(pom.getDescription())
                    .setLocations(new ArrayList<>(idLocations))
                    .setCondition(new DefaultNutsEnvConditionBuilder().setPlatform(
                            Arrays.asList(mavenCompilerTarget==null?"java":("java#"+mavenCompilerTarget))
                    ))
                    .setDependencies(deps)
                    .setStandardDependencies(depsM)
                    .setCategories(
                            StringTokenizerUtils.splitDefault(categories).stream()
                                    .map(String::trim)
                                    .filter(x -> !x.isEmpty())
                                    .collect(Collectors.toList())
                    )
                    .setInstaller(installerCall)
                    .setExecutor(executorCall)
                    .setIcons(
                            StringTokenizerUtils.splitDefault(icons).stream()
                                    .map(String::trim)
                                    .filter(x -> !x.isEmpty())
                                    .collect(Collectors.toList())
                    )
                    .setGenericName(genericName)
                    .setProperties(props)
                    .build();
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            LOG.with().session(session).level(Level.FINEST).verb(NutsLogVerb.FAIL).time(time)
                    .log(NutsMessage.jstyle("caching pom file {0}", urlDesc));
            throw new NutsParseException(session, NutsMessage.cstyle("error parsing %s", urlDesc), e);
        }
    }

    private NutsIdLocation parseLocation(Map<String, String> properties, String propName, Set<String> toRemoveProps, NutsSession session) {
        String url = properties.get(propName + ".url");
        String region = properties.get(propName + ".region");
        String classifier = properties.get(propName + ".classifier");
        if(!NutsBlankable.isBlank(url)){
            toRemoveProps.add(propName + ".url");
            toRemoveProps.add(propName + ".region");
            toRemoveProps.add(propName + ".classifier");
            return new NutsIdLocation(NutsUtilStrings.trimToNull(url), NutsUtilStrings.trimToNull(region), NutsUtilStrings.trimToNull(classifier));
        }
        return null;
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

    public NutsDescriptor parsePomXmlAndResolveParents(NutsPath path, NutsFetchMode fetchMode, NutsRepository repository) throws IOException {
        try {
            session.getTerminal().printProgress("%-8s %s", "parse", path.toCompressedForm());
            try (InputStream is = path.getInputStream()) {
                NutsDescriptor nutsDescriptor = parsePomXmlAndResolveParents(is, fetchMode, path.toString(), repository);
                if (nutsDescriptor.getId().getArtifactId() == null) {
                    //why name is null ? should checkout!
                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.with().session(session).level(Level.FINE).verb(NutsLogVerb.FAIL)
                                .log(NutsMessage.jstyle("unable to fetch Valid Nuts from {0} : resolved id was {1}", path, nutsDescriptor.getId()));
                    }
                    return null;
                }
                return nutsDescriptor;
            }
        } catch (IOException ex) {
            throw new NutsIOException(session, ex);
        }
    }

    public NutsDescriptor parsePomXmlAndResolveParents(InputStream stream, NutsFetchMode fetchMode, String urlDesc, NutsRepository repository) {
        NutsDescriptor nutsDescriptor = null;
//        if (session == null) {
//            session = ws.createSession();
//        }
        try {
            try {
//            bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = parsePomXml(stream, fetchMode, urlDesc, repository);
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
                    nutsDescriptor = NutsDescriptorUtils.applyProperties(nutsDescriptor.builder(),properties,session).build();
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
                            thisId=NutsDescriptorUtils.applyProperties(thisId.builder(),new MapToFunction<>(
                                    NutsDescriptorUtils.getPropertiesMap(d.getProperties(), session)
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
                if (nutsPackaging != null && !NutsBlankable.isBlank(nutsPackaging.getValue())) {
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
                nutsDescriptor = NutsDescriptorUtils.applyProperties(
                                nutsDescriptor/*.setProperties(properties, true)*/.builder(),properties,session
                        ).build();
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
        return IteratorBuilder.of(
                NutsIterator.of(it, stream.toString()),
                session).map(NutsFunction.of(this::toNutsId, "PomId->NutsId")).build();
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

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomPath(NutsId rid, NutsRepositoryLocation[] bootRepositories, NutsSession session) {
        String urlPath = CoreNutsUtils.idToPath(rid) + "/" + rid.getArtifactId() + "-" + rid.getVersion() + ".pom";
        return loadDependenciesAndRepositoriesFromPomPath(urlPath, bootRepositories, session);
    }

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomPath(String urlPath, NutsRepositoryLocation[] bootRepositories, NutsSession session) {
        NutsSessionUtils.checkSession(this.session.getWorkspace(), session);
        DepsAndRepos depsAndRepos = null;
//        if (!NO_M2) {
        File mavenNutsCorePom = new File(System.getProperty("user.home"), (".m2/repository/" + urlPath).replace("/", File.separator));
        if (mavenNutsCorePom.isFile()) {
            depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(mavenNutsCorePom.getPath(), session);
        }
//        }
        if (depsAndRepos == null || depsAndRepos.deps.isEmpty()) {
            for (NutsRepositoryLocation baseUrl : bootRepositories) {
                NutsAddRepositoryOptions opt = NutsRepositorySelectorHelper.createRepositoryOptions(baseUrl, false, session);
                String location =
                        opt.getConfig() == null
                                || NutsBlankable.isBlank(opt.getConfig().getLocation())
                                || NutsBlankable.isBlank(opt.getConfig().getLocation().getPath())
                                ? opt.getLocation() : opt.getConfig().getLocation().getPath();
                depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(location + "/" + urlPath, session);
                if (!depsAndRepos.deps.isEmpty()) {
                    break;
                }
            }
        }
        return depsAndRepos;
    }

    public DepsAndRepos loadDependenciesAndRepositoriesFromPomUrl(String url, NutsSession session) {
        NutsPath ppath = NutsPath.of(url, session);
        session.getTerminal().printProgress("%-8s %s", "load", ppath.toCompressedForm());
        DepsAndRepos depsAndRepos = new DepsAndRepos();
//        String repositories = null;
//        String dependencies = null;
        InputStream xml = null;
        try {

            if (ppath.isHttp()) {
                xml = NutsPath.of(url, session).getInputStream();
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsAndRepos;
                }
            }
            NutsDescriptor descr = parsePomXml(xml,
                    session.getFetchStrategy() == NutsFetchStrategy.OFFLINE ? NutsFetchMode.LOCAL :
                            NutsFetchMode.REMOTE
                    , null, null);
            NutsDescriptorProperty t = descr.getProperty("nuts-runtime-repositories");
            if (t != null) {
                if (CoreFilterUtils.acceptCondition(t.getCondition(), true, session)) {
                    depsAndRepos.deps.addAll(StringTokenizerUtils.split(t.getValue(), ";", true));
                }
            }
            for (NutsDependency dependency : descr.getDependencies()) {
                if (CoreFilterUtils.acceptDependency(dependency, session)) {
                    String groupId = dependency.getGroupId();
                    String version = dependency.getVersion().getValue();
                    String artifactId = dependency.getArtifactId();
                    String scope = dependency.getScope();
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
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected empty version"));
                    } else if (version.contains("$")) {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected maven version in version=%s", version));
                    }
                    if (!NutsBlankable.isBlank(scope) && groupId.contains("$")) {
                        throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unexpected maven variable in scope=%s", scope));
                    }
                    if (NutsDependencyScope.parse(dependency.getScope()).orElse( NutsDependencyScope.API)
                            == NutsDependencyScope.API) {
                        depsAndRepos.deps.add(groupId + ":" + artifactId + "#" + version);
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
                Document doc = builder.parse(NutsPath.of(runtimeMetadata, session).getInputStream());
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

    public NutsArtifactCall parseCall(String callString,NutsSession session){
        if(callString==null){
            return null;
        }
        NutsCommandLine cl = NutsCommandLine.of(callString,NutsShellFamily.BASH, session).setExpandSimpleOptions(false);
        NutsId callId=null;
        Map<String,String> callProps=new LinkedHashMap<>();
        List<String> callPropsAsArgs=new ArrayList<>();
        while(cl.hasNext() && cl.peek().isOption()){
            NutsArgument a = cl.next();
            callPropsAsArgs.add(a.toString());
            if(a.isKeyValue()){
                callProps.put(a.getStringKey(),a.getStringValue());
            }else{
                callProps.put(a.toString(),null);
            }
        }
        if(cl.hasNext()){
            String callIdString=cl.next().toString();
            callId=NutsId.of(callIdString).orElse(null);
        }
        List<String> callArgs = cl.toStringList();
        if(callId!=null){
            return new DefaultNutsArtifactCall(callId,callArgs,callProps);
        }
        //there is no callId, props are considered as args!
        if(!callPropsAsArgs.isEmpty()){
            return new DefaultNutsArtifactCall(null,callPropsAsArgs,null);
        }
        return null;
    }
}
