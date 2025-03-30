package net.thevpc.nuts.boot.reserved.maven;

import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.boot.reserved.cmdline.NBootArg;
import net.thevpc.nuts.boot.reserved.util.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NReservedMavenUtilsBoot {
    public static final Pattern JAR_POM_PATH = Pattern.compile("META-INF/maven/(?<g>[a-zA-Z0-9_.-]+)/(?<a>[a-zA-Z0-9_-]+)/pom.xml");
    public static final Pattern JAR_NUTS_JSON_POM_PATH = Pattern.compile("META-INF/nuts/(?<g>[a-zA-Z0-9_.-]+)/(?<a>[a-zA-Z0-9_-]+)/nuts.json");
    public static final Pattern NUTS_OS_ARCH_DEPS_PATTERN = Pattern.compile("^nuts([.](?<os>[a-zA-Z0-9-_]+)-os)?([.](?<arch>[a-zA-Z0-9-_]+)-arch)?-dependencies$");
    public static final Pattern PATTERN_TARGET_CLASSES = Pattern.compile("(?<src>.*)[/\\\\]+target[/\\\\]+classes[/\\\\]*");

    public NReservedMavenUtilsBoot() {
    }

    /**
     * detect artifact ids from a URL. Work in very simplistic way :
     * It looks for pom.xml or nuts.json files and parses them with simplistic heuristics
     * (do not handle comments or multiline values for XML)
     *
     * @param url to look into!
     * @return list of detected urls
     */
    public static NBootId[] resolveJarIds(URL url) {
        File file = NBootUtils.toFile(url);
        if (file != null) {
            if (file.isDirectory()) {
                Matcher m = PATTERN_TARGET_CLASSES
                        .matcher(file.getPath().replace('/', File.separatorChar));
                if (m.find()) {
                    String src = m.group("src");
                    if (new File(src, "pom.xml").exists()) {
                        Map<String, String> map = resolvePomTagValues(new String[]{
                                "groupId", "artifactId", "version"
                        }, new File(src, "pom.xml"));
                        String groupId = map.get("groupId");
                        String artifactId = map.get("artifactId");
                        String version = map.get("version");
                        if (groupId != null && artifactId != null && version != null) {
                            return new NBootId[]{NBootId.of(
                                    groupId, artifactId, version
                            )};
                        }
                    }
                }


                return new NBootId[0];
            } else if (file.isFile()) {
                List<NBootId> all = new ArrayList<>();
                String fileName = file.getName().toLowerCase();
                if (fileName.endsWith(".jar") || fileName.endsWith(".zip")) {
                    try (ZipFile zf = new ZipFile(file)) {

                        Enumeration<? extends ZipEntry> zipEntries = zf.entries();
                        while (zipEntries.hasMoreElements()) {
                            ZipEntry entry = zipEntries.nextElement();
                            String currPath = entry.getName();
                            Matcher m = JAR_POM_PATH.matcher(currPath);
                            if (m.find()) {
                                String groupId = m.group("g");
                                String artifactId = m.group("a");
                                //now detect version from pom
                                try (InputStream is = zf.getInputStream(entry)) {
                                    Map<String, String> map = resolvePomTagValues(new String[]{"groupId", "artifactId", "version"}, is);
                                    if (map.containsKey("version")) {
                                        String version = map.get("version");
                                        all.add(NBootId.of(groupId, artifactId, version));
                                    }
                                }
                            }
                            m = JAR_NUTS_JSON_POM_PATH.matcher(currPath);
                            if (m.find()) {
                                String groupId = m.group("g");
                                String artifactId = m.group("a");
                                //now detect version from pom
                                try (InputStream is = zf.getInputStream(entry)) {
                                    try (Reader r = new InputStreamReader(is)) {
                                        Object p = new NBootJsonParser(r).parse();
                                        if (p instanceof Map) {
                                            Map<?, ?> map = ((Map<?, ?>) p);
                                            Object v = map.get("version");
                                            if (v instanceof String) {
                                                all.add(NBootId.of(groupId, artifactId, ((String) v)));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        //
                    }
                }
                return all.toArray(new NBootId[0]);
            }
        }
        return new NBootId[0];
    }

    public static String getFileName(NBootId id, String ext) {
        return id.getArtifactId() + "-" + id.getVersion() + "." + ext;
    }

    public static String toMavenPath(NBootId nutsId) {
        return NBootUtils.resolveIdPath(nutsId);
    }

    public static String resolveMavenFullPath(NBootRepositoryLocation repo, NBootId nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + getFileName(nutsId, ext);
        String mvnUrl = repo.getPath();
        String sep = "/";
        if (!NBootUtils.isURL(mvnUrl)) {
            sep = File.separator;
        }
        if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
            mvnUrl += sep;
        }
        return mvnUrl + jarPath;
    }


    public static String getPathFile(NBootId id, String name) {
        return toMavenPath(id) + "/" + name;
    }

    public static File resolveOrDownloadJar(NBootId nutsId, NBootRepositoryLocation[] repositories, NBootRepositoryLocation cacheFolder, boolean includeDesc, Instant expire, NBootErrorInfoList errors) {
        File cachedJarFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
        if (cachedJarFile.isFile()) {
            if (NBootUtils.isFileAccessible(cachedJarFile.toPath(), expire)) {
                return cachedJarFile;
            }
        }
        NBootLog log = NBootContext.log();
        for (NBootRepositoryLocation r : repositories) {
            log.with().level(Level.FINE).verbCache().log(NBootMsg.ofC("checking %s from %s", nutsId, r));
//                File file = toFile(r);
            if (includeDesc) {
                String path = resolveMavenFullPath(r, nutsId, "pom");
                File cachedPomFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "pom"));
                try {
                    NBootUtils.copy(path, cachedPomFile);
                } catch (Exception ex) {
                    errors.add(new NReservedErrorInfo(nutsId, r.toString(), path, "unable to load descriptor", ex));
                    log.with().level(Level.SEVERE).verbFail().log(NBootMsg.ofC("unable to load descriptor %s from %s.", nutsId, r));
                    continue;
                }
            }
            String path = resolveMavenFullPath(r, nutsId, "jar");
            try {
                NBootUtils.copy(path, cachedJarFile);
                log.with().level(Level.CONFIG).verbCache().log(NBootMsg.ofC("cache jar file %s", cachedJarFile.getPath()));
                errors.removeErrorsFor(nutsId);
                return cachedJarFile;
            } catch (Exception ex) {
                errors.add(new NReservedErrorInfo(nutsId, r.toString(), path, "unable to load binaries", ex));
                log.with().level(Level.SEVERE).verbFail().log(NBootMsg.ofC("unable to load binaries %s from %s.", nutsId, r));
            }
        }
        return null;
    }

    public static Set<NBootId> loadDependenciesFromId(NBootId rid, Collection<NBootRepositoryLocation> repos) {
        String pomPath = NBootUtils.resolveFilePath(rid, "pom");
        String nutsPath = NBootUtils.resolveFilePath(rid, "nuts");
        Set<NBootId> deps = null;
        for (NBootRepositoryLocation baseUrl : repos) {
            String loc = baseUrl.getPath();
            Set<String> urls = expandRepoUrls(baseUrl);
            for (String url : urls) {
                if (url != null) {
                    deps = loadDependenciesFromPomUrl(url + "/" + pomPath);
                    if (deps != null) {
                        return deps;
                    }
                }
            }
            if (loc != null) {
                if (loc.startsWith("htmlfs:")) {
                    loc = loc.substring("htmlfs:".length());
                }
                deps = loadDependenciesFromPomUrl(loc + "/" + pomPath);
                if (deps != null) {
                    break;
                }
                deps = loadDependenciesFromNutsUrl(loc + "/" + nutsPath);
                if (deps != null) {
                    break;
                }
            }
        }
        return deps;
    }


    public static Set<NBootId> loadDependenciesFromNutsUrl(String url) {
        InputStream inputStream = NBootUtils.resolveInputStream(url);
        Map<String, Object> descNuts = null;
        if (inputStream != null) {
            try {
                NBootJsonParser parser = null;
                parser = new NBootJsonParser(new InputStreamReader(inputStream));
                descNuts = parser.parseObject();
                List<String> dependencies = (List<String>) descNuts.get("dependencies");
                if (dependencies == null) {
                    return new LinkedHashSet<>();
                }
                return dependencies.stream().map(x -> NBootId.of(x)).collect(Collectors.toSet());
            } finally {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    //throw new RuntimeException(e);
                }
            }
        }
        return null;
    }


    private static Set<NBootId> loadDependenciesFromPomUrl(String url) {
        LinkedHashSet<NBootId> depsSet = new LinkedHashSet<>();
        InputStream xml = NBootUtils.resolveInputStream(url);
        if (xml != null) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();

                Document doc = null;
                try {
                    doc = builder.parse(xml);
                } catch (SAXParseException ex) {
                    throw ex;
                }
                Element c = doc.getDocumentElement();
                Map<String, String> osMap = new HashMap<>();
                Map<String, String> archMap = new HashMap<>();
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
                                String optional = null;
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
                                            case "optional": {
                                                optional = c4.getTextContent().trim();
                                                break;
                                            }
                                        }
                                    }
                                }
                                if (NBootUtils.isBlank(groupId)) {
                                    throw new NBootException(NBootMsg.ofPlain("unexpected empty groupId"));
                                } else if (groupId.contains("$")) {
                                    throw new NBootException(NBootMsg.ofC("unexpected maven variable in groupId=%s", groupId));
                                }
                                if (NBootUtils.isBlank(artifactId)) {
                                    throw new NBootException(NBootMsg.ofPlain("unexpected empty artifactId"));
                                } else if (artifactId.contains("$")) {
                                    throw new NBootException(NBootMsg.ofC("unexpected maven variable in artifactId=%s", artifactId));
                                }
                                if (NBootUtils.isBlank(version)) {
                                    throw new NBootException(NBootMsg.ofPlain("unexpected empty artifactId"));
                                } else if (version.contains("$")) {
                                    throw new NBootException(NBootMsg.ofC("unexpected maven variable in artifactId=%s", version));
                                }
                                //this is maven dependency, using "compile"
                                if (NBootUtils.isBlank(scope) || scope.equals("compile")) {
                                    boolean optionalBool = NBootUtils.parseBooleanOr(optional, false);
                                    depsSet.add(
                                            NBootId.of(
                                                            groupId,
                                                            artifactId,
                                                            version)
                                                    .setProperty(NBootConstants.IdProperties.OPTIONAL,
                                                            optionalBool ? Boolean.TRUE.toString() : null)
                                                    .setCondition(
                                                            new NBootEnvCondition()
                                                                    .setOs(Arrays.asList(osMap.get(groupId + ":" + artifactId)))
                                                                    .setArch(Arrays.asList(archMap.get(groupId + ":" + artifactId))))
                                    );
                                } else if (version.contains("$")) {
                                    throw new NBootException(NBootMsg.ofC("unexpected maven variable in artifactId=%s", version));
                                }
                            }
                        }
                    } else if (c.getChildNodes().item(i) instanceof Element && c.getChildNodes().item(i).getNodeName().equals("properties")) {
                        Element c2 = (Element) c.getChildNodes().item(i);
                        for (int j = 0; j < c2.getChildNodes().getLength(); j++) {
                            if (c2.getChildNodes().item(j) instanceof Element) {
                                Element c3 = (Element) c2.getChildNodes().item(j);
                                String nodeName = c3.getNodeName();
                                switch (nodeName) {
                                    default: {
                                        Matcher m = NUTS_OS_ARCH_DEPS_PATTERN.matcher(nodeName);
                                        if (m.find()) {
                                            String os = m.group("os");
                                            String arch = m.group("arch");
                                            String txt = c3.getTextContent().trim();
                                            for (String a : txt.trim().split("[;,\n\t]")) {
                                                a = a.trim();
                                                if (a.startsWith("#")) {
                                                    //ignore!
                                                } else {
                                                    if (!NBootUtils.isBlank(os)) {
                                                        osMap.put(a, os);
                                                    }
                                                    if (!NBootUtils.isBlank(arch)) {
                                                        archMap.put(a, arch);
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                List<NBootId> ok = new ArrayList<>();
                for (NBootId idep : depsSet) {
                    NBootDependency dep = idep.toDependency();

                    String arch = archMap.get(idep.getShortName());
                    String os = osMap.get(idep.getShortName());
                    boolean replace = false;
                    if (arch != null || os != null) {
                        if ((dep.getCondition().getOs().isEmpty() && os != null)
                                || (dep.getCondition().getArch().isEmpty() && arch != null)) {
                            replace = true;
                        }
                    }
                    if (replace) {
                        ok.add(
                                dep
                                        .setCondition(
                                                dep.getCondition().builder()
                                                        .setArch(
                                                                arch != null ? Arrays.asList(arch) : dep.getCondition().getArch())
                                                        .setOs(
                                                                arch != null ? Arrays.asList(arch) : dep.getCondition().getArch())
                                                        .build()
                                        ).toId()
                        );
                    } else {
                        ok.add(idep);
                    }
                }
                depsSet.clear();
                depsSet.addAll(ok);

            } catch (Exception ex) {
                NBootContext.log().with().level(Level.FINE).verbFail().error(ex).log(NBootMsg.ofC("unable to loadDependenciesAndRepositoriesFromPomUrl %s", url));
            } finally {
                try {
                    xml.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
            return depsSet;
        } else {
            return null;
        }
    }

    static List<NBootVersion> detectVersionsFromMetaData(String mavenMetadata) {
        List<NBootVersion> all = new ArrayList<>();
        NBootLog log = NBootContext.log();
        try {
            String urlType = "";
            if (mavenMetadata.startsWith("dotfilefs:")) {
                urlType = "dotfilefs";
                mavenMetadata = mavenMetadata.substring("dotfilefs:".length());
            }
            URL runtimeMetadata = NBootUtils.urlOf(mavenMetadata);
            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = null;
            try {
                is = NBootUtils.preloadStream(NBootUtils.openStream(runtimeMetadata));
            } catch (Exception ex) {
                //do not need to log error
                //ignore
            }
            if (is != null) {
                log.with().level(Level.FINEST).verbSuccess().log(NBootMsg.ofC("parsing %s", mavenMetadata));
                Document doc = builder.parse(is);
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
                                        NBootVersion p = NBootVersion.of(c4.getTextContent());
                                        if (!p.isBlank()) {
                                            all.add(p);
                                        }
                                    }
                                }

                            }
                        }
                    }
                    //NutsConstants.Ids.NUTS_RUNTIME.replaceAll("[.:]", "/")
                }
            }
        } catch (Exception ex) {
            log.with().level(Level.FINE).verbFail().error(ex).log(NBootMsg.ofC("unable to parse %s", mavenMetadata));
            // ignore any error
        }
        return all;
    }

    static VersionAndPath resolveLatestMavenId(NBootId zId, String path, Predicate<NBootVersion> filter,
                                               NBootRepositoryLocation repoUrl2, boolean stopFirst, NBootOptionsInfo options) {
        String descType = "MAVEN";
        if (NBootConstants.RepoTypes.NUTS.equalsIgnoreCase(repoUrl2.getLocationType())) {
            descType = "NUTS";
        }

        Set<String> urls = expandRepoUrls(repoUrl2);
        boolean found = false;
        NBootVersion bestVersion = null;
        String bestPath = null;
        String fetchStrategy = NBootUtils.firstNonNull(options.getFetchStrategy(), "ANYWHERE");
        boolean offline = !NBootUtils.sameEnum(fetchStrategy, "REMOTE");
        boolean online = !NBootUtils.sameEnum(fetchStrategy, "OFFLINE");

        NBootLog log = NBootContext.log();
        for (String repoUrl : urls) {
            if (!repoUrl.contains("://")) {
                if (offline) {
                    File mavenNutsCoreFolder = new File(repoUrl, path.replace("/", File.separator));
                    FilenameFilter filenameFilter =
                            descType.equals("NUTS") ? (dir, name) -> name.endsWith(NBootConstants.Files.DESCRIPTOR_FILE_EXTENSION)
                                    : (dir, name) -> name.endsWith(".pom");
                    if (mavenNutsCoreFolder.isDirectory()) {
                        File[] children = mavenNutsCoreFolder.listFiles();
                        if (children != null) {
                            for (File file : children) {
                                if (file.isDirectory()) {
                                    String[] goodChildren = file.list(filenameFilter);
                                    if (goodChildren != null && goodChildren.length > 0) {
                                        NBootVersion p = NBootVersion.of(file.getName());//folder name is version name
                                        if (filter == null || filter.test(p)) {
                                            found = true;
                                            if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                                //we will ignore artifact classifier to simplify search
                                                Path jarPath = file.toPath().resolve(
                                                        getFileName(NBootId.of(zId.getGroupId(), zId.getArtifactId(), p.getValue()), "jar")
                                                );
                                                if (Files.isRegularFile(jarPath)) {
                                                    bestVersion = p;
                                                    bestPath = "local location : " + jarPath;
                                                    if (log != null) {
                                                        log.with().level(Level.FINEST).verbSuccess().log(NBootMsg.ofC("%s#%s found in %s as %s", zId, bestVersion, repoUrl2, bestPath));
                                                    }
                                                    if (stopFirst) {
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                boolean htmlfs = repoUrl.startsWith("htmlfs:");
                if (htmlfs) {
                    repoUrl = repoUrl.substring("htmlfs:".length());
                }
                if (!repoUrl.endsWith("/")) {
                    repoUrl = repoUrl + "/";
                }
                String basePath = repoUrl + path;
                if (!basePath.endsWith("/")) {
                    basePath = basePath + "/";
                }
                boolean remoteURL = new NBootPath(basePath).isRemote();
                if ((remoteURL && online) || (!remoteURL && offline)) {
                    //do nothing
                    if (htmlfs) {
                        for (NBootVersion p : detectVersionsFromHtmlfsTomcatDirectoryListing(basePath)) {
                            if (filter == null || filter.test(p)) {
                                found = true;
                                if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                    bestVersion = p;
                                    bestPath = "remote file " + basePath;
                                    if (log != null) {
                                        log.with().level(Level.FINEST).verbSuccess().log(NBootMsg.ofC("%s#%s found in %s as %s", zId, bestVersion, repoUrl2, bestPath));
                                    }
                                    if (stopFirst) {
                                        break;
                                    }
                                }
                            }
                        }
                    } else {
                        int bootConnectionTimout=options.getCustomOptions().stream().map(x-> NBootArg.of(x)).filter(x->Objects.equals(x.getOptionName(),"---boot-connection-timeout")).map(x->x.getIntValue())
                            .filter(x->x!=null)
                            .findFirst().orElse(0);
                        String mavenMetadata = basePath + "maven-metadata.xml";
                        for (NBootVersion p : detectVersionsFromMetaData(mavenMetadata)) {
                            if (filter == null || filter.test(p)) {
                                found = true;
                                if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                    bestVersion = p;
                                    bestPath = "remote file " + mavenMetadata;
                                    if (log != null) {
                                        log.with().level(Level.FINEST).verbSuccess().log(NBootMsg.ofC("%s#%s found in %s as %s", zId, bestVersion, repoUrl2, bestPath));
                                    }
                                    if (stopFirst) {
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return new VersionAndPath(bestVersion, bestPath);
    }

    /**
     * find latest maven artifact
     *
     * @param filter  filter
     * @param options
     * @return latest runtime version
     */
    public static NBootId resolveLatestMavenId(NBootId zId, Predicate<NBootVersion> filter,
                                               Collection<NBootRepositoryLocation> bootRepositories, NBootOptionsInfo options) {
        NBootLog log = NBootContext.log();
        if (log.isLoggable(Level.FINEST)) {
            switch (bootRepositories.size()) {
                case 0: {
                    log.with().level(Level.FINEST).verbStart().log(NBootMsg.ofC("search for %s nuts there are no repositories to look into.", zId));
                    break;
                }
                case 1: {
                    log.with().level(Level.FINEST).verbStart().log(NBootMsg.ofC("search for %s in: %s", zId, bootRepositories.toArray()[0]));
                    break;
                }
                default: {
                    log.with().level(Level.FINEST).verbStart().log(NBootMsg.ofC("search for %s in: ", zId));
                    for (NBootRepositoryLocation repoUrl : bootRepositories) {
                        log.with().level(Level.FINEST).verbStart().log(NBootMsg.ofC("    %s", repoUrl));
                    }
                }
            }
        }
        String path = NBootUtils.resolveIdPath(zId.getShortId());
        NBootVersion bestVersion = null;
        String bestPath = null;
        boolean stopOnFirstValidRepo = false;
        for (NBootRepositoryLocation repoUrl2 : bootRepositories) {
            VersionAndPath r = resolveLatestMavenId(zId, path, filter, repoUrl2, false, options);
            if (r.version != null) {
                if (bestVersion == null || bestVersion.compareTo(r.version) < 0) {
                    bestVersion = r.version;
                    bestPath = r.path;
                    if (stopOnFirstValidRepo) {
                        break;
                    }
                }
            }
        }
        if (bestVersion == null) {
            return null;
        }
        NBootId iid = NBootId.of(zId.getGroupId(), zId.getArtifactId(), bestVersion.getValue());
        log.with().level(Level.FINEST).verbSuccess().log(NBootMsg.ofC("resolve %s from %s", iid, bestPath));
        return iid;
    }

    private static List<NBootVersion> detectVersionsFromHtmlfsTomcatDirectoryListing(String basePath) {
        List<NBootVersion> all = new ArrayList<>();
        try (InputStream in = NBootUtils.openStream(NBootUtils.urlOf(basePath))) {
            List<String> p = new HtmlfsTomcatDirectoryListParser().parse(in);
            if (p != null) {
                for (String s : p) {
                    if (s.endsWith("/")) {
                        s = s.substring(0, s.length() - 1);
                        int a = s.lastIndexOf('/');
                        if (a >= 0) {
                            String n = s.substring(a + 1);
                            NBootVersion v = NBootVersion.of(n);
                            if (!v.isBlank()) {
                                all.add(v);
                            }
                        }
                    }
                }
            }
        } catch (IOException | UncheckedIOException ex) {
            //ignore
        }
        return all;
    }

    public static File getBootCacheJar(NBootId vid, NBootRepositoryLocation[] repositories, NBootRepositoryLocation cacheFolder, boolean useCache, String name,
                                       Instant expire, NBootErrorInfoList errorList, NBootOptionsInfo bOptions,
                                       Function<String, String> pathExpansionConverter) {
        File f = getBootCacheFile(vid, getFileName(vid, "jar"), repositories, cacheFolder, useCache, expire, errorList, bOptions, pathExpansionConverter);
        if (f == null) {
            throw new NBootInvalidWorkspaceException(bOptions.getWorkspace(),
                    NBootMsg.ofC("unable to load %s %s from repositories %s", name, vid, Arrays.asList(repositories)));
        }
        return f;
    }

    static File getBootCacheFile(NBootId vid, String fileName, NBootRepositoryLocation[] repositories, NBootRepositoryLocation cacheFolder,
                                 boolean useCache, Instant expire, NBootErrorInfoList errorList,
                                 NBootOptionsInfo bOptions,
                                 Function<String, String> pathExpansionConverter) {
        String path = getPathFile(vid, fileName);
        if (useCache && cacheFolder != null) {

            File f = new File(cacheFolder.getPath(), path.replace('/', File.separatorChar));
            if (NBootUtils.isFileAccessible(f.toPath(), expire)) {
                return f;
            }
        }
        for (NBootRepositoryLocation repository : repositories) {
            if (useCache && cacheFolder != null && cacheFolder.equals(repository)) {
                return null; // I do not remember why I did this!
            }
            File file = getBootCacheFile(vid, path, repository, cacheFolder, useCache, expire, errorList, bOptions, pathExpansionConverter);
            if (file != null) {
                return file;
            }
        }
        NBootCache cache = NBootContext.cache();
        NBootIdCache e = cache.fallbackIdMap.get(vid);
        if (e != null && e.jar != null) {
            return new File(e.jar);
        }
        e = cache.fallbackIdMap.get(vid.getShortId());
        if (e != null && e.jar != null) {
            return new File(e.jar);
        }
        return null;
    }

    private static File getBootCacheFile(NBootId nutsId, String path, NBootRepositoryLocation repository0, NBootRepositoryLocation cacheFolder,
                                         boolean useCache, Instant expire, NBootErrorInfoList errorList,
                                         NBootOptionsInfo bOptions, Function<String, String> pathExpansionConverter) {
        boolean cacheLocalFiles = true;//Boolean.getBoolean("nuts.cache.cache-local-files");
        NBootLog log = NBootContext.log();

        for (String repository : expandRepoUrls(repository0)) {
            //we know exactly the file path, so we will trim "htmlfs:" protocol
            if (repository.startsWith("htmlfs:")) {
                repository = repository.substring("htmlfs:".length());
            }
            repository = NBootUtils.expandPath(repository, bOptions.getWorkspace(), pathExpansionConverter);
            File repositoryFolder = null;
            if (NBootUtils.isURL(repository)) {
                try {
                    repositoryFolder = NBootUtils.toFile(NBootUtils.urlOf(repository));
                } catch (Exception ex) {
                    log.with().level(Level.FINE).verbFail().error(ex).log(NBootMsg.ofC("unable to convert url to file : %s", repository));
                    //ignore
                }
            } else {
                repositoryFolder = new File(repository);
            }
            if (repositoryFolder == null) {
                if (cacheFolder == null) {
                    return null;
                }
                File ok = null;
                File to = new File(cacheFolder.getPath(), path);
                String urlPath = repository;
                if (!urlPath.endsWith("/")) {
                    urlPath += "/";
                }
                urlPath += path;
                try {
                    NBootUtils.copy(urlPath, to);
                    errorList.removeErrorsFor(nutsId);
                    ok = to;
                } catch (IOException | UncheckedIOException ex) {
                    errorList.add(new NReservedErrorInfo(nutsId, repository, urlPath, "unable to load", ex));
                    //not found
                }
                return ok;
            } else {
                repository = repositoryFolder.getPath();
            }
            File repoFolder = NBootUtils.createFile(NBootUtils.getHome("CONF", bOptions), repository);
            File ff = null;

            if (repoFolder.isDirectory()) {
                File file = new File(repoFolder, path.replace('/', File.separatorChar));
                if (file.isFile()) {
                    ff = file;
                } else {
                    log.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("locate %s", file));
                }
            } else {
                File file = new File(repoFolder, path.replace('/', File.separatorChar));
                log.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("locate %s ; repository is not a valid folder : %s", file, repoFolder));
            }

            if (ff != null) {
                if (cacheFolder != null && cacheLocalFiles) {
                    File to = new File(cacheFolder.getPath(), path);
                    String toc = NBootUtils.getAbsolutePath(to.getPath());
                    String ffc = NBootUtils.getAbsolutePath(ff.getPath());
                    if (ffc.equals(toc)) {
                        return ff;
                    }
                    try {
                        if (to.getParentFile() != null) {
                            to.getParentFile().mkdirs();
                        }
                        String ext = "config";
                        if (ff.getName().endsWith(".jar")) {
                            ext = "jar";
                        }
                        if (to.isFile()) {
                            NBootUtils.copy(ff, to);
                            log.with().level(Level.CONFIG).verbCache().log(NBootMsg.ofC("recover cached %s file %s to %s", ext, ff, to));
                        } else {
                            NBootUtils.copy(ff, to);
                            log.with().level(Level.CONFIG).verbCache().log(NBootMsg.ofC("cache %s file %s to %s", ext, ff, to));
                        }
                        return to;
                    } catch (IOException | UncheckedIOException ex) {
                        errorList.add(new NReservedErrorInfo(nutsId, repository, ff.getPath(), "unable to cache", ex));
                        log.with().level(Level.CONFIG).verbFail().log(NBootMsg.ofC("error caching file %s to %s : %s", ff, to, ex.toString()));
                        //not found
                    }
                    return ff;

                }
                return ff;
            }
        }
        return null;
    }

    public static boolean isMavenSettingsRepo(NBootRepositoryLocation loc) {
        if (
                "maven".equals(loc.getName())
                        || "maven".equals(loc.getLocationType())
        ) {
            if (NBootUtils.isBlank(loc.getPath()) || "maven".equals(loc.getPath())) {
                return true;
            }
        }
        return false;
    }

    private static Set<String> expandRepoUrls(NBootRepositoryLocation repoUrl2) {
        Function<String, Object> mappingFunction = new Function<String, Object>() {
            @Override
            public Object apply(String repo) {
                Set<String> urls = new LinkedHashSet<>();
                if (isMavenSettingsRepo(repoUrl2)) {
                    Map<String, String> p = repoUrl2.getProperties();
                    Boolean local = null;
                    Boolean central = null;
                    Boolean other = null;
                    for (String s : p.keySet()) {
                        switch (NBootUtils.trim(s).toLowerCase()) {
                            case "local": {
                                local = NBootUtils.parseBoolean(p.get(s), true, local);
                                break;
                            }
                            case "no-local": {
                                local = !NBootUtils.parseBoolean(p.get(s), true, !local);
                                break;
                            }
                            case "local-only":
                            case "localonly": {
                                boolean localOnly = NBootUtils.parseBoolean(p.get(s), true, false);
                                if (localOnly) {
                                    local = true;
                                    central = false;
                                    other = false;
                                }
                                break;
                            }
                            case "central": {
                                central = NBootUtils.parseBoolean(p.get(s), true, central);
                                break;
                            }
                            case "no-central": {
                                central = !NBootUtils.parseBoolean(p.get(s), true, !central);
                                break;
                            }
                            case "central-only":
                            case "centralonly": {
                                boolean centralOnly = NBootUtils.parseBoolean(p.get(s), true, false);
                                if (centralOnly) {
                                    local = false;
                                    central = true;
                                    other = false;
                                }
                                break;
                            }
                            case "other": {
                                other = NBootUtils.parseBoolean(p.get(s), true, other);
                                break;
                            }
                            case "no-other": {
                                other = !NBootUtils.parseBoolean(p.get(s), true, !other);
                                break;
                            }
                            case "other-only":
                            case "otheronly": {
                                boolean otherOnly = NBootUtils.parseBoolean(p.get(s), true, false);
                                if (otherOnly) {
                                    local = false;
                                    central = false;
                                    other = true;
                                }
                                break;
                            }
                        }
                    }
                    if (local == null) {
                        local = true;
                    }
                    if (central == null) {
                        central = true;
                    }
                    if (other == null) {
                        other = true;
                    }
                    urls.addAll(loadMavenSettingsUrls(local, central, other));
                } else {
                    urls.add(repoUrl2.getPath());
                }
                return urls;
            }
        };
        return (Set<String>) NBootContext.cache().get((NMavenSettingsBoot.class.getName() + "::expandRepoUrls::" + repoUrl2), mappingFunction);
    }


    private static Set<String> loadMavenSettingsUrls(boolean local, boolean central, boolean other) {
        NMavenSettingsBoot mavenSettings = (NMavenSettingsBoot) NBootContext.cache().get(NMavenSettingsBoot.class.getName(), x -> new NMavenSettingsLoaderBoot().loadSettingsRepos());
        Set<String> urls = new LinkedHashSet<>();
        if (local) {
            urls.add(mavenSettings.getLocalRepository());
        }
        if (central) {
            urls.add(mavenSettings.getRemoteRepository());
        }
        if (other) {
            for (NBootRepositoryLocation activeRepository : mavenSettings.getActiveRepositories()) {
                urls.add(activeRepository.getPath());
            }
        }
        return urls;
    }

    public static String resolveNutsApiVersionFromClassPath(NBootContext bContext) {
        return resolveNutsApiPomPattern("version", bContext);
    }

    public static String resolveNutsApiPomPattern(String propName, NBootContext bContext) {
//        boolean devMode = false;
        String propValue = null;
        try {
            URL resource = NBootWorkspaceImpl.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.properties");
            if (resource != null) {
                switch (propName) {
                    case "groupId":
                    case "artifactId":
                    case "version": {
                        propValue = NBootUtils.loadURLProperties(
                                resource,
                                null, false).getProperty(propName);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            //
        }
        if (!NBootUtils.isBlank(propValue)) {
            return propValue;
        }
        URL pomXml = NBootWorkspaceImpl.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.xml");
        if (pomXml != null) {
            try (InputStream is = NBootUtils.openStream(pomXml)) {
                propValue = resolvePomTagValues(new String[]{propName}, is).get(propName);
            } catch (Exception ex) {
                //
            }
        }
        if (!NBootUtils.isBlank(propValue)) {
            return propValue;
        }
        //check if we are in dev mode
        String cp = System.getProperty("java.class.path");
        for (String p : cp.split(File.pathSeparator)) {
            File f = new File(p);
            if (f.isDirectory()) {
                Matcher m = Pattern.compile("(?<src>.*)[/\\\\]+target[/\\\\]+classes[/\\\\]*")
                        .matcher(f.getPath().replace('/', File.separatorChar));
                if (m.find()) {
                    String src = m.group("src");
                    if (new File(src, "pom.xml").exists() && new File(src,
                            "src/main/java/net/thevpc/nuts/Nuts.java".replace('/', File.separatorChar)
                    ).exists()) {
                        propValue = resolvePomTagValues(new String[]{propName}, new File(src, "pom.xml")).get(propName);
                    }
                }
            }
        }
        if (!NBootUtils.isBlank(propValue)) {
            return propValue;
        }
        try {
            URL resource = NBootWorkspaceImpl.class.getResource("/META-INF/nuts/net.thevpc.nuts/nuts/nuts.properties");
            if (resource != null) {
                switch (propName) {
                    case "groupId":
                    case "artifactId":
                    case "version": {
                        String id = NBootUtils.loadURLProperties(
                                resource,
                                null, false).getProperty("id");
                        if (!NBootUtils.isBlank(id)) {
                            NBootId nId = NBootId.of(id);
                            switch (propName) {
                                case "groupId":
                                    propValue = nId.getGroupId();
                                    break;
                                case "artifactId":
                                    propValue = nId.getArtifactId();
                                    break;
                                case "version":
                                    propValue = nId.getVersion().toString();
                                    break;
                            }
                        }
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            //
        }
        if (!NBootUtils.isBlank(propValue)) {
            return propValue;
        }
        return null;
    }

    public static Map<String, String> resolvePomTagValues(String[] propNames, File file) {
        if (file != null && file.isFile()) {
            try (InputStream is = Files.newInputStream(file.toPath())) {
                return resolvePomTagValues(propNames, is);
            } catch (IOException e) {
                //
            }
        }
        return new HashMap<>();
    }

    public static Map<String, String> resolvePomTagValues(String[] propNames, InputStream is) {
//        boolean devMode = false;
        String propValue = null;
        StringBuilder sb = new StringBuilder("<(?<name>");
        for (int i = 0; i < propNames.length; i++) {
            if (i > 0) {
                sb.append("|");
            }
            sb.append(propNames[i].replace(".", "[.]"));
        }
        sb.append(")>");
        sb.append("(?<value>[^<]*)");
        sb.append("</(?<name2>");
        for (int i = 0; i < propNames.length; i++) {
            if (i > 0) {
                sb.append("|");
            }
            sb.append(propNames[i].replace(".", "[.]"));
        }
        sb.append(")>");
        Pattern pattern = Pattern.compile(sb.toString());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            NBootUtils.copy(is, bos, false, false);
        } catch (Exception ex) {
            //
        }
        Map<String, String> map = new HashMap<>();
        Matcher m = pattern.matcher(bos.toString());
        while (m.find()) {
            String n = m.group("name").trim();
            String n2 = m.group("name2").trim();
            //n==n2
            propValue = m.group("value").trim();
            //only consider the very first!
            if (!map.containsKey(n)) {
                map.put(n, propValue);
            }
        }
        return map;
    }

    private enum SimpleTomcatDirectoryListParserState {
        EXPECT_DOCTYPE,
        EXPECT_BODY,
        EXPECT_PRE,
        EXPECT_HREF,
    }

    private static class VersionAndPath {
        NBootVersion version;
        String path;

        public VersionAndPath(NBootVersion version, String path) {
            this.version = version;
            this.path = path;
        }
    }

    private static class HtmlfsTomcatDirectoryListParser {

        public List<String> parse(InputStream html) {
            try {
                List<String> found = new ArrayList<>();
                BufferedReader br = new BufferedReader(new InputStreamReader(html));
                SimpleTomcatDirectoryListParserState s = SimpleTomcatDirectoryListParserState.EXPECT_DOCTYPE;
                String line;
                while ((line = br.readLine()) != null) {
                    line = line.trim();
                    switch (s) {
                        case EXPECT_DOCTYPE: {
                            if (!line.isEmpty()) {
                                if (line.toLowerCase().startsWith("<!DOCTYPE html".toLowerCase())) {
                                    s = SimpleTomcatDirectoryListParserState.EXPECT_BODY;
                                } else if (
                                        line.toLowerCase().startsWith("<html>".toLowerCase())
                                                || line.toLowerCase().startsWith("<html ".toLowerCase())
                                ) {
                                    s = SimpleTomcatDirectoryListParserState.EXPECT_BODY;
                                } else {
                                    return null;
                                }
                            }
                            break;
                        }
                        case EXPECT_BODY: {
                            if (!line.isEmpty()) {
                                if (
                                        line.toLowerCase()
                                                .startsWith("<body>".toLowerCase())
                                                || line.toLowerCase()
                                                .startsWith("<body ".toLowerCase())
                                ) {
                                    s = SimpleTomcatDirectoryListParserState.EXPECT_PRE;
                                }
                            }
                            break;
                        }
                        case EXPECT_PRE: {
                            if (!line.isEmpty()) {
                                String lowLine = line;
                                if (
                                        lowLine.toLowerCase()
                                                .startsWith("<pre>".toLowerCase())
                                                || lowLine.toLowerCase()
                                                .startsWith("<pre ".toLowerCase())
                                ) {
                                    //spring.io
                                    if (lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>name[ ]+last modified[ ]+size</pre>(<hr/>)?")) {
                                        //just ignore
                                    } else if (lowLine.toLowerCase().startsWith("<pre>") && lowLine.toLowerCase().matches("<pre>[ ]*<a href=.*")) {
                                        lowLine = lowLine.substring("<pre>".length()).trim();
                                        if (lowLine.toLowerCase().startsWith("<a href=\"")) {
                                            int i0 = "<a href=\"".length();
                                            int i1 = lowLine.indexOf('\"', i0);
                                            if (i1 > 0) {
                                                found.add(lowLine.substring(i0, i1));
                                                s = SimpleTomcatDirectoryListParserState.EXPECT_HREF;
                                            } else {
                                                return null;
                                            }
                                        }
                                    } else if (lowLine.toLowerCase().startsWith("<pre ")) {
                                        s = SimpleTomcatDirectoryListParserState.EXPECT_HREF;
                                    } else {
                                        //ignore
                                    }
                                } else if (lowLine.toLowerCase().matches("<td .*<strong>last modified</strong>.*</td>")) {
                                    s = SimpleTomcatDirectoryListParserState.EXPECT_HREF;
                                }
                            }
                            break;
                        }
                        case EXPECT_HREF: {
                            if (!line.isEmpty()) {
                                String lowLine = line;
                                if (lowLine.toLowerCase().startsWith("</pre>".toLowerCase())) {
                                    return found;
                                }
                                if (lowLine.toLowerCase().startsWith("</html>".toLowerCase())) {
                                    return found;
                                }
                                if (lowLine.toLowerCase().startsWith("<a href=\"")) {
                                    int i0 = "<a href=\"".length();
                                    int i1 = lowLine.indexOf('\"', i0);
                                    if (i1 > 0) {
                                        found.add(lowLine.substring(i0, i1));
                                    } else {
                                        //ignore
                                    }
                                }
                            }
                            break;
                        }
                    }
                }
            } catch (Exception ex) {
                //ignore
            }
            return null;
        }
    }
}
