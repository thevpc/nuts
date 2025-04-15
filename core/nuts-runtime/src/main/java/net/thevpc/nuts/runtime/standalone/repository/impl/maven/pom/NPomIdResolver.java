package net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom;

import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.io.urlpart.URLPart;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NPomId;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.util.NMsg;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class NPomIdResolver {

    //    private NutsWorkspace ws;
    public NPomIdResolver() {
    }

    public NPomId[] resolvePomIds(NPath baseUrl) {
        return resolvePomIds(baseUrl, null);
    }

    private NPomId[] resolvePomIdsFromTargetClasses(URLPart aa, String referenceResourcePath) {
        String basePath = aa.getPath().substring(0, aa.getPath().length() - (referenceResourcePath == null ? 0 : referenceResourcePath.length()));
        if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
            basePath += "/";
        }

        List<NPomId> all = new ArrayList<NPomId>();
        if (basePath.matches(".*[/\\\\]target[/\\\\]classes[/\\\\]")) {
            String s2 = basePath.substring(0, basePath.length() - "/target/classes".length()) + "pom.xml";
            //this is most likely to be a maven project
            try {
                all.add(new NPomXmlParser().parse(NPath.of(s2).toURL().get()).getPomId());
            } catch (Exception ex) {
                NLogOp.of(NPomXmlParser.class)
                        .verb(NLogVerb.WARNING)
                        .level(Level.FINEST)
                        .log(NMsg.ofC("failed to parse pom file %s : %s", s2, ex));
            }
        }
        return all.toArray(new NPomId[0]);
    }


    private NPomId[] resolvePomIdsFromMetaInfMavenByRef(URLPart aa, String referenceResourcePath) {
        String basePath = aa.getPath().substring(0, aa.getPath().length() - (referenceResourcePath == null ? 0 : referenceResourcePath.length()));
        if (!basePath.endsWith("/") && !basePath.endsWith("\\")) {
            basePath += "/";
        }
        final URLPart p = aa.rootSibling(basePath +"META-INF/maven");
        return resolvePomIdsFromMetaInfMaven0(p);
    }

    private NPomId[] resolvePomIdsFromMetaInfMaven0(URLPart p) {
        List<NPomId> all = new ArrayList<NPomId>();
        URLPart[] children = p.getChildren(false, true, new MvnPomPropsURLFilter());
        for (URLPart url : children) {
            if (url != null) {
                Properties prop = new Properties();
                try (InputStream is=url.getInputStream()){
                    prop.load(is);
                } catch (IOException e) {
                    //
                }
                String version = prop.getProperty("version");
                String groupId = prop.getProperty("groupId");
                String artifactId = prop.getProperty("artifactId");
                if (version != null && version.trim().length() != 0) {
                    all.add(new NPomId(groupId, artifactId, version));
                }
            }
        }
        return all.toArray(new NPomId[0]);
    }

    private NPomId[] resolvePomIdsFromMetaInfMavenAsRoot(URLPart aa) {
//        if (aa.len() == 2
//                && aa.parent().getType() == URLPart.Type.JAR
//                && aa.getType() == URLPart.Type.FS_FILE
//        ) {
            URLPart p = aa.rootSibling("/META-INF/maven");
            return resolvePomIdsFromMetaInfMaven0(p);
//        }
//        return new NPomId[0];
    }

    public NPomId[] resolvePomIds(NPath baseUrl, String referenceResourcePath) {
        final URLPart aa = URLPart.of(baseUrl.toURL().get());
        NPomId[] result;
        //List<URL> pomProperties = NCollections.list(Thread.currentThread().getContextClassLoader().getResources("META-INF/maven./pom.properties"));
        result = resolvePomIdsFromMetaInfMavenByRef(aa, referenceResourcePath);
        if (result.length > 0) {
            return result;
        }
        result = resolvePomIdsFromMetaInfMavenAsRoot(aa);
        if (result.length > 0) {
            return result;
        }
        result = resolvePomIdsFromTargetClasses(aa, referenceResourcePath);
        if (result.length > 0) {
            return result;
        }
        return result;
    }

    /**
     * resolve all Maven/Nuts artifact definitions in the classloader that has
     * loaded <code>clazz</code>
     *
     * @param clazz class
     * @return artifacts array in the form groupId:artifactId#version
     */
    public NPomId[] resolvePomIds(Class clazz) {
        List<NPomId> all = new ArrayList<NPomId>();
        try {
            final String n = clazz.getName().replace('.', '/').concat(".class");
            final Enumeration<URL> r = clazz.getClassLoader().getResources(n);
            ArrayList<URL> list = Collections.list(r);
            for (URL url : list) {
                all.addAll(Arrays.asList(resolvePomIds(NPath.of(url), n)));
            }
        } catch (IOException ex) {
            NLogOp.of(NPomXmlParser.class)
                    .verb(NLogVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NMsg.ofC("failed to parse class %s : %s", clazz.getName(), ex));
        }
        if (all.isEmpty() && JavaClassUtils.isCGLib(clazz)) {
            Class s = JavaClassUtils.unwrapCGLib(clazz);
            if (s != null) {
                return resolvePomIds(s);
            }
        }
        return all.toArray(new NPomId[0]);
    }

    public NPomId resolvePomId(Class clazz) {
        return resolvePomId(clazz, new NPomId("dev", "dev", "dev"));
    }

    public NPomId resolvePomId(Class clazz, NPomId defaultValue) {
        NPomId[] pomIds = resolvePomIds(clazz);
        if (pomIds.length > 1) {
            NLogOp.of(NPomXmlParser.class)
                    .verb(NLogVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NMsg.ofC(
                            "multiple ids found : %s for class %s and id %s",
                            Arrays.asList(pomIds), clazz, defaultValue
                    ));
        }
        for (NPomId v : pomIds) {
            return v;
        }
        return defaultValue;
    }

    public NPomId resolvePomId(Class clazz, String groupId, String artifactId, String defaultValue) {
        String ver = resolvePomVersion(clazz, groupId, artifactId, defaultValue);
        return new NPomId(
                groupId, artifactId, ver
        );
    }

    public String resolvePomVersion(String groupId, String artifactId, String defaultValue) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");

        if (url != null) {
            Properties p = new Properties();
            try {
                p.load(DefaultNWebCli.prepareGlobalOpenStream(url));
            } catch (IOException e) {
                //
            }
            String version = p.getProperty("version");
            if (version != null && version.trim().length() != 0) {
                return version;
            }
        }
        return defaultValue;
    }

    public String resolvePomVersion(Class clazz, String groupId, String artifactId, String defaultValue) {
        URL url = clazz.getClassLoader().getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");

        if (url != null) {
            Properties p = new Properties();
            try {
                p.load(DefaultNWebCli.prepareGlobalOpenStream(url));
            } catch (IOException e) {
                //
            }
            String version = p.getProperty("version");
            if (version != null && version.trim().length() != 0) {
                return version;
            }
        }
        return defaultValue;
    }

    public NPomId resolvePropertiesPomId(InputStream stream) {
        Properties prop = new Properties();
        try {
            prop.load(stream);
        } catch (IOException e) {
            //
        }
        String version = prop.getProperty("version");
        String groupId = prop.getProperty("groupId");
        String artifactId = prop.getProperty("artifactId");
        if (version != null && version.trim().length() != 0) {
            return new NPomId(groupId, artifactId, version);
        }
        return null;
    }

    public NPomId[] resolveJarPomIds(InputStream jarStream) throws IOException {
        final List<NPomId> list = new ArrayList<>();
        visitZipStream(jarStream, new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) {
                if (path.startsWith("META-INF/")
                        && path.endsWith("/pom.properties")) {

                    NPomId id = resolvePropertiesPomId(inputStream);
                    if (id != null) {
                        list.add(new NPomId(
                                id.getGroupId(),
                                id.getArtifactId(),
                                id.getVersion()
                        ));
                    }
                }
                return true;
            }
        });
        return list.toArray(new NPomId[0]);
    }

    public NPomId resolveJarPomId(InputStream jarStream) throws IOException {
        NPomId[] v = resolveJarPomIds(jarStream);
        if (v.length == 0) {
            return null;
        }
        if (v.length >= 2) {
            throw new IllegalArgumentException("too many Ids");
        }
        return v[0];
    }

    private boolean visitZipStream(InputStream zipFile, InputStreamVisitor visitor) throws IOException {
        //byte[] buffer = new byte[4 * 1024];

        //get the zip file content
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(zipFile);
            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();
            final ZipInputStream finalZis = zis;
            InputStream entryInputStream = new InputStream() {
                @Override
                public int read() throws IOException {
                    return finalZis.read();
                }

                @Override
                public int read(byte[] b) throws IOException {
                    return finalZis.read(b);
                }

                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    return finalZis.read(b, off, len);
                }

                @Override
                public void close() throws IOException {
                    finalZis.closeEntry();
                }
            };

            while (ze != null) {

                String fileName = ze.getName();
                if (!fileName.endsWith("/")) {
                    if (!visitor.visit(fileName, entryInputStream)) {
                        break;
                    }
                }
                ze = zis.getNextEntry();
            }
        } finally {
            if (zis != null) {
                zis.close();
            }
        }

        return false;
    }

    private interface InputStreamVisitor {

        boolean visit(String path, InputStream inputStream) throws IOException;
    }

    private static class MvnPomPropsURLFilter implements Predicate<URLPart> {

        public MvnPomPropsURLFilter() {
        }

        @Override
        public boolean test(URLPart path) {
            String name = path.getName();
            return name.equals("pom.properties");
        }
    }
}
