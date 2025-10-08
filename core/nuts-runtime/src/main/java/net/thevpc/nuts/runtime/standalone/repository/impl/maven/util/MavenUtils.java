/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.maven.util;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetchCmd;
import net.thevpc.nuts.command.NFetchMode;
import net.thevpc.nuts.command.NFetchStrategy;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.expr.NParseException;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NShellFamily;
import net.thevpc.nuts.core.NAddRepositoryOptions;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.runtime.standalone.*;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.text.NPositionType;
import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.*;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.*;
import net.thevpc.nuts.runtime.standalone.format.xml.XmlUtils;
import net.thevpc.nuts.runtime.standalone.util.MapToFunction;
import net.thevpc.nuts.runtime.standalone.xtra.expr.StringTokenizerUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreNUtils;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.util.*;
import org.w3c.dom.Element;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * Created by vpc on 2/20/17.
 */
public class MavenUtils {


    private MavenUtils() {
    }

    public static MavenUtils of() {
        MavenUtils wp = (MavenUtils) NWorkspace.of().getProperties().get(MavenUtils.class.getName());
        if (wp == null) {
            wp = new MavenUtils();
            NWorkspace.of().setProperty(MavenUtils.class.getName(), wp);
        }
        return wp;
    }

    public static NPomIdResolver createPomIdResolver(NWorkspace workspace) {
        NPomIdResolver wp = (NPomIdResolver) NWorkspace.of().getProperties().get(NPomIdResolver.class.getName());
        if (wp == null) {
            wp = new NPomIdResolver();
            NWorkspace.of().setProperty(NPomIdResolver.class.getName(), wp);
        }
        return wp;
    }

    public List<NId> toNutsId(List<NPomId> ids) {
        return ids.stream().map(this::toNutsId).collect(Collectors.toList());
    }

    public NDependency[] toNutsDependencies(NPomDependency[] deps, NPom pom, NPomProfileActivation ac, String profile) {
        NDependency[] a = new NDependency[deps.length];
        for (int i = 0; i < deps.length; i++) {
            a[i] = toNutsDependency(deps[i], pom, ac, profile);
        }
        return a;
    }

    public NId toNutsId(NPomId d) {
        return NIdBuilder.of(d.getGroupId(), d.getArtifactId()).setVersion(toNutsVersion(d.getVersion())).build();
    }

