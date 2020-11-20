package net.thevpc.nuts.runtime.bridges.maven.mvnutil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import net.thevpc.nuts.NutsIllegalArgumentException;
import net.thevpc.nuts.NutsLogger;
import net.thevpc.nuts.NutsWorkspace;

public class PomIdResolver {
    private final NutsLogger LOG;
    private NutsWorkspace ws;

    public static PomIdResolver of(NutsWorkspace ws) {
        Map<String, Object> up = ws.userProperties();
        PomIdResolver wp = (PomIdResolver) up.get(PomIdResolver.class.getName());
        if (wp == null) {
            wp = new PomIdResolver(ws);
            up.put(PomIdResolver.class.getName(), wp);
        }
        return wp;
    }

    private PomIdResolver(NutsWorkspace ws) {
        this.ws = ws;
        LOG=ws.log().of(PomIdResolver.class);
    }

    public PomId[] resolvePomId(URL baseUrl, String referenceResourcePath) {
        List<PomId> all = new ArrayList<PomId>();
        final URLParts aa = new URLParts(baseUrl);
        String basePath = aa.getLastPart().getPath().substring(0, aa.getLastPart().getPath().length() - referenceResourcePath.length());
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        final URLParts p = aa.getParent().append(basePath + "META-INF/maven");
        int beforeSize = all.size();
        URL[] children = new URL[0];
        try {
            children = p.getChildren(false, true, new MyURLFilter());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (URL url : children) {
            if (url != null) {
                Properties prop = new Properties();
                try {
                    prop.load(url.openStream());
                } catch (IOException e) {
                    //
                }
                String version = prop.getProperty("version");
                String groupId = prop.getProperty("groupId");
                String artifactId = prop.getProperty("artifactId");
                if (version != null && version.trim().length() != 0) {
                    all.add(new PomId(groupId, artifactId, version));
                }
            }
        }
        if (beforeSize == all.size()) {
            //no found !
            if (basePath.endsWith("/target/classes/")) {
                String s2 = basePath.substring(0, basePath.length() - "/target/classes/".length()) + "/pom.xml";
                //this is most likely to be a maven project
                try {
                    all.add(new PomXmlParser(ws).parse(new URL(s2)).getPomId());
                } catch (Exception ex) {
                    LOG.with().level(Level.SEVERE).error(ex).log("Failed to parse pom file {0} : {1}", s2,ex.toString());
                }
            }
        }
        return all.toArray(new PomId[0]);
    }

    /**
     * resolve all Maven/Nuts artifact definitions in the classloader that has
     * loaded <code>clazz</code>
     *
     * @param clazz class
     * @return artifacts array in the form groupId:artfcatId#version
     */
    public PomId[] resolvePomIds(Class clazz) {
        List<PomId> all = new ArrayList<PomId>();
        try {
            final String n = clazz.getName().replace('.', '/').concat(".class");
            final Enumeration<URL> r = clazz.getClassLoader().getResources(n);
            for (URL url : Collections.list(r)) {
                all.addAll(Arrays.asList(resolvePomId(url, n)));
            }
        } catch (IOException ex) {
            LOG.with().level(Level.SEVERE).error(ex).log("Error : {0}",ex.toString());
        }
        return all.toArray(new PomId[0]);
    }

    public PomId resolvePomId(Class clazz) {
        return resolvePomId(clazz, new PomId("dev", "dev", "dev"));
    }

    public PomId resolvePomId(Class clazz, PomId defaultValue) {
        PomId[] pomIds = resolvePomIds(clazz);
//        if(pomIds.length>1){
//            System.out.println("==== Multiple ids found : "+Arrays.asList(pomIds));
//        }else{
//            System.out.println("==== Single id found : "+Arrays.asList(pomIds));
//        }
//        System.out.println(clazz.getClassLoader());
//        if(clazz.getClassLoader() instanceof URLClassLoader){
//            URLClassLoader u=(URLClassLoader)clazz.getClassLoader();
//            for (URL url : u.getURLs()) {
//                System.out.println("\t"+url);
//            }
//        }
//        new Throwable().printStackTrace(System.out);
        for (PomId v : pomIds) {
            return v;
        }
        return defaultValue;
    }

    public PomId resolvePomId(Class clazz, String groupId, String artifactId, String defaultValue) {
        String ver = resolvePomVersion(clazz, groupId, artifactId, defaultValue);
        return new PomId(
                groupId, artifactId, ver
        );
    }

    public String resolvePomVersion(String groupId, String artifactId, String defaultValue) {
        URL url = Thread.currentThread().getContextClassLoader().getResource("META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");

        if (url != null) {
            Properties p = new Properties();
            try {
                p.load(url.openStream());
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
//            System.out.println("== " + url);
            Properties p = new Properties();
            try {
                p.load(url.openStream());
            } catch (IOException e) {
                //
            }
            String version = p.getProperty("version");
            if (version != null && version.trim().length() != 0) {
//                System.out.println("\t found!!");
                return version;
            }
//            System.out.println("\t not found");
        }
        return defaultValue;
    }

    public PomId resolvePropertiesPomId(InputStream stream) {
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
            return new PomId(groupId, artifactId, version);
        }
        return null;
    }

    private static class MyURLFilter implements URLFilter {

        @Override
        public boolean accept(URL path) {
            return new URLParts(path).getName().equals("pom.properties");
        }
    }

    public PomId[] resolveJarPomIds(InputStream jarStream) throws IOException {
        final List<PomId> list = new ArrayList<>();
        visitZipStream(jarStream, new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) {
                if (path.startsWith("META-INF/")
                        && path.endsWith("/pom.properties")) {

                    PomId id = resolvePropertiesPomId(inputStream);
                    if (id != null) {
                        list.add(new PomId(
                                id.getGroupId(),
                                id.getArtifactId(),
                                id.getVersion()
                        ));
                    }
                }
                return true;
            }
        });
        return list.toArray(new PomId[0]);
    }

    public PomId resolveJarPomId(InputStream jarStream) throws IOException {
        PomId[] v = resolveJarPomIds(jarStream);
        if (v.length == 0) {
            return null;
        }
        if (v.length >= 2) {
            throw new NutsIllegalArgumentException(null, "Too many Ids");
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

}
