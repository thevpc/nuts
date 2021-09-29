package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

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

/**
 * @app.category Internal
 */
public final class PrivateNutsUtilMaven {
    public static final Pattern JAR_POM_PATH = Pattern.compile("META-INF/maven/(?<g>[a-zA-Z0-9_.]+)/(?<a>[a-zA-Z0-9_]+)/pom.xml");
    public static final Pattern JAR_NUTS_JSON_POM_PATH = Pattern.compile("META-INF/nuts/(?<g>[a-zA-Z0-9_.]+)/(?<a>[a-zA-Z0-9_]+)/nuts.json");

    public PrivateNutsUtilMaven() {
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
                Matcher m = Pattern.compile("(?<src>.*)[/\\\\]+target[/\\\\]+classes[/\\\\]*")
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

    public static String resolveMavenFullPath(String repo, NutsBootId nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + getFileName(nutsId, ext);
        String mvnUrl = repo;
        String sep = "/";
        if (!PrivateNutsUtilIO.isURL(repo)) {
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

    public static File resolveOrDownloadJar(NutsBootId nutsId, String[] repositories, String cacheFolder, PrivateNutsLog LOG, boolean includeDesc, Instant expire, PrivateNutsErrorInfoList errors) {
        File cachedJarFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "jar"));
        if (cachedJarFile.isFile()) {
            if (PrivateNutsUtils.isFileAccessible(cachedJarFile.toPath(), expire, LOG)) {
                return cachedJarFile;
            }
        }
        for (String r : repositories) {
            LOG.log(Level.FINE, NutsLogVerb.CACHE, "checking {0} from {1}", new Object[]{nutsId, r});
//                File file = toFile(r);
            if (includeDesc) {
                String path = resolveMavenFullPath(r, nutsId, "pom");
                File cachedPomFile = new File(resolveMavenFullPath(cacheFolder, nutsId, "pom"));
                try {
                    PrivateNutsUtilIO.copy(new URL(path), cachedPomFile, LOG);
                } catch (Exception ex) {
                    errors.add(new PrivateNutsErrorInfo(nutsId, r, path, "unable to load descriptor", ex));
                    LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "unable to load descriptor {0} from {1}.\n", new Object[]{nutsId, r});
                    continue;
                }
            }
            String path = resolveMavenFullPath(r, nutsId, "jar");
            try {
                PrivateNutsUtilIO.copy(new URL(path), cachedJarFile, LOG);
                LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "cache jar file {0}", new Object[]{cachedJarFile.getPath()});
                errors.removeErrorsFor(nutsId);
                return cachedJarFile;
            } catch (Exception ex) {
                errors.add(new PrivateNutsErrorInfo(nutsId, r, path, "unable to load binaries", ex));
                LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "unable to load binaries {0} from {1}.\n", new Object[]{nutsId, r});
            }
        }
        return null;
    }

    static PrivateNutsUtils.Deps loadDependencies(NutsBootId rid, PrivateNutsLog LOG, Collection<String> repos) {
        String urlPath = PrivateNutsUtils.idToPath(rid) + "/" + rid.getArtifactId() + "-" + rid.getVersion() + ".pom";
        return loadDependencies(urlPath, LOG, repos);
    }

    static PrivateNutsUtils.Deps loadDependencies(String urlPath, PrivateNutsLog LOG, Collection<String> repos) {
        PrivateNutsUtils.Deps depsAndRepos = null;
        for (String baseUrl : repos) {
            depsAndRepos = loadDependenciesAndRepositoriesFromPomUrl(baseUrl + "/" + urlPath, LOG);
            if (!depsAndRepos.deps.isEmpty()) {
                break;
            }
        }
        return depsAndRepos;
    }

    static PrivateNutsUtils.Deps loadDependenciesAndRepositoriesFromPomUrl(String url, PrivateNutsLog LOG) {
        PrivateNutsUtils.Deps depsAndRepos = new PrivateNutsUtils.Deps();
        InputStream xml = null;
        try {
            if (url.startsWith("http://") || url.startsWith("https://")) {
                URL url1 = new URL(url);
                try {
                    xml = url1.openStream();
                } catch (IOException ex) {
                    //do not need to log error
                    return depsAndRepos;
                }
            } else if (url.startsWith("file://")) {
                URL url1 = new URL(url);
                File file = PrivateNutsUtilIO.toFile(url1);
                if (file == null) {
                    // was not able to resolve to File
                    try {
                        xml = url1.openStream();
                    } catch (IOException ex) {
                        //do not need to log error
                        return depsAndRepos;
                    }
                } else if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsAndRepos;
                }
            } else {
                File file = new File(url);
                if (file.isFile()) {
                    xml = Files.newInputStream(file.toPath());
                } else {
                    return depsAndRepos;
                }
            }
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xml);
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
                                depsAndRepos.deps.add(
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
                                case "nuts-runtime-repositories": {
                                    String t = c3.getTextContent().trim();
                                    if (t.length() > 0) {
                                        depsAndRepos.repos.addAll(
                                                Arrays.stream(t.split(";"))
                                                        .map(String::trim)
                                                        .filter(x -> x.length() > 0)
                                                        .collect(Collectors.toList())
                                        );
                                    }
                                    break;
                                }
                                default: {
                                    if (nodeName.startsWith("dependencies.")) {
                                        String np = nodeName.substring("dependencies.".length());
                                        if (np.endsWith(".os")) {
                                            String iid = np.substring(0, np.length() - 3);
                                            String os = c3.getTextContent().trim();
                                            osMap.put(iid, os);
                                        } else if (np.endsWith(".arch")) {
                                            String iid = np.substring(0, np.length() - 5);
                                            String arch = c3.getTextContent().trim();
                                            archMap.put(iid, arch);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            List<NutsBootId> ok = new ArrayList<>();
            for (NutsBootId dep : depsAndRepos.deps) {
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
            depsAndRepos.deps.clear();
            depsAndRepos.deps.addAll(ok);

        } catch (Exception ex) {
            LOG.log(Level.FINE, "unable to loadDependenciesAndRepositoriesFromPomUrl " + url, ex);
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

    static List<NutsBootVersion> detectVersionsFromMetaData(String mavenMetadata, PrivateNutsLog LOG) {
        List<NutsBootVersion> all = new ArrayList<>();
        try {
            URL runtimeMetadata = new URL(mavenMetadata);
            DocumentBuilderFactory factory
                    = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream is = null;
            try {
                is = runtimeMetadata.openStream();
            } catch (IOException ex) {
                //do not need to log error
                //ignore
            }
            if (is != null) {
                LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "parsing " + mavenMetadata);
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
            LOG.log(Level.FINE, "unable to parse " + mavenMetadata, ex);
            // ignore any error
        }
        return all;
    }

    /**
     * find latest maven artifact
     *
     * @param filter filter
     * @return latest runtime version
     */
    static NutsBootId resolveLatestMavenId(NutsBootId zId, Predicate<NutsBootVersion> filter, PrivateNutsLog LOG, Collection<String> bootRepositories) {
        if (LOG.isLoggable(Level.FINEST)) {
            if (bootRepositories.isEmpty()) {
                LOG.log(Level.FINEST, NutsLogVerb.START, "search for {0} nuts there are no repositories to look into.", zId);
            } else if (bootRepositories.size() == 1) {
                LOG.log(Level.FINEST, NutsLogVerb.START, "search for {0} in: {1}", new Object[]{zId, bootRepositories.toArray()[0]});
            } else {
                LOG.log(Level.FINEST, NutsLogVerb.START, "search for {0} in: ", zId);
                for (String repoUrl : bootRepositories) {
                    LOG.log(Level.FINEST, NutsLogVerb.START, "    {0}", repoUrl);
                }
            }
        }
        String path = zId.getGroupId().replace('.', '/') + '/' + zId.getArtifactId();
        NutsBootVersion bestVersion = null;
        String bestPath = null;
        boolean stopOnFirstValidRepo = false;
        for (String repoUrl : bootRepositories) {
            boolean found = false;
            if (!repoUrl.contains("://")) {
                File mavenNutsCoreFolder = new File(repoUrl, path.replace("/", File.separator));
                if (mavenNutsCoreFolder.isDirectory()) {
                    File[] children = mavenNutsCoreFolder.listFiles();
                    if (children != null) {
                        for (File file : children) {
                            if (file.isDirectory()) {
                                String[] goodChildren = file.list((dir, name) -> name.endsWith(".pom"));
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
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                if (!repoUrl.endsWith("/")) {
                    repoUrl = repoUrl + "/";
                }
                String basePath = repoUrl + path;
                if (!basePath.endsWith("/")) {
                    basePath = basePath + "/";
                }
                String mavenMetadata = basePath + "maven-metadata.xml";
                for (NutsBootVersion p : detectVersionsFromMetaData(mavenMetadata, LOG)) {
                    if (filter == null || filter.test(p)) {
                        found = true;
                        if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                            bestVersion = p;
                            bestPath = "remote file " + mavenMetadata;
                        }
                    }
                }
                if (!found) {
                    for (NutsBootVersion p : detectVersionsFromTomcatDirectoryListing(basePath)) {
                        if (filter == null || filter.test(p)) {
                            found = true;
                            if (bestVersion == null || bestVersion.compareTo(p) < 0) {
                                bestVersion = p;
                                bestPath = "remote file " + basePath;
                            }
                        }
                    }
                }
            }
            if (stopOnFirstValidRepo && found) {
                break;
            }
        }
        if (bestVersion == null) {
            return null;
        }
        NutsBootId iid = new NutsBootId(zId.getGroupId(), zId.getArtifactId(), bestVersion);
        LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "resolve " + iid + " from " + bestPath);
        return iid;
    }

    private static List<NutsBootVersion> detectVersionsFromTomcatDirectoryListing(String basePath) {
        List<NutsBootVersion> all = new ArrayList<>();
        try (InputStream in = new URL(basePath).openStream()) {
            List<String> p = new SimpleTomcatDirectoryListParser().parse(in);
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

    static File getBootCacheJar(NutsBootId vid, String[] repositories, String cacheFolder, boolean useCache, String name, Instant expire, PrivateNutsErrorInfoList errorList, PrivateNutsWorkspaceInitInformation workspaceInformation, Function<String, String> pathExpansionConverter, PrivateNutsLog LOG) {
        File f = getBootCacheFile(vid, getFileName(vid, "jar"), repositories, cacheFolder, useCache, expire, errorList, workspaceInformation, pathExpansionConverter, LOG);
        if (f == null) {
            throw new NutsInvalidWorkspaceException(workspaceInformation.getWorkspaceLocation(),
                    NutsMessage.cstyle("unable to load %s %s from repositories %s", name, vid, Arrays.asList(repositories)));
        }
        return f;
    }

    static File getBootCacheFile(NutsBootId vid, String fileName, String[] repositories, String cacheFolder, boolean useCache, Instant expire, PrivateNutsErrorInfoList errorList, PrivateNutsWorkspaceInitInformation workspaceInformation, Function<String, String> pathExpansionConverter, PrivateNutsLog LOG) {
        String path = getPathFile(vid, fileName);
        if (useCache && cacheFolder != null) {

            File f = new File(cacheFolder, path.replace('/', File.separatorChar));
            if (PrivateNutsUtils.isFileAccessible(f.toPath(), expire, LOG)) {
                return f;
            }
        }
        for (String repository : repositories) {
            if (useCache && cacheFolder != null && cacheFolder.equals(repository)) {
                return null; // I do not remember why I did this!
            }
            File file = getBootCacheFile(vid, path, repository, cacheFolder, useCache, expire, errorList, workspaceInformation, pathExpansionConverter, LOG);
            if (file != null) {
                return file;
            }
        }
        return null;
    }

    private static File getBootCacheFile(NutsBootId nutsId, String path, String repository, String cacheFolder, boolean useCache, Instant expire, PrivateNutsErrorInfoList errorList, PrivateNutsWorkspaceInitInformation workspaceInformation, Function<String, String> pathExpansionConverter, PrivateNutsLog LOG) {
        boolean cacheLocalFiles = true;//Boolean.getBoolean("nuts.cache.cache-local-files");
        repository = PrivateNutsUtilIO.expandPath(repository, workspaceInformation.getWorkspaceLocation(), pathExpansionConverter);
        File repositoryFolder = null;
        if (PrivateNutsUtilIO.isURL(repository)) {
            try {
                repositoryFolder = PrivateNutsUtilIO.toFile(new URL(repository));
            } catch (Exception ex) {
                LOG.log(Level.FINE, "unable to convert url to file : " + repository, ex);
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
            File to = new File(cacheFolder, path);
            String urlPath = repository;
            if (!urlPath.endsWith("/")) {
                urlPath += "/";
            }
            urlPath += path;
            long start = System.currentTimeMillis();
            try {
                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "load  {0}", new Object[]{urlPath});
                PrivateNutsUtilIO.copy(new URL(urlPath), to, LOG);
                errorList.removeErrorsFor(nutsId);
                long end = System.currentTimeMillis();
                LOG.log(Level.CONFIG, NutsLogVerb.SUCCESS, "load   {0} ({1}ms)", new Object[]{urlPath, end - start});
                ok = to;
            } catch (IOException ex) {
                errorList.add(new PrivateNutsErrorInfo(nutsId, repository, urlPath, "unable to load", ex));
                long end = System.currentTimeMillis();
                LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "load   {0} ({1}ms)", new Object[]{urlPath, end - start});
                //not found
            }
            return ok;
        } else {
            repository = repositoryFolder.getPath();
        }
        File repoFolder = PrivateNutsUtilIO.createFile(PrivateNutsUtils.getHome(NutsStoreLocation.CONFIG, workspaceInformation), repository);
        File ff = null;

        if (repoFolder.isDirectory()) {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            if (file.isFile()) {
                ff = file;
            } else {
                LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "locate {0}", new Object[]{file});
            }
        } else {
            File file = new File(repoFolder, path.replace('/', File.separatorChar));
            LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "locate {0} ; repository is not a valid folder : {1}", new Object[]{file, repoFolder});
        }

        if (ff != null) {
            if (cacheFolder != null && cacheLocalFiles) {
                File to = new File(cacheFolder, path);
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
                        PrivateNutsUtilIO.copy(ff, to, LOG);
                        LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "recover cached " + ext + " file {0} to {1}", new Object[]{ff, to});
                    } else {
                        PrivateNutsUtilIO.copy(ff, to, LOG);
                        LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "cache " + ext + " file {0} to {1}", new Object[]{ff, to});
                    }
                    return to;
                } catch (IOException ex) {
                    errorList.add(new PrivateNutsErrorInfo(nutsId, repository, ff.getPath(), "unable to cache", ex));
                    LOG.log(Level.CONFIG, NutsLogVerb.FAIL, "error caching file {0} to {1} : {2}", new Object[]{ff, to, ex.toString()});
                    //not found
                }
                return ff;

            }
            return ff;
        }
        return null;
    }

    public static String resolveNutsApiVersionFromClassPath(PrivateNutsLog LOG) {
        return resolveNutsApiPomPattern("version", LOG);
    }

    public static String resolveNutsApiPomPattern(String propName, PrivateNutsLog LOG) {
//        boolean devMode = false;
        String propValue = null;
        try {
            switch (propName) {
                case "groupId":
                case "artifactId":
                case "versionId": {
                    propValue = PrivateNutsUtilIO.loadURLProperties(
                            Nuts.class.getResource("/META-INF/maven/net.thevpc.nuts/nuts/pom.properties"),
                            null, false, LOG).getProperty(propName);
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
            try (InputStream is = pomXml.openStream()) {
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

    private static class SimpleTomcatDirectoryListParser {

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