    public NEnvCondition toCondition(String os0, String arch0, NPomProfileActivation a, String profile) {
//        if (a == null) {
//            return null;
//        }
        NOsFamily os = NOsFamily.parse(os0).orNull();
        NArchFamily arch = NArchFamily.parse(arch0).orNull();
        String osVersion = null;
        String platform = null;
        Map<String, String> props = new LinkedHashMap<>();
        if (a != null) {
            if (!NBlankable.isBlank(a.getOsVersion())) {
                osVersion = a.getOsVersion();
            }
            if (!NBlankable.isBlank(a.getOsArch())) {
                arch = NArchFamily.parse(a.getOsArch()).orNull();
            }
            if (!NBlankable.isBlank(a.getOsName())) {
                NOsFamily os2 = NOsFamily.parse(a.getOsName()).orNull();
                if (os2 != null) {
                    os = os2;
                }
            } else if (!NBlankable.isBlank(a.getOsFamily())) {
                NOsFamily os2 = NOsFamily.parse(a.getOsFamily()).orNull();
                if (os2 != null) {
                    os = os2;
                }
            }
            if (!NBlankable.isBlank(a.getJdk())) {
                platform = "java#" + toNutsVersion(a.getJdk());
            }
            if (a.getPropertyName() != null) {
                props.put(a.getPropertyName(), a.getPropertyValue());
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
        NEnvConditionBuilder bb = new DefaultNEnvConditionBuilder()
                .setOs(oss == null ? null : Arrays.asList(oss))
                .setArch(ars == null ? null : Arrays.asList(ars))
                .setPlatform(platform == null ? null : Arrays.asList(platform))
                .setProfile(profile == null ? null : Arrays.asList(profile));
        bb.setProperties(props);
        return bb.build();
    }

    public NDependency toNutsDependency(NPomDependency d, NPom pom, NPomProfileActivation a, String profile) {
        String s = d.getScope();
        if (s == null) {
            s = "";
        }
        s = s.trim();
        NDependencyScope dependencyScope = NDependencyScope.API;
        switch (s) {
            case "":
            case "compile": {
                dependencyScope = NDependencyScope.API;
                break;
            }
            case "test": {
                dependencyScope = NDependencyScope.TEST_API;
                break;
            }
            case "system": {
                dependencyScope = NDependencyScope.SYSTEM;
                break;
            }
            case "runtime": {
                dependencyScope = NDependencyScope.RUNTIME;
                break;
            }
            case "provided": {
                dependencyScope = NDependencyScope.PROVIDED;
                break;
            }
            case "import": {
                dependencyScope = NDependencyScope.IMPORT;
                break;
            }
            default: {
                dependencyScope = NDependencyScope.parse(s).orElse(NDependencyScope.API);
                if (dependencyScope == null) {
                    LOG()
                            .log(NMsg.ofJ("unable to parse maven scope {0} for {1}", s, d)
                                    .asFinestFail()
                            );
                    dependencyScope = NDependencyScope.API;
                }
            }
        }
        return NDependencyBuilder.of()
                .setGroupId(d.getGroupId())
                .setArtifactId(d.getArtifactId())
                .setClassifier(d.getClassifier())
                .setVersion(toNutsVersion((d.getVersion())))
                .setOptional(d.getOptional())
                .setScope(dependencyScope.id())
                .setCondition(toCondition(d.getOs(), d.getArch(), a, profile))
                .setType(d.getType())
                .setExclusions(toNutsId(Arrays.asList(d.getExclusions())))
                .build();
    }
    private NLog LOG(){
        return NLog.of(MavenUtils.class);
    }

    public NDescriptor parsePomXml(InputStream stream, NFetchMode fetchMode, String urlDesc, NRepository repository) {
        long startTime = System.currentTimeMillis();
        try {
            if (stream == null) {
                return null;
            }
            byte[] bytes = NIOUtils.loadByteArray(stream);
            InputStream bytesStream = CoreIOUtils.createBytesStream(bytes,
                    urlDesc == null ? NMsg.ofNtf("pom.xml") : NMsg.ofNtf(urlDesc), "text/xml",
                    StandardCharsets.UTF_8.name(), urlDesc == null ? "pom.xml" : urlDesc);
            NPom pom = new NPomXmlParser().parse(bytesStream);
            LinkedHashSet<NDescriptorFlag> flags = new LinkedHashSet<>();
            if (NLiteral.of(pom.getProperties().get("nuts.executable")).asBoolean().orElse(false)) {
                flags.add(NDescriptorFlag.EXEC);
            } else {
                final Element ee = pom.getXml().getDocumentElement();
                if (XmlUtils.testNode(ee, x -> {
                    if (x instanceof Element) {
                        Element e = (Element) x;
                        if (XmlUtils.isNode(e, "build", "plugins", "plugin", "configuration", "archive", "manifest", "mainClass")) {
                            return true;
                        }
                        if (NStringUtils.trim(e.getTextContent()).equals("exec-war-only") &&
                                XmlUtils.isNode(e, "build", "plugins", "plugin", "executions", "execution", "goals", "goal")) {
                            return true;
                        }
                    }
                    return false;
                })) {
                    flags.add(NDescriptorFlag.EXEC);
                    flags.add(NDescriptorFlag.PLATFORM_APP);
                }
            }
            if (NLiteral.of(pom.getProperties().get("nuts.application")).asBoolean().orElse(false)) {
                flags.add(NDescriptorFlag.NUTS_APP);
                flags.add(NDescriptorFlag.EXEC);
            }
            if (NLiteral.of(pom.getProperties().get("nuts.gui")).asBoolean().orElse(false)) {
                flags.add(NDescriptorFlag.GUI);
                flags.add(NDescriptorFlag.EXEC);
            }
            if (NLiteral.of(pom.getProperties().get("nuts.term")).asBoolean().orElse(false)) {
                flags.add(NDescriptorFlag.TERM);
                flags.add(NDescriptorFlag.EXEC);
            }
            if (pom.getPackaging().isEmpty()) {
                pom.setPackaging("jar");
            }

            long time = System.currentTimeMillis() - startTime;
            if (fetchMode == null) {
                fetchMode = NFetchMode.REMOTE;
            }
            String fetchString = "[" + NStringUtils.formatAlign(fetchMode.id(), 7, NPositionType.FIRST) + "] ";
            LOG()
                    .log(NMsg.ofJ("{0}{1} parse pom    {2}", fetchString,
                            NStringUtils.formatAlign(repository == null ? "<no-repo>" : repository.getName(), 20, NPositionType.FIRST),
                            NText.ofStyledPath(urlDesc)
                    ).withLevel(Level.FINEST).withIntent(NMsgIntent.SUCCESS).withDurationMillis(time));

            String icons = pom.getProperties().get("nuts.icons");
            if (icons == null) {
                icons = "";
            }
            String categories = pom.getProperties().get("nuts.categories");
            if (categories == null) {
                categories = "";
            }
            NPomProfile[] profiles = pom.getProfiles();//Arrays.stream(pom.getProfiles()).filter(x -> acceptRuntimeActivation(x.getActivation())).toArray(PomProfile[]::new);
            List<NDependency> deps = new ArrayList<>(
                    Arrays.asList(toNutsDependencies(pom.getDependencies(), pom, null, null)));
            for (NPomProfile profile : profiles) {
                deps.addAll(Arrays.asList(toNutsDependencies(profile.getDependencies(), pom, profile.getActivation(), profile.getId())));
            }
            List<NDependency> depsM = new ArrayList<>(
                    Arrays.asList(toNutsDependencies(pom.getDependenciesManagement(), pom, null, null)));
            for (NPomProfile profile : profiles) {
                depsM.addAll(Arrays.asList(toNutsDependencies(profile.getDependenciesManagement(), pom, profile.getActivation(), profile.getId())));
            }
            List<NDescriptorProperty> props = new ArrayList<>();
            for (Map.Entry<String, String> e : pom.getProperties().entrySet()) {
                props.add(new DefaultNDescriptorPropertyBuilder().setName(e.getKey())
                        .setValue(e.getValue()).build());
            }
            for (NPomProfile profile : profiles) {
                for (Map.Entry<String, String> e : profile.getProperties().entrySet()) {
                    props.add(new DefaultNDescriptorPropertyBuilder()
                            .setName(e.getKey())
                            .setValue(e.getValue())
                            .setCondition(toCondition(null, null, profile.getActivation(), profile.getId()))
                            .build());
                }
            }
            NVersion mavenCompilerTarget = null;
            for (String v : new String[]{"maven.compiler.target", "project.target.level"}) {
                String vv = pom.getProperties().get(v);
                if (!NBlankable.isBlank(vv)) {
                    if (mavenCompilerTarget == null || mavenCompilerTarget.compareTo(vv) < 0) {
                        mavenCompilerTarget = NVersion.get(vv).get();
                    }
                }
            }

            Set<String> toRemoveProps = new LinkedHashSet<>();
            NArtifactCall installerCall = parseCall(
                    pom.getProperties().get("nuts.installer"),
                    pom.getProperties().get("nuts.installer.scriptName"),
                    pom.getProperties().get("nuts.installer.scriptContent")
            );
            NArtifactCall executorCall = parseCall(
                    pom.getProperties().get("nuts.executor"),
                    pom.getProperties().get("nuts.executor.scriptName"),
                    pom.getProperties().get("nuts.executor.scriptContent")
            );
            LinkedHashSet<NIdLocation> idLocations = new LinkedHashSet<>();
            NIdLocation idLocation = parseLocation(pom.getProperties(), "nuts.location", toRemoveProps);
            if (idLocation != null) {
                idLocations.add(idLocation);
            }
            String genericName = pom.getProperties().get("nuts.genericName");
            idLocation = parseLocation(pom.getProperties(), "nuts.location.0", toRemoveProps);
            if (idLocation != null) {
                idLocations.add(idLocation);
            }
            for (int i = 0; i < 32; i++) {
                idLocation = parseLocation(pom.getProperties(), "nuts.location." + i, toRemoveProps);
                if (idLocation != null) {
                    idLocations.add(idLocation);
                } else {
                    break;
                }
            }

            //delete special properties
            for (Iterator<NDescriptorProperty> iterator = props.iterator(); iterator.hasNext(); ) {
                NDescriptorProperty prop = iterator.next();
                if (prop.getCondition().isBlank()) {
                    String n = prop.getName();
                    switch (n) {
                        case "nuts.installer":
                        case "nuts.executor":
                        case "nuts.categories":
                        case "nuts.icons":
                        case "nuts.term":
                        case "nuts.gui":
                        case "nuts.application":
                        case "nuts.executable":
                        case "nuts.genericName": {
                            iterator.remove();
                            break;
                        }
                        default: {
                            if (toRemoveProps.contains(n)) {
                                iterator.remove();
                            }
                        }
                    }
                }
            }
            return new DefaultNDescriptorBuilder()
                    .setId(toNutsId(pom.getPomId()))
                    .setParents(pom.getParent() == null ? null : Arrays.asList(toNutsId(pom.getParent())))
                    .setPackaging(pom.getPackaging())
                    .setFlags(flags)
                    .setName(pom.getName())
                    .setDescription(pom.getDescription())
                    .setLocations(new ArrayList<>(idLocations))
                    .setCondition(new DefaultNEnvConditionBuilder().setPlatform(
                            Arrays.asList(mavenCompilerTarget == null ? "java" : ("java#" + mavenCompilerTarget))
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
                    .setLicenses(
                            pom.getLicenses() == null ? new ArrayList<>() :
                                    Arrays.stream(pom.getLicenses()).map(x -> {
                                        return new DefaultNDescriptorLicense(
                                                x.getName(),
                                                x.getName(),
                                                x.getUrl(),
                                                x.getDistribution(),
                                                x.getComments(),
                                                null,
                                                new LinkedHashMap<>()
                                        );
                                    }).collect(Collectors.toList())
                    )
                    .setContributors(
                            pom.getContributors() == null ? new ArrayList<>() :
                                    Arrays.stream(pom.getContributors()).map(x -> {
                                        return new DefaultNDescriptorContributor(
                                                x.getEmail(),
                                                x.getName(),
                                                x.getUrl(),
                                                x.getEmail(),
                                                new ArrayList<String>(),
                                                x.getTimeZone(),
                                                new ArrayList<String>(),
                                                new DefaultNDescriptorOrganization(
                                                        x.getOrganization(),
                                                        x.getOrganization(),
                                                        x.getOrganizationUrl(),
                                                        null,
                                                        new LinkedHashMap<>()
                                                ),
                                                x.getProperties() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(x.getProperties()),
                                                null
                                        );
                                    }).collect(Collectors.toList())
                    )
                    .setDevelopers(
                            pom.getDevelopers() == null ? new ArrayList<>() :
                                    Arrays.stream(pom.getDevelopers()).map(x -> {
                                        return new DefaultNDescriptorContributor(
                                                x.getEmail(),
                                                x.getName(),
                                                x.getUrl(),
                                                x.getEmail(),
                                                new ArrayList<String>(),
                                                x.getTimeZone(),
                                                new ArrayList<String>(),
                                                new DefaultNDescriptorOrganization(
                                                        x.getOrganization(),
                                                        x.getOrganization(),
                                                        x.getOrganizationUrl(),
                                                        null,
                                                        new LinkedHashMap<>()
                                                ),
                                                x.getProperties() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(x.getProperties()),
                                                null
                                        );
                                    }).collect(Collectors.toList())
                    )
                    .setGenericName(genericName)
                    .setProperties(props)
                    .build();
        } catch (Exception e) {
            long time = System.currentTimeMillis() - startTime;
            LOG()
                    .log(NMsg.ofJ("caching pom file {0}", urlDesc)
                            .withLevel(Level.FINEST).withIntent(NMsgIntent.FAIL).withDurationMillis(time)
                    );
            throw new NParseException(NMsg.ofC("error parsing %s", urlDesc), e);
        }
    }

    private NIdLocation parseLocation(Map<String, String> properties, String propName, Set<String> toRemoveProps) {
        String url = properties.get(propName + ".url");
        String region = properties.get(propName + ".region");
        String classifier = properties.get(propName + ".classifier");
        if (!NBlankable.isBlank(url)) {
            toRemoveProps.add(propName + ".url");
            toRemoveProps.add(propName + ".region");
            toRemoveProps.add(propName + ".classifier");
            return new NIdLocation(NStringUtils.trimToNull(url), NStringUtils.trimToNull(region), NStringUtils.trimToNull(classifier));
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

    public NDescriptor parsePomXmlAndResolveParents(NPath path, NFetchMode fetchMode, NRepository repository) {
        try {
            NSession session = NSession.of();
            session.getTerminal().printProgress(NMsg.ofC("%-8s %s", "parse", path.toCompressedForm()));
            try (InputStream is = path.getInputStream()) {
                NDescriptor nutsDescriptor = parsePomXmlAndResolveParents(is, fetchMode, path.toString(), repository);
                if (nutsDescriptor.getId().getArtifactId() == null) {
                    //why name is null ? should check out!
                    if (LOG().isLoggable(Level.FINE)) {
                        LOG()
                                .log(NMsg.ofJ("unable to fetch valid descriptor artifactId from {0} : resolved id was {1}", path, nutsDescriptor.getId()).asFineFail());
                    }
                    return null;
                }
                return nutsDescriptor;
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        }
    }

    public NDescriptor parsePomXmlAndResolveParents(InputStream stream, NFetchMode fetchMode, String urlDesc, NRepository repository) {
        NDescriptor nutsDescriptor = null;
//        if (session == null) {
//            session = ws.createSession();
//        }
        try {
            try {
//            bytes = IOUtils.loadByteArray(stream, true);
                nutsDescriptor = parsePomXml(stream, fetchMode, urlDesc, repository);
                HashMap<String, String> properties = new HashMap<>();
                NId parentId = null;
                for (NId nutsId : nutsDescriptor.getParents()) {
                    parentId = nutsId;
                }
                NDescriptor parentDescriptor = null;
                if (parentId != null) {
                    if (!CoreNUtils.isEffectiveId(parentId)) {
                        try {
                            parentDescriptor = NFetchCmd.of(parentId)
                                    .setTransitive(true)
                                    .setFetchStrategy(
                                            fetchMode == NFetchMode.REMOTE ? NFetchStrategy.ONLINE
                                                    : NFetchStrategy.OFFLINE
                                    )
                                    .setDependencyFilter(NDependencyFilters.of().byRunnable())
                                    .getResultDescriptor();
                        } catch (NException ex) {
                            throw ex;
                        } catch (Exception ex) {
                            throw new NArtifactNotFoundException(nutsDescriptor.getId(), NMsg.ofC("unable to resolve %s parent %s", nutsDescriptor.getId(), parentId, ex));
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
                    nutsDescriptor = NDescriptorUtils.applyProperties(nutsDescriptor.builder(), properties).build();
                }
                NId thisId = nutsDescriptor.getId();
                if (!CoreNUtils.isEffectiveId(thisId)) {
                    if (parentId != null) {
                        if (NBlankable.isBlank(thisId.getGroupId())) {
                            thisId = thisId.builder().setGroupId(parentId.getGroupId()).build();
                        }
                        if (NBlankable.isBlank(thisId.getVersion().getValue())) {
                            thisId = thisId.builder().setVersion(parentId.getVersion().getValue()).build();
                        }
                    }
                    HashMap<NId, NDescriptor> cache = new HashMap<>();
                    Set<String> done = new HashSet<>();
                    Stack<NId> todo = new Stack<>();
                    todo.push(nutsDescriptor.getId());
                    cache.put(nutsDescriptor.getId(), nutsDescriptor);
                    while (todo.isEmpty()) {
                        NId pid = todo.pop();
                        NDescriptor d = cache.get(pid);
                        if (d == null) {
                            try {
                                d = NFetchCmd.of(pid)
                                        .setDependencyFilter(NDependencyFilters.of().byRunnable())
                                        .getResultDescriptor();
                            } catch (NException ex) {
                                throw ex;
                            } catch (Exception ex) {
                                throw new NArtifactNotFoundException(nutsDescriptor.getId(), NMsg.ofC("unable to resolve %s parent %s", nutsDescriptor.getId(), pid, ex));
                            }
                        }
                        done.add(pid.getShortName());
                        if (CoreNUtils.containsVars(thisId)) {
                            thisId = NDescriptorUtils.applyProperties(thisId.builder(), new MapToFunction<>(
                                    NDescriptorUtils.getPropertiesMap(d.getProperties())
                            )).build();
                        } else {
                            break;
                        }
                        for (NId nutsId : d.getParents()) {
                            if (!done.contains(nutsId.getShortName())) {
                                todo.push(nutsId);
                            }
                        }
                    }
                    if (CoreNUtils.containsVars(thisId)) {
                        throw new NArtifactNotFoundException(nutsDescriptor.getId(), NMsg.ofC("unable to resolve %s parent %s", nutsDescriptor.getId(), parentId));
                    }
                    nutsDescriptor = nutsDescriptor.builder().setId(thisId).build();
                }
                NDescriptorProperty nutsPackaging = nutsDescriptor.getProperty("nuts-packaging").orNull();
                if (nutsPackaging != null && !NBlankable.isBlank(nutsPackaging.getValue())) {
                    nutsDescriptor = nutsDescriptor.builder().setPackaging(nutsDescriptor.getPropertyValue("nuts-packaging")
                                    .flatMap(NLiteral::asString).get())
                            .build();
                }
                properties.put("pom.groupId", thisId.getGroupId());
                properties.put("pom.version", thisId.getVersion().getValue());
                properties.put("pom.artifactId", thisId.getArtifactId());
                properties.put("project.groupId", thisId.getGroupId());
                properties.put("project.artifactId", thisId.getArtifactId());
                properties.put("project.version", thisId.getVersion().getValue());
                properties.put("version", thisId.getVersion().getValue());
                nutsDescriptor = NDescriptorUtils.applyProperties(
                        nutsDescriptor/*.setProperties(properties, true)*/.builder(), properties
                ).build();
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }
        } catch (IOException ex) {
            throw new NIOException(ex);
        } catch (Exception ex) {
            throw new NParseException(NMsg.ofC("error Parsing %s", urlDesc), ex);
        }
        return nutsDescriptor;
    }

//    public Iterator<NutsId> createArchetypeCatalogIterator(InputStream stream, NutsIdFilter filter, boolean autoClose) {
//        Iterator<PomId> it = ArchetypeCatalogParser.createArchetypeCatalogIterator(stream, filter == null ? null : new PomIdFilter() {
//            @Override
//            public boolean accept(PomId id) {
//                return filter.acceptId(toNutsId(id), session);
//            }
//        }, autoClose);
//        return IteratorBuilder.of(
//                NutsIterator.of(it, stream.toString()),
//                session).map(NutsFunction.of(this::toNutsId, "PomId->NutsId")).build();
//    }

    public MavenMetadata parseMavenMetaData(InputStream metadataStream) {
        MavenMetadata s = new MavenMetadataParser().parseMavenMetaData(metadataStream);
        if (s == null) {
            return s;
        }
        for (Iterator<String> iterator = s.getVersions().iterator(); iterator.hasNext(); ) {
            String version = iterator.next();
            if (!s.getLatest().isEmpty() && NVersionComparator.ofMaven().compare(NVersion.of(version), NVersion.of(s.getLatest())) > 0) {
                iterator.remove();
            }
        }
        return s;
    }

    public NArtifactCall parseCall(String callString,String scriptName,String scriptContent) {
        if (callString == null) {
            return null;
        }
        NCmdLine cl = NCmdLine.of(callString, NShellFamily.BASH).setExpandSimpleOptions(false);
        NId callId = null;
        Map<String, String> callProps = new LinkedHashMap<>();
        List<String> callPropsAsArgs = new ArrayList<>();
        while (cl.hasNext() && cl.isNextOption()) {
            NArg a = cl.next().get();
            callPropsAsArgs.add(a.toString());
            if (a.isKeyValue()) {
                callProps.put(a.getStringKey().get(), a.getStringValue().get());
            } else {
                callProps.put(a.toString(), null);
            }
        }
        if (cl.hasNext()) {
            String callIdString = cl.next().get().toString();
            callId = NId.get(callIdString).orNull();
        }
        List<String> callArgs = cl.toStringList();
        if (callId != null) {
            return new DefaultNArtifactCall(callId, callArgs,scriptName,scriptContent);
        }
        //there is no callId, props are considered as args!
        if (!callPropsAsArgs.isEmpty()) {
            return new DefaultNArtifactCall(null, callPropsAsArgs,scriptName,scriptContent);
        }
        return null;
    }

    public static boolean isMavenSettingsRepository(NAddRepositoryOptions options){
        if(!"maven".equals(options.getName())){
            return false;
        }
        if(options.getRepositoryModel()!=null){
            return false;
        }
        if(options.getConfig()!=null){
            if(!NBlankable.isBlank(options.getConfig().getName())){
                if(!"maven".equals(options.getConfig().getName())) {
                    return false;
                }
            }
            if(!NBlankable.isBlank(options.getConfig().getLocation())){
                String n = options.getConfig().getLocation().toString();
                if(!NBlankable.isBlank(n)){
                    if(!"maven".equals(n) && !"maven@maven".equals(n)){
                        return false;
                    }
                }
            }
        }
        return true;
    }
}
