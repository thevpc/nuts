package net.thevpc.nuts.boot.reserved.maven;

import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.boot.*;
import net.thevpc.nuts.boot.reserved.*;
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
    public static NIdBoot[] resolveJarIds(URL url) {
        File file = NReservedIOUtilsBoot.toFile(url);
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
                            return new NIdBoot[]{NIdBoot.of(
                                    groupId, artifactId, version
                            )};
                        }
                    }
                }


                return new NIdBoot[0];
            } else if (file.isFile()) {
                List<NIdBoot> all = new ArrayList<>();
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
                                        all.add(NIdBoot.of(groupId, artifactId, version));
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
                                        Object p = new NReservedJsonParser(r).parse();
                                        if (p instanceof Map) {
                                            Map<?, ?> map = ((Map<?, ?>) p);
                                            Object v = map.get("version");
                                            if (v instanceof String) {
                                                all.add(NIdBoot.of(groupId, artifactId, ((String) v)));
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
                return all.toArray(new NIdBoot[0]);
            }
        }
        return new NIdBoot[0];
    }

    public static String getFileName(NIdBoot id, String ext) {
        return id.getArtifactId() + "-" + id.getVersion() + "." + ext;
    }

    public static String toMavenPath(NIdBoot nutsId) {
        return NIdUtilsBoot.resolveIdPath(nutsId);
    }

    public static String resolveMavenFullPath(NRepositoryLocationBoot repo, NIdBoot nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + getFileName(nutsId, ext);
        String mvnUrl = repo.getPath();
        String sep = "/";
        if (!NReservedIOUtilsBoot.isURL(mvnUrl)) {
            sep = File.separator;
        }
        if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
            mvnUrl += sep;
        }
        return mvnUrl + jarPath;
    }


    public static String getPathFile(NIdBoot id, String name) {
        return toMavenPath(id) + "/" + name;
    }

    public static File resolveOrDownloadJar(NIdBoot nutsId, NRepositoryLocationBoot[] repositories, NRepositoryLocationBoot cacheFolder, NLogBoot bLog, boolean includeDesc, Instant expire, NReservedErrorInfoList errors) {
        File cachedJarFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
        if (cachedJarFile.isFile()) {
            if (NReservedIOUtilsBoot.isFileAccessible(cachedJarFile.toPath(), expire, bLog)) {
                return cachedJarFile;
            }
        }
        for (NRepositoryLocationBoot r : repositories) {
            bLog.with().level(Level.FINE).verbCache().log(NMsgBoot.ofC("checking %s from %s", nutsId, r));
//                File file = toFile(r);
            if (includeDesc) {
                String path = resolveMavenFullPath(r, nutsId, "pom");
                File cachedPomFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "pom"));
                try {
                    NReservedIOUtilsBoot.copy(path, cachedPomFile, bLog);
                } catch (Exception ex) {
                    errors.add(new NReservedErrorInfo(nutsId, r.toString(), path, "unable to load descriptor", ex));
                    bLog.with().level(Level.SEVERE).verbFail().log(NMsgBoot.ofC("unable to load descriptor %s from %s.", nutsId, r));
                    continue;
                }
            }
            String path = resolveMavenFullPath(r, nutsId, "jar");
            try {
                NReservedIOUtilsBoot.copy(path, cachedJarFile, bLog);
                bLog.with().level(Level.CONFIG).verbCache().log(NMsgBoot.ofC("cache jar file %s", cachedJarFile.getPath()));
                errors.removeErrorsFor(nutsId);
                return cachedJarFile;
            } catch (Exception ex) {
                errors.add(new NReservedErrorInfo(nutsId, r.toString(), path, "unable to load binaries", ex));
                bLog.with().level(Level.SEVERE).verbFail().log(NMsgBoot.ofC("unable to load binaries %s from %s.", nutsId, r));
            }
        }
        return null;
    }

    public static Set<NIdBoot> loadDependenciesFromId(NIdBoot rid, NLogBoot bLog, Collection<NRepositoryLocationBoot> repos, NBootCache cache) {
        String pomPath = NIdUtilsBoot.resolveFilePath(rid, "pom");
        String nutsPath = NIdUtilsBoot.resolveFilePath(rid, "nuts");
        Set<NIdBoot> deps = null;
        for (NRepositoryLocationBoot baseUrl : repos) {
            String loc = baseUrl.getPath();
            if (loc != null) {
                if (isMavenSettingsRepo(baseUrl)) {
                    Set<String> urls = loadMavenSettingsUrls(bLog, cache);
                    for (String url : urls) {
                        deps = loadDependenciesFromPomUrl(url + "/" + pomPath, bLog);
                        if (deps != null) {
                            return deps;
                        }
                    }
                    //this is a special cas
                }
                if (loc.startsWith("htmlfs:")) {
                    loc = loc.substring("htmlfs:".length());
                }
                deps = loadDependenciesFromPomUrl(loc + "/" + pomPath, bLog);
                if (deps != null) {
                    break;
                }
                deps = loadDependenciesFromNutsUrl(loc + "/" + nutsPath, bLog);
                if (deps != null) {
                    break;
                }
            }
        }
        return deps;
    }


    public static Set<NIdBoot> loadDependenciesFromNutsUrl(String url, NLogBoot bLog) {
        InputStream inputStream = NReservedIOUtilsBoot.resolveInputStream(url, bLog);
        Map<String, Object> descNuts = null;
        if (inputStream != null) {
            try {
                NReservedJsonParser parser = null;
                parser = new NReservedJsonParser(new InputStreamReader(inputStream));
                descNuts = parser.parseObject();
                List<String> dependencies = (List<String>) descNuts.get("dependencies");
                if (dependencies == null) {
                    return new LinkedHashSet<>();
                }
                return dependencies.stream().map(x -> NIdBoot.of(x)).collect(Collectors.toSet());
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


    private static Set<NIdBoot> loadDependenciesFromPomUrl(String url, NLogBoot bLog) {
        LinkedHashSet<NIdBoot> depsSet = new LinkedHashSet<>();
        InputStream xml = NReservedIOUtilsBoot.resolveInputStream(url, bLog);
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
                                if (NStringUtilsBoot.isBlank(groupId)) {
                                    throw new NBootException(NMsgBoot.ofPlain("unexpected empty groupId"));
                                } else if (groupId.contains("$")) {
                                    throw new NBootException(NMsgBoot.ofC("unexpected maven variable in groupId=%s", groupId));
                                }
                                if (NStringUtilsBoot.isBlank(artifactId)) {
                                    throw new NBootException(NMsgBoot.ofPlain("unexpected empty artifactId"));
                                } else if (artifactId.contains("$")) {
                                    throw new NBootException(NMsgBoot.ofC("unexpected maven variable in artifactId=%s", artifactId));
                                }
                                if (NStringUtilsBoot.isBlank(version)) {
                                    throw new NBootException(NMsgBoot.ofPlain("unexpected empty artifactId"));
                                } else if (version.contains("$")) {
                                    throw new NBootException(NMsgBoot.ofC("unexpected maven variable in artifactId=%s", version));
                                }
                                //this is maven dependency, using "compile"
                                if (NStringUtilsBoot.isBlank(scope) || scope.equals("compile")) {
                                    boolean optionalBool = NUtilsBoot.parseBooleanOr(optional,false);
                                    depsSet.add(
                                            NIdBoot.of(
                                                            groupId,
                                                            artifactId,
                                                            version)
                                                    .setProperty(NConstants.IdProperties.OPTIONAL,
                                                            optionalBool ? Boolean.TRUE.toString() : null)
                                                    .setCondition(
                                                            new NEnvConditionBoot()
                                                                    .setOs(Arrays.asList(osMap.get(groupId + ":" + artifactId)))
                                                                    .setArch(Arrays.asList(archMap.get(groupId + ":" + artifactId))))
                                    );
                                } else if (version.contains("$")) {
                                    throw new NBootException(NMsgBoot.ofC("unexpected maven variable in artifactId=%s", version));
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
                                                    if (!NStringUtilsBoot.isBlank(os)) {
                                                        osMap.put(a, os);
                                                    }
                                                    if (!NStringUtilsBoot.isBlank(arch)) {
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
                List<NIdBoot> ok = new ArrayList<>();
                for (NIdBoot idep : depsSet) {
                    NDependencyBoot dep = idep.toDependency();

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
                bLog.with().level(Level.FINE).verbFail().error(ex).log(NMsgBoot.ofC("unable to loadDependenciesAndRepositoriesFromPomUrl %s", url));
            } finally {
                try {
                    xml.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }
        return depsSet;
    }

    static List<NVersionBoot> detectVersionsFromMetaData(String mavenMetadata, NLogBoot bLog) {
        List<NVersionBoot> all = new ArrayList<>();
        try {
            URL runtimeMetadata = new URL(mavenMetadata);
            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = null;
            try {
                is = NReservedIOUtilsBoot.preloadStream(NReservedIOUtilsBoot.openStream(runtimeMetadata, bLog), bLog);
            } catch (Exception ex) {
                //do not need to log error
                //ignore
            }
            if (is != null) {
                bLog.with().level(Level.FINEST).verbSuccess().log(NMsgBoot.ofC("parsing %s", mavenMetadata));
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
                                        NVersionBoot p = NVersionBoot.of(c4.getTextContent());
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
            bLog.with().level(Level.FINE).verbFail().error(ex).log(NMsgBoot.ofC("unable to parse %s", mavenMetadata));
            // ignore any error
        }
        return all;
    }

    static VersionAndPath resolveLatestMavenId(NIdBoot zId, String path, Predicate<NVersionBoot> filter,
                                               NLogBoot bLog, NRepositoryLocationBoot repoUrl2, boolean stopFirst, NBootOptionsBoot options) {
        String descType = "MAVEN";
        if (NConstants.RepoTypes.NUTS.equalsIgnoreCase(repoUrl2.getLocationType())) {
            descType = "NUTS";
        }
        String repoUrl = repoUrl2.getPath();
        boolean found = false;
        NVersionBoot bestVersion = null;
        String bestPath = null;
        String fetchStrategy = NUtilsBoot.firstNonNull(options.getFetchStrategy(),"ANYWHERE");
        boolean offline = !NUtilsBoot.sameEnum(fetchStrategy,"REMOTE");
        boolean online = !NUtilsBoot.sameEnum(fetchStrategy,"OFFLINE");
        if (!repoUrl.contains("://")) {
            if (offline) {
                File mavenNutsCoreFolder = new File(repoUrl, path.replace("/", File.separator));
                FilenameFilter filenameFilter =
                        descType.equals("NUTS") ? (dir, name) -> name.endsWith(".nuts")
                                : (dir, name) -> name.endsWith(".pom");
                if (mavenNutsCoreFolder.isDirectory()) {
                    File[] children = mavenNutsCoreFolder.listFiles();
                    if (children != null) {
                        for (File file : children) {
                            if (file.isDirectory()) {
                                String[] goodChildren = file.list(filenameFilter);
                                if (goodChildren != null && goodChildren.length > 0) {
                                    NVersionBoot p = NVersionBoot.of(file.getName());//folder name is version name
                                    if (filter == null || filter.test(p)) {
                                        found = true;
                                        if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                            //we will ignore artifact classifier to simplify search
                                            Path jarPath = file.toPath().resolve(
                                                    getFileName(NIdBoot.of(zId.getGroupId(), zId.getArtifactId(), p.getValue()), "jar")
                                            );
                                            if (Files.isRegularFile(jarPath)) {
                                                bestVersion = p;
                                                bestPath = "local location : " + jarPath;
                                                if (bLog != null) {
                                                    bLog.with().level(Level.FINEST).verbSuccess().log(NMsgBoot.ofC("%s#%s found in %s as %s", zId, bestVersion, repoUrl2, bestPath));
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
            return new VersionAndPath(bestVersion, bestPath);
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
            boolean remoteURL = new NReservedPath(basePath).isRemote();
            if ((remoteURL && online) || (!remoteURL && offline)) {
                //do nothing
                if (htmlfs) {
                    for (NVersionBoot p : detectVersionsFromHtmlfsTomcatDirectoryListing(basePath, bLog)) {
                        if (filter == null || filter.test(p)) {
                            found = true;
                            if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                bestVersion = p;
                                bestPath = "remote file " + basePath;
                                if (bLog != null) {
                                    bLog.with().level(Level.FINEST).verbSuccess().log(NMsgBoot.ofC("%s#%s found in %s as %s", zId, bestVersion, repoUrl2, bestPath));
                                }
                                if (stopFirst) {
                                    break;
                                }
                            }
                        }
                    }
                } else {
                    String mavenMetadata = basePath + "maven-metadata.xml";
                    for (NVersionBoot p : detectVersionsFromMetaData(mavenMetadata, bLog)) {
                        if (filter == null || filter.test(p)) {
                            found = true;
                            if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                bestVersion = p;
                                bestPath = "remote file " + mavenMetadata;
                                if (bLog != null) {
                                    bLog.with().level(Level.FINEST).verbSuccess().log(NMsgBoot.ofC("%s#%s found in %s as %s", zId, bestVersion, repoUrl2, bestPath));
                                }
                                if (stopFirst) {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
            return new VersionAndPath(bestVersion, bestPath);
        }
    }

    /**
     * find latest maven artifact
     *
     * @param filter  filter
     * @param options
     * @return latest runtime version
     */
    public static NIdBoot resolveLatestMavenId(NIdBoot zId, Predicate<NVersionBoot> filter,
                                           NLogBoot bLog, Collection<NRepositoryLocationBoot> bootRepositories, NBootOptionsBoot options) {
        if (bLog.isLoggable(Level.FINEST)) {
            switch (bootRepositories.size()) {
                case 0: {
                    bLog.with().level(Level.FINEST).verbStart().log(NMsgBoot.ofC("search for %s nuts there are no repositories to look into.", zId));
                    break;
                }
                case 1: {
                    bLog.with().level(Level.FINEST).verbStart().log(NMsgBoot.ofC("search for %s in: %s", zId, bootRepositories.toArray()[0]));
                    break;
                }
                default: {
                    bLog.with().level(Level.FINEST).verbStart().log(NMsgBoot.ofC("search for %s in: ", zId));
                    for (NRepositoryLocationBoot repoUrl : bootRepositories) {
                        bLog.with().level(Level.FINEST).verbStart().log(NMsgBoot.ofC("    %s", repoUrl));
                    }
                }
            }
        }
        String path = NIdUtilsBoot.resolveIdPath(zId.getShortId());
        NVersionBoot bestVersion = null;
        String bestPath = null;
        boolean stopOnFirstValidRepo = false;
        for (NRepositoryLocationBoot repoUrl2 : bootRepositories) {
            VersionAndPath r = resolveLatestMavenId(zId, path, filter, bLog, repoUrl2, false, options);
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
        NIdBoot iid = NIdBoot.of(zId.getGroupId(), zId.getArtifactId(), bestVersion.getValue());
        bLog.with().level(Level.FINEST).verbSuccess().log(NMsgBoot.ofC("resolve %s from %s", iid, bestPath));
        return iid;
    }

    private static List<NVersionBoot> detectVersionsFromHtmlfsTomcatDirectoryListing(String basePath, NLogBoot bLog) {
        List<NVersionBoot> all = new ArrayList<>();
        try (InputStream in = NReservedIOUtilsBoot.openStream(new URL(basePath), bLog)) {
            List<String> p = new HtmlfsTomcatDirectoryListParser().parse(in);
            if (p != null) {
                for (String s : p) {
                    if (s.endsWith("/")) {
                        s = s.substring(0, s.length() - 1);
                        int a = s.lastIndexOf('/');
                        if (a >= 0) {
                            String n = s.substring(a + 1);
                            NVersionBoot v = NVersionBoot.of(n);
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

    public static File getBootCacheJar(NIdBoot vid, NRepositoryLocationBoot[] repositories, NRepositoryLocationBoot cacheFolder, boolean useCache, String name,
                                       Instant expire, NReservedErrorInfoList errorList, NBootOptionsBoot bOptions,
                                       Function<String, String> pathExpansionConverter, NLogBoot bLog, NBootCache cache) {
        File f = getBootCacheFile(vid, getFileName(vid, "jar"), repositories, cacheFolder, useCache, expire, errorList, bOptions, pathExpansionConverter, bLog, cache);
        if (f == null) {
            throw new NInvalidWorkspaceException(bOptions.getWorkspace(),
                    NMsgBoot.ofC("unable to load %s %s from repositories %s", name, vid, Arrays.asList(repositories)));
        }
        return f;
    }

    static File getBootCacheFile(NIdBoot vid, String fileName, NRepositoryLocationBoot[] repositories, NRepositoryLocationBoot cacheFolder,
                                 boolean useCache, Instant expire, NReservedErrorInfoList errorList,
                                 NBootOptionsBoot bOptions,
                                 Function<String, String> pathExpansionConverter, NLogBoot bLog, NBootCache cache) {
        String path = getPathFile(vid, fileName);
        if (useCache && cacheFolder != null) {

            File f = new File(cacheFolder.getPath(), path.replace('/', File.separatorChar));
            if (NReservedIOUtilsBoot.isFileAccessible(f.toPath(), expire, bLog)) {
                return f;
            }
        }
        for (NRepositoryLocationBoot repository : repositories) {
            if (useCache && cacheFolder != null && cacheFolder.equals(repository)) {
                return null; // I do not remember why I did this!
            }
            File file = getBootCacheFile(vid, path, repository, cacheFolder, useCache, expire, errorList, bOptions, pathExpansionConverter, bLog, cache);
            if (file != null) {
                return file;
            }
        }
        NIdCache e = cache.fallbackIdMap.get(vid);
        if (e != null && e.jar != null) {
            return new File(e.jar);
        }
        e = cache.fallbackIdMap.get(vid.getShortId());
        if (e != null && e.jar != null) {
            return new File(e.jar);
        }
        return null;
    }

    private static File getBootCacheFile(NIdBoot nutsId, String path, NRepositoryLocationBoot repository0, NRepositoryLocationBoot cacheFolder,
                                         boolean useCache, Instant expire, NReservedErrorInfoList errorList,
                                         NBootOptionsBoot bOptions, Function<String, String> pathExpansionConverter,
                                         NLogBoot bLog, NBootCache cache) {
        boolean cacheLocalFiles = true;//Boolean.getBoolean("nuts.cache.cache-local-files");
        Set<String> urls = new LinkedHashSet<>();
        if (isMavenSettingsRepo(repository0)) {
            urls.addAll(loadMavenSettingsUrls(bLog, cache));
        } else {
            urls.add(repository0.getPath());
        }
        for (String repository : urls) {
            //we know exactly the file path, so we will trim "htmlfs:" protocol
            if (repository.startsWith("htmlfs:")) {
                repository = repository.substring("htmlfs:".length());
            }
            repository = NReservedIOUtilsBoot.expandPath(repository, bOptions.getWorkspace(), pathExpansionConverter);
            File repositoryFolder = null;
            if (NReservedIOUtilsBoot.isURL(repository)) {
                try {
                    repositoryFolder = NReservedIOUtilsBoot.toFile(new URL(repository));
                } catch (Exception ex) {
                    bLog.with().level(Level.FINE).verbFail().error(ex).log(NMsgBoot.ofC("unable to convert url to file : %s", repository));
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
                    NReservedIOUtilsBoot.copy(urlPath, to, bLog);
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
            File repoFolder = NReservedIOUtilsBoot.createFile(NUtilsBoot.getHome("CONF", bOptions), repository);
            File ff = null;

            if (repoFolder.isDirectory()) {
                File file = new File(repoFolder, path.replace('/', File.separatorChar));
                if (file.isFile()) {
                    ff = file;
                } else {
                    bLog.with().level(Level.CONFIG).verbFail().log(NMsgBoot.ofC("locate %s", file));
                }
            } else {
                File file = new File(repoFolder, path.replace('/', File.separatorChar));
                bLog.with().level(Level.CONFIG).verbFail().log(NMsgBoot.ofC("locate %s ; repository is not a valid folder : %s", file, repoFolder));
            }

            if (ff != null) {
                if (cacheFolder != null && cacheLocalFiles) {
                    File to = new File(cacheFolder.getPath(), path);
                    String toc = NReservedIOUtilsBoot.getAbsolutePath(to.getPath());
                    String ffc = NReservedIOUtilsBoot.getAbsolutePath(ff.getPath());
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
                            NReservedIOUtilsBoot.copy(ff, to, bLog);
                            bLog.with().level(Level.CONFIG).verbCache().log(NMsgBoot.ofC("recover cached %s file %s to %s", ext, ff, to));
                        } else {
                            NReservedIOUtilsBoot.copy(ff, to, bLog);
                            bLog.with().level(Level.CONFIG).verbCache().log(NMsgBoot.ofC("cache %s file %s to %s", ext, ff, to));
                        }
                        return to;
                    } catch (IOException ex) {
                        errorList.add(new NReservedErrorInfo(nutsId, repository, ff.getPath(), "unable to cache", ex));
                        bLog.with().level(Level.CONFIG).verbFail().log(NMsgBoot.ofC("error caching file %s to %s : %s", ff, to, ex.toString()));
                        //not found
                    }
                    return ff;

                }
                return ff;
            }
        }
        return null;
    }

    public static boolean isMavenSettingsRepo(NRepositoryLocationBoot loc) {
        if ("maven".equals(loc.getPath()) && "maven".equals(loc.getName())) {
            return true;
        }
        return false;
    }

    private static Set<String> loadMavenSettingsUrls(NLogBoot bLog, NBootCache cache) {
        NMavenSettingsBoot mavenSettings = (NMavenSettingsBoot) cache.cache.computeIfAbsent(NMavenSettingsBoot.class.getName(), x -> new NMavenSettingsLoaderBoot(bLog).loadSettingsRepos());
        Set<String> urls = new LinkedHashSet<>();
        urls.add(mavenSettings.getLocalRepository());
        urls.add(mavenSettings.getRemoteRepository());
        for (NRepositoryLocationBoot activeRepository : mavenSettings.getActiveRepositories()) {
            urls.add(activeRepository.getPath());
        }
        return urls;
    }

    public static String resolveNutsApiVersionFromClassPath(NLogBoot bLog) {
        return resolveNutsApiPomPattern("version", bLog);
    }

    public static String resolveNutsApiPomPattern(String propName, NLogBoot bLog) {
//        boolean devMode = false;
        String propValue = null;
        try {
            URL resource = NBootWorkspace.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.properties");
            if (resource != null) {
                switch (propName) {
                    case "groupId":
                    case "artifactId":
                    case "version": {
                        propValue = NReservedIOUtilsBoot.loadURLProperties(
                                resource,
                                null, false, bLog).getProperty(propName);
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            //
        }
        if (!NStringUtilsBoot.isBlank(propValue)) {
            return propValue;
        }
        URL pomXml = NBootWorkspace.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.xml");
        if (pomXml != null) {
            try (InputStream is = NReservedIOUtilsBoot.openStream(pomXml, bLog)) {
                propValue = resolvePomTagValues(new String[]{propName}, is).get(propName);
            } catch (Exception ex) {
                //
            }
        }
        if (!NStringUtilsBoot.isBlank(propValue)) {
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
        if (!NStringUtilsBoot.isBlank(propValue)) {
            return propValue;
        }
        try {
            URL resource = NBootWorkspace.class.getResource("/META-INF/nuts/net.thevpc.nuts/nuts/nuts.properties");
            if (resource != null) {
                switch (propName) {
                    case "groupId":
                    case "artifactId":
                    case "version": {
                        String id = NReservedIOUtilsBoot.loadURLProperties(
                                resource,
                                null, false, bLog).getProperty("id");
                        if(!NStringUtilsBoot.isBlank(id)){
                            NIdBoot nId = NIdBoot.of(id);
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
        if (!NStringUtilsBoot.isBlank(propValue)) {
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
            NReservedIOUtilsBoot.copy(is, bos, false, false);
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
        NVersionBoot version;
        String path;

        public VersionAndPath(NVersionBoot version, String path) {
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
