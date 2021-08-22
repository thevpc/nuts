package net.thevpc.nuts;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;

/**
 * @app.category Internal
 */
public final class PrivateNutsMavenUtils {
    public PrivateNutsMavenUtils() {
    }

    public static String toMavenFileName(String nutsId, String extension) {
        String[] arr = nutsId.split("[:#]");
        return arr[1]
                + "-"
                + arr[2]
                + "."
                + extension;
    }

    public static String toMavenPath(String nutsId) {
        String[] arr = nutsId.split("[:#]");
        StringBuilder sb = new StringBuilder();
        sb.append(arr[0].replace(".", "/"));
        sb.append("/");
        sb.append(arr[1]);
        if (arr.length > 2) {
            sb.append("/");
            sb.append(arr[2]);
        }
        return sb.toString();
    }

    //        public static String resolveMavenReleaseVersion(String mavenURLBase, String nutsId) {
//            String mvnUrl = (mavenURLBase + toMavenPath(nutsId) + "/maven-metadata.xml");
//            String str = null;
//            try {
//                str = PrivateNutsUtils.readStringFromURL(new URL(mvnUrl));
//            } catch (IOException e) {
//                throw new UncheckedIOException(e);
//            }
//            if (str != null) {
//                for (String line : str.split("\n")) {
//                    line = line.trim();
//                    if (line.startsWith("<release>")) {
//                        return line.substring("<release>".length(), line.length() - "</release>".length()).trim();
//                    }
//                }
//            }
//            throw new NutsNotFoundException(null, nutsId);
//        }
    public static String resolveMavenFullPath(String repo, String nutsId, String ext) {
        String jarPath = toMavenPath(nutsId) + "/" + toMavenFileName(nutsId, ext);
        String mvnUrl = repo;
        String sep = "/";
        if (!PrivateNutsUtils.isURL(repo)) {
            sep = File.separator;
        }
        if (!mvnUrl.endsWith("/") && !mvnUrl.endsWith(sep)) {
            mvnUrl += sep;
        }
        return mvnUrl + jarPath;
    }

    public static String getFileName(NutsBootId id, String ext) {
        return id.getArtifactId() + "-" + id.getVersion() + "." + ext;
    }

    public static String getPathFile(NutsBootId id, String name) {
        return id.getGroupId().replace('.', '/') + '/' + id.getArtifactId() + '/' + id.getVersion() + "/" + name;
    }

    public static File resolveOrDownloadJar(String nutsId, String[] repositories, String cacheFolder, PrivateNutsLog LOG, boolean includeDesc, Instant expire, PrivateNutsUtils.ErrorInfoList errors) {
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
                    PrivateNutsUtils.copy(new URL(path), cachedPomFile, LOG);
                } catch (Exception ex) {
                    errors.add(new PrivateNutsUtils.ErrorInfo(nutsId, r, path, "unable to load descriptor", ex.toString()));
                    LOG.log(Level.SEVERE, NutsLogVerb.FAIL, "unable to load descriptor {0} from {1}.\n", new Object[]{nutsId, r});
                    continue;
                }
            }
            String path = resolveMavenFullPath(r, nutsId, "jar");
            try {
                PrivateNutsUtils.copy(new URL(path), cachedJarFile, LOG);
                LOG.log(Level.CONFIG, NutsLogVerb.CACHE, "cache jar file {0}", new Object[]{cachedJarFile.getPath()});
                errors.removeErrorsFor(nutsId);
                return cachedJarFile;
            } catch (Exception ex) {
                errors.add(new PrivateNutsUtils.ErrorInfo(nutsId, r, path, "unable to load binaries", ex.toString()));
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
                File file = PrivateNutsUtils.toFile(url1);
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
                            if (NutsUtilStrings.isBlank(groupId)) {
                                throw new NutsBootException(NutsMessage.plain("unexpected empty groupId"));
                            } else if (groupId.contains("$")) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected maven variable in groupId=%s", groupId));
                            }
                            if (NutsUtilStrings.isBlank(artifactId)) {
                                throw new NutsBootException(NutsMessage.plain("unexpected empty artifactId"));
                            } else if (artifactId.contains("$")) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected maven variable in artifactId=%s", artifactId));
                            }
                            if (NutsUtilStrings.isBlank(version)) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected empty artifactId"));
                            } else if (version.contains("$")) {
                                throw new NutsBootException(NutsMessage.cstyle("unexpected maven variable in artifactId=%s", version));
                            }
                            //this is maven dependency, using "compile"
                            if (NutsUtilStrings.isBlank(scope) || scope.equals("compile")) {
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
                                                PrivateNutsUtils.split(t, ";", true)
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

    /**
     * find latest maven artifact
     *
     * @param filter filter
     * @return latest runtime version
     */
    static NutsBootId resolveLatestMavenId(NutsBootId zId, Predicate<String> filter, PrivateNutsLog LOG, Collection<String> bootRepositories) {
        LOG.log(Level.FINEST, NutsLogVerb.START, "looking for {0}", zId);
        String path = zId.getGroupId().replace('.', '/') + '/' + zId.getArtifactId();
        String bestVersion = null;
        String bestPath = null;
        boolean stopOnFirstValidRepo = false;
        for (String repoUrl : bootRepositories) {
            boolean found = false;
            if (!repoUrl.contains("://")) {
                File mavenNutsCoreFolder = new File(repoUrl, path + "/".replace("/", File.separator));
                if (mavenNutsCoreFolder.isDirectory()) {
                    File[] children = mavenNutsCoreFolder.listFiles();
                    if (children != null) {
                        for (File file : children) {
                            if (file.isDirectory()) {
                                String[] goodChildren = file.list((dir, name) -> name.endsWith(".pom"));
                                if (goodChildren != null && goodChildren.length > 0) {
                                    String p = file.getName();
                                    if (filter == null || filter.test(p)) {
                                        found = true;
                                        if (bestVersion == null || NutsBootVersion.parse(bestVersion).compareTo(NutsBootVersion.parse(p)) < 0) {
                                            bestVersion = p;
                                            bestPath = "local location : " + file.getPath();
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
                String mavenMetadata = repoUrl + path + "/maven-metadata.xml";
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
                                                String p = c4.getTextContent();
                                                if (filter == null || filter.test(p)) {
                                                    found = true;
                                                    if (bestVersion == null || NutsBootVersion.parse(bestVersion).compareTo(NutsBootVersion.parse(p)) < 0) {
                                                        bestVersion = p;
                                                        bestPath = "remote file " + mavenMetadata;
                                                    }
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
            }
            if (stopOnFirstValidRepo && found) {
                break;
            }
        }
        if (bestVersion == null) {
            return null;
        }
        NutsBootId iid = new NutsBootId(zId.getGroupId(), zId.getArtifactId(), NutsBootVersion.parse(bestVersion), false, null, null);
        LOG.log(Level.FINEST, NutsLogVerb.SUCCESS, "resolved " + iid + " from " + bestPath);
        return iid;
    }
}
