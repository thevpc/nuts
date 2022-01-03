/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.spi.NutsBootId;
import net.thevpc.nuts.spi.NutsBootVersion;
import net.thevpc.nuts.spi.NutsRepositoryLocation;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @app.category Internal
 */
public final class PrivateNutsUtilMavenRepos {
    public static final Pattern JAR_POM_PATH = Pattern.compile("META-INF/maven/(?<g>[a-zA-Z0-9_.]+)/(?<a>[a-zA-Z0-9_]+)/pom.xml");
    public static final Pattern JAR_NUTS_JSON_POM_PATH = Pattern.compile("META-INF/nuts/(?<g>[a-zA-Z0-9_.]+)/(?<a>[a-zA-Z0-9_]+)/nuts.json");
    public static final Pattern NUTS_OS_ARCH_DEPS_PATTERN = Pattern.compile("^nuts([.](?<os>[a-zA-Z0-9-_]+)-os)?([.](?<arch>[a-zA-Z0-9-_]+)-arch)?-dependencies$");
    public static final Pattern PATTERN_TARGET_CLASSES = Pattern.compile("(?<src>.*)[/\\\\]+target[/\\\\]+classes[/\\\\]*");

    public PrivateNutsUtilMavenRepos() {
    }

    /**
     * detect artifact ids from an URL. Work in very simplistic way :
     * It looks for pom.xml or nuts.json files and parses them with simplistic heuristics
     * (do not handle comments or multiline values for XML)
     *
     * @param url to look into!
     * @return list of detected urls
     */
    public static NutsBootId[] resolveJarIds(URL url) {
        File file = PrivateNutsUtilIO.toFile(url);
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
                            return new NutsBootId[]{new NutsBootId(
                                    groupId, artifactId, NutsBootVersion.parse(version)
                            )};
                        }
                    }
                }


                return new NutsBootId[0];
            } else if (file.isFile()) {
                List<NutsBootId> all = new ArrayList<>();
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
                                        all.add(new NutsBootId(groupId, artifactId, NutsBootVersion.parse(version)));
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
                                        Object p = new PrivateNutsJsonParser(r).parse();
                                        if (p instanceof Map) {
                                            Map<?, ?> map = ((Map<?, ?>) p);
                                            Object v = map.get("version");
                                            if (v instanceof String) {
                                                all.add(new NutsBootId(groupId, artifactId, NutsBootVersion.parse((String) v)));
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
                return all.toArray(new NutsBootId[0]);
            }
        }
        return new NutsBootId[0];
    }

    public static String getFileName(NutsBootId id, String ext) {
        return id.getArtifactId() + "-" + id.getVersion() + "." + ext;
    }

    public static String toMavenPath(NutsBootId nutsId) {
        StringBuilder sb = new StringBuilder();
        sb.append(nutsId.getGroupId().replace(".", "/"));
        sb.append("/");
        sb.append(nutsId.getArtifactId());
        if (nutsId.getVersionString() != null && nutsId.getVersionString().length() > 0) {
            sb.append("/");
            sb.append(nutsId.getVersionString());
        }
        return sb.toString();
    }

    public static String resolveMavenFullPath(NutsRepositoryLocation repo, NutsBootId nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + getFileName(nutsId, ext);
        String mvnUrl = repo.getPath();
        String sep = "/";
        if (!PrivateNutsUtilIO.isURL(mvnUrl)) {
            sep = File.separator;
        }
        if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
            mvnUrl += sep;
        }
        return mvnUrl + jarPath;
    }


    public static String getPathFile(NutsBootId id, String name) {
        return id.getGroupId().replace('.', '/') + '/' + id.getArtifactId() + '/' + id.getVersion() + "/" + name;
    }

    public static File resolveOrDownloadJar(NutsBootId nutsId, NutsRepositoryLocation[] repositories, NutsRepositoryLocation cacheFolder, PrivateNutsBootLog bLog, boolean includeDesc, Instant expire, PrivateNutsErrorInfoList errors) {
        File cachedJarFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
        if (cachedJarFile.isFile()) {
            if (PrivateNutsUtilIO.isFileAccessible(cachedJarFile.toPath(), expire, bLog)) {
                return cachedJarFile;
            }
        }
        for (NutsRepositoryLocation r : repositories) {
            bLog.log(Level.FINE, NutsLogVerb.CACHE, NutsMessage.jstyle("checking {0} from {1}", nutsId, r));
//                File file = toFile(r);
            if (includeDesc) {
                String path = resolveMavenFullPath(r, nutsId, "pom");
                File cachedPomFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "pom"));
                try {
                    PrivateNutsUtilIO.copy(new URL(path), cachedPomFile, bLog);
                } catch (Exception ex) {
                    errors.add(new PrivateNutsErrorInfo(nutsId, r.toString(), path, "unable to load descriptor", ex));
                    bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load descriptor {0} from {1}.\n", nutsId, r));
                    continue;
                }
            }
            String path = resolveMavenFullPath(r, nutsId, "jar");
            try {
                PrivateNutsUtilIO.copy(new URL(path), cachedJarFile, bLog);
                bLog.log(Level.CONFIG, NutsLogVerb.CACHE, NutsMessage.jstyle("cache jar file {0}", cachedJarFile.getPath()));
                errors.removeErrorsFor(nutsId);
                return cachedJarFile;
            } catch (Exception ex) {
                errors.add(new PrivateNutsErrorInfo(nutsId, r.toString(), path, "unable to load binaries", ex));
                bLog.log(Level.SEVERE, NutsLogVerb.FAIL, NutsMessage.jstyle("unable to load binaries {0} from {1}.\n", nutsId, r));
            }
        }
        return null;
    }

    static Set<NutsBootId> loadDependenciesFromId(NutsBootId rid, PrivateNutsBootLog bLog, Collection<NutsRepositoryLocation> repos) {
        String urlPath = PrivateNutsUtils.idToPath(rid) + "/" + rid.getArtifactId() + "-" + rid.getVersion() + ".pom";
        Set<NutsBootId> deps = null;
        for (NutsRepositoryLocation baseUrl : repos) {
            String loc = baseUrl.getPath();
            if (loc != null) {
                if (loc.startsWith("htmlfs:")) {
                    loc = loc.substring("htmlfs:".length());
                }
                deps = loadDependenciesFromPomUrl(loc + "/" + urlPath, bLog);
                if (!deps.isEmpty()) {
                    break;
                }
            }
        }
        if (deps == null) {
            deps = new LinkedHashSet<>();
        }
        return deps;
    }

    static Set<NutsBootId> loadDependenciesFromPomUrl(String url, PrivateNutsBootLog bLog) {
        LinkedHashSet<NutsBootId> depsSet = new LinkedHashSet<>();
        InputStream xml = null;
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                URL url1 = new URL(url);
                try {
                    xml = PrivateNutsUtilIO.openStream(url1, bLog);
                } catch (NutsBootException ex) {
                    //do not need to log error
                    return depsSet;
                }
            } else if (url.startsWith("file://")) {
                URL url1 = new URL(url);
                File file = PrivateNutsUtilIO.toFile(url1);
                if (file == null) {
                    // was not able to resolve to File
                    try {
                        xml = PrivateNutsUtilIO.openStream(url1, bLog);
                    } catch (NutsBootException ex) {
                        //do not need to log error
                        return depsSet;
                    }
                } else if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsSet;
                }
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsSet;
                }
            }
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
                            if (NutsBlankable.isBlank(groupId)) {
                                throw new NutsBootException(NutsMessage.plain("unexpected empty groupId"));
                            } else if (groupId.contains("$")) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected maven variable in groupId=%s", groupId));
                            }
                            if (NutsBlankable.isBlank(artifactId)) {
                                throw new NutsBootException(NutsMessage.plain("unexpected empty artifactId"));
                            } else if (artifactId.contains("$")) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected maven variable in artifactId=%s", artifactId));
                            }
                            if (NutsBlankable.isBlank(version)) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected empty artifactId"));
                            } else if (version.contains("$")) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected maven variable in artifactId=%s", version));
                            }
                            //this is maven dependency, using "compile"
                            if (NutsBlankable.isBlank(scope) || scope.equals("compile")) {
                                depsSet.add(
                                        new NutsBootId(
                                                groupId, artifactId, NutsBootVersion.parse(version), NutsUtilStrings.parseBoolean(optional, false, false),
                                                osMap.get(groupId + ":" + artifactId),
                                                archMap.get(groupId + ":" + artifactId)
                                        )
                                );
                            } else if (version.contains("$")) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected maven variable in artifactId=%s", version));
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
                                                if (!NutsBlankable.isBlank(os)) {
                                                    osMap.put(a, os);
                                                }
                                                if (!NutsBlankable.isBlank(arch)) {
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
            List<NutsBootId> ok = new ArrayList<>();
            for (NutsBootId dep : depsSet) {
                String arch = archMap.get(dep.getShortName());
                String os = archMap.get(dep.getShortName());
                boolean replace = false;
                if (arch != null || os != null) {
                    if ((dep.getOs().isEmpty() && os != null)
                            || (dep.getArch().isEmpty() && arch != null)) {
                        replace = true;
                    }
                }
                if (replace) {
                    ok.add(new NutsBootId(dep.getGroupId(), dep.getArtifactId(), dep.getVersion(), dep.isOptional(),
                            os != null ? os : dep.getOs(),
                            arch != null ? arch : dep.getArch()
                    ));
                } else {
                    ok.add(dep);
                }
            }
            depsSet.clear();
            depsSet.addAll(ok);

        } catch (Exception ex) {
            bLog.log(Level.FINE, NutsMessage.jstyle("unable to loadDependenciesAndRepositoriesFromPomUrl {0}", url), ex);
        } finally {
            if (xml != null) {
                try {
                    xml.close();
                } catch (IOException ex) {
                    //ignore
                }
            }
        }

        return depsSet;
    }

    static List<NutsBootVersion> detectVersionsFromMetaData(String mavenMetadata, PrivateNutsBootLog bLog) {
        List<NutsBootVersion> all = new ArrayList<>();
        try {
            URL runtimeMetadata = new URL(mavenMetadata);
            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = null;
            try {
                is = PrivateNutsUtilIO.preloadStream(PrivateNutsUtilIO.openStream(runtimeMetadata, bLog), bLog);
            } catch (NutsBootException ex) {
                //do not need to log error
                //ignore
            }
            if (is != null) {
                bLog.log(Level.FINEST, NutsLogVerb.SUCCESS, NutsMessage.jstyle("parsing {0}", mavenMetadata));
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
                                        NutsBootVersion p = NutsBootVersion.parse(c4.getTextContent());
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
            bLog.log(Level.FINE, NutsMessage.jstyle("unable to parse {0}", mavenMetadata), ex);
            // ignore any error
        }
        return all;
    }

    static VersionAndPath resolveLatestMavenId(NutsBootId zId, String path, Predicate<NutsBootVersion> filter,
                                               PrivateNutsBootLog bLog, NutsRepositoryLocation repoUrl2, boolean stopFirst) {
        NutsDescriptorStyle descType = NutsDescriptorStyle.MAVEN;
        if (NutsConstants.RepoTypes.NUTS.equalsIgnoreCase(repoUrl2.getLocationType())) {
            descType = NutsDescriptorStyle.NUTS;
        }
        String repoUrl = repoUrl2.getPath();
        boolean found = false;
        NutsBootVersion bestVersion = null;
        String bestPath = null;
        if (!repoUrl.contains("://")) {
            File mavenNutsCoreFolder = new File(repoUrl, path.replace("/", File.separator));
            FilenameFilter filenameFilter =
                    descType == NutsDescriptorStyle.NUTS ? (dir, name) -> name.endsWith(".nuts")
                            : (dir, name) -> name.endsWith(".pom");
            if (mavenNutsCoreFolder.isDirectory()) {
                File[] children = mavenNutsCoreFolder.listFiles();
                if (children != null) {
                    for (File file : children) {
                        if (file.isDirectory()) {
                            String[] goodChildren = file.list(filenameFilter);
                            if (goodChildren != null && goodChildren.length > 0) {
                                NutsBootVersion p = NutsBootVersion.parse(file.getName());//folder name is version name
                                if (filter == null || filter.test(p)) {
                                    found = true;
                                    if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                        //we will ignore artifact classifier to simplify search
                                        Path jarPath = file.toPath().resolve(
                                                getFileName(new NutsBootId(zId.getGroupId(), zId.getArtifactId(), p), "jar")
                                        );
                                        if (Files.isRegularFile(jarPath)) {
                                            bestVersion = p;
                                            bestPath = "local location : " + jarPath;
                                            if (bLog != null) {
                                                bLog.log(Level.FINEST, NutsLogVerb.SUCCESS, NutsMessage.jstyle("{0}#{1} found in {2} as {3}", zId, bestVersion, repoUrl2, bestPath));
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
            if (htmlfs) {
                for (NutsBootVersion p : detectVersionsFromHtmlfsTomcatDirectoryListing(basePath, bLog)) {
                    if (filter == null || filter.test(p)) {
                        found = true;
                        if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                            bestVersion = p;
                            bestPath = "remote file " + basePath;
                            if (bLog != null) {
                                bLog.log(Level.FINEST, NutsLogVerb.SUCCESS, NutsMessage.jstyle("{0}#{1} found in {2} as {3}", zId, bestVersion, repoUrl2, bestPath));
                            }
                            if (stopFirst) {
                                break;
                            }
                        }
                    }
                }
            } else {
                String mavenMetadata = basePath + "maven-metadata.xml";
                for (NutsBootVersion p : detectVersionsFromMetaData(mavenMetadata, bLog)) {
                    if (filter == null || filter.test(p)) {
                        found = true;
                        if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                            bestVersion = p;
                            bestPath = "remote file " + mavenMetadata;
                            if (bLog != null) {
                                bLog.log(Level.FINEST, NutsLogVerb.SUCCESS, NutsMessage.jstyle("{0}#{1} found in {2} as {3}", zId, bestVersion, repoUrl2, bestPath));
                            }
                            if (stopFirst) {
                                break;
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
     * @param filter filter
     * @return latest runtime version
     */
    static NutsBootId resolveLatestMavenId(NutsBootId zId, Predicate<NutsBootVersion> filter,
                                           PrivateNutsBootLog bLog, Collection<NutsRepositoryLocation> bootRepositories) {
        if (bLog.isLoggable(Level.FINEST)) {
            if (bootRepositories.isEmpty()) {
                bLog.log(Level.FINEST, NutsLogVerb.START, NutsMessage.jstyle("search for {0} nuts there are no repositories to look into.", zId));
            } else if (bootRepositories.size() == 1) {
                bLog.log(Level.FINEST, NutsLogVerb.START, NutsMessage.jstyle("search for {0} in: {1}", zId, bootRepositories.toArray()[0]));
            } else {
                bLog.log(Level.FINEST, NutsLogVerb.START, NutsMessage.jstyle("search for {0} in: ", zId));
                for (NutsRepositoryLocation repoUrl : bootRepositories) {
                    bLog.log(Level.FINEST, NutsLogVerb.START, NutsMessage.jstyle("    {0}", repoUrl));
                }
            }
        }
        String path = zId.getGroupId().replace('.', '/') + '/' + zId.getArtifactId();
        NutsBootVersion bestVersion = null;
        String bestPath = null;
        boolean stopOnFirstValidRepo = false;
        for (NutsRepositoryLocation repoUrl2 : bootRepositories) {
            VersionAndPath r = resolveLatestMavenId(zId, path, filter, bLog, repoUrl2, false);
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
        NutsBootId iid = new NutsBootId(zId.getGroupId(), zId.getArtifactId(), bestVersion);
        bLog.log(Level.FINEST, NutsLogVerb.SUCCESS, NutsMessage.jstyle("resolve {0} from {1}", iid, bestPath));
        return iid;
    }

    private static List<NutsBootVersion> detectVersionsFromHtmlfsTomcatDirectoryListing(String basePath, PrivateNutsBootLog bLog) {
        List<NutsBootVersion> all = new ArrayList<>();
        try (InputStream in = PrivateNutsUtilIO.openStream(new URL(basePath), bLog)) {
            List<String> p = new HtmlfsTomcatDirectoryListParser().parse(in);
            if (p != null) {
                for (String s : p) {
                    if (s.endsWith("/")) {
                        s = s.substring(0, s.length() - 1);
                        int a = s.lastIndexOf('/');
                        if (a >= 0) {
                            String n = s.substring(a + 1);
                            NutsBootVersion v = NutsBootVersion.parse(n);
                            if (!v.isBlank()) {
                                all.add(v);
                            }
                        }
                    }
                }
            }
        } catch (IOException ex) {
            //ignore
        }
        return all;
    }

    static File getBootCacheJar(NutsBootId vid, NutsRepositoryLocation[] repositories, NutsRepositoryLocation cacheFolder, boolean useCache, String name,
                                Instant expire, PrivateNutsErrorInfoList errorList, NutsBootOptions bOptions,
                                Function<String, String> pathExpansionConverter, PrivateNutsBootLog bLog) {
        File f = getBootCacheFile(vid, getFileName(vid, "jar"), repositories, cacheFolder, useCache, expire, errorList, bOptions, pathExpansionConverter, bLog);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(bOptions.getWorkspace(),
                    NutsMessage.cstyle("unable to load %s %s from repositories %s", name, vid, Arrays.asList(repositories)));
        }
        return f;
    }

    static File getBootCacheFile(NutsBootId vid, String fileName, NutsRepositoryLocation[] repositories, NutsRepositoryLocation cacheFolder,
                                 boolean useCache, Instant expire, PrivateNutsErrorInfoList errorList,
                                 NutsBootOptions bOptions,
                                 Function<String, String> pathExpansionConverter, PrivateNutsBootLog bLog) {
        String path = getPathFile(vid, fileName);
        if (useCache && cacheFolder != null) {

            File f = new File(cacheFolder.getPath(), path.replace('/', File.separatorChar));
            if (PrivateNutsUtilIO.isFileAccessible(f.toPath(), expire, bLog)) {
                return f;
            }
        }
        for (NutsRepositoryLocation repository : repositories) {
            if (useCache && cacheFolder != null && cacheFolder.equals(repository)) {
                return null; // I do not remember why I did this!
            }
            File file = getBootCacheFile(vid, path, repository, cacheFolder, useCache, expire, errorList, bOptions, pathExpansionConverter, bLog);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private static File getBootCacheFile(NutsBootId nutsId, String path, NutsRepositoryLocation repository0, NutsRepositoryLocation cacheFolder,
                                         boolean useCache, Instant expire, PrivateNutsErrorInfoList errorList,
                                         NutsBootOptions bOptions, Function<String, String> pathExpansionConverter,
                                         PrivateNutsBootLog bLog) {
        boolean cacheLocalFiles = true;//Boolean.getBoolean("nuts.cache.cache-local-files");
        String repository = repository0.getPath();
        //we know exactly the file path, so we will trim "htmlfs:" protocol
        if (repository.startsWith("htmlfs:")) {
            repository = repository.substring("htmlfs:".length());
        }
        repository = PrivateNutsUtilIO.expandPath(repository, bOptions.getWorkspace(), pathExpansionConverter);
        File repositoryFolder = null;
        if (PrivateNutsUtilIO.isURL(repository)) {
            try {
                repositoryFolder = PrivateNutsUtilIO.toFile(new URL(repository));
            } catch (Exception ex) {
                bLog.log(Level.FINE, NutsMessage.jstyle("unable to convert url to file : {0}", repository), ex);
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
            long start = System.currentTimeMillis();
            try {
                PrivateNutsUtilIO.copy(new URL(urlPath), to, bLog);
                errorList.removeErrorsFor(nutsId);
                ok = to;
            } catch (IOException ex) {
                errorList.add(new PrivateNutsErrorInfo(nutsId, repository, urlPath, "unable to load", ex));
                //not found
            }
            return ok;
        } else {
            repository = repositoryFolder.getPath();
        }
        File repoFolder = PrivateNutsUtilIO.createFile(PrivateNutsUtils.getHome(NutsStoreLocation.CONFIG, bOptions), repository);
        File ff = null;

        if (repoFolder.isDirectory()) {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            if (file.isFile()) {
                ff = file;
            } else {
                bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("locate {0}", file));
            }
        } else {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("locate {0} ; repository is not a valid folder : {1}", file, repoFolder));
        }

        if (ff != null) {
            if (cacheFolder != null && cacheLocalFiles) {
                File to = new File(cacheFolder.getPath(), path);
                String toc = PrivateNutsUtilIO.getAbsolutePath(to.getPath());
                String ffc = PrivateNutsUtilIO.getAbsolutePath(ff.getPath());
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
                        PrivateNutsUtilIO.copy(ff, to, bLog);
                        bLog.log(Level.CONFIG, NutsLogVerb.CACHE, NutsMessage.jstyle("recover cached {0} file {0} to {1}", ext, ff, to));
                    } else {
                        PrivateNutsUtilIO.copy(ff, to, bLog);
                        bLog.log(Level.CONFIG, NutsLogVerb.CACHE, NutsMessage.jstyle("cache {0} file {0} to {1}", ext, ff, to));
                    }
                    return to;
                } catch (IOException ex) {
                    errorList.add(new PrivateNutsErrorInfo(nutsId, repository, ff.getPath(), "unable to cache", ex));
                    bLog.log(Level.CONFIG, NutsLogVerb.FAIL, NutsMessage.jstyle("error caching file {0} to {1} : {2}", ff, to, ex.toString()));
                    //not found
                }
                return ff;

            }
            return ff;
        }
        return null;
    }

    public static String resolveNutsApiVersionFromClassPath(PrivateNutsBootLog bLog) {
        return resolveNutsApiPomPattern("version", bLog);
    }

    public static String resolveNutsApiPomPattern(String propName, PrivateNutsBootLog bLog) {
//        boolean devMode = false;
        String propValue = null;
        try {
            switch (propName) {
                case "groupId":
                case "artifactId":
                case "versionId": {
                    propValue = PrivateNutsUtilIO.loadURLProperties(
                            Nuts.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.properties"),
                            null, false, bLog).getProperty(propName);
                    break;
                }
            }
        } catch (Exception ex) {
            //
        }
        if (!NutsBlankable.isBlank(propValue)) {
            return propValue;
        }
        URL pomXml = Nuts.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.xml");
        if (pomXml != null) {
            try (InputStream is = PrivateNutsUtilIO.openStream(pomXml, bLog)) {
                propValue = resolvePomTagValues(new String[]{propName}, is).get(propName);
            } catch (Exception ex) {
                //
            }
        }
        if (!NutsBlankable.isBlank(propValue)) {
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
        if (!NutsBlankable.isBlank(propValue)) {
            return propValue;
        }
        return null;
    }

    public static Map<String, String> resolvePomTagValues(String[] propNames, File file) {
        if (file != null && file.isFile()) {
            try (InputStream is = new FileInputStream(file)) {
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
            PrivateNutsUtilIO.copy(is, bos, false, false);
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
        NutsBootVersion version;
        String path;

        public VersionAndPath(NutsBootVersion version, String path) {
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
