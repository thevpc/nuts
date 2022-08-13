package net.thevpc.nuts.runtime.standalone.xtra.idresolver;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.api.NutsPomId;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.NutsPomXmlParser;
import net.thevpc.nuts.runtime.standalone.repository.impl.maven.pom.URLParts;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.util.NutsLoggerOp;
import net.thevpc.nuts.util.NutsLoggerVerb;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PomIdResolver2 {

    //    private NutsWorkspace ws;
    private NutsSession session;

    public PomIdResolver2(NutsSession session) {
        this.session = session;
    }

    public NutsId[] resolvePomId(NutsPath baseUrl, String referenceResourcePath, NutsSession session) {
        List<NutsId> all = new ArrayList<>();
        final URLParts aa = new URLParts(baseUrl.toURL());
        String basePath = aa.getLastPart().getPath().substring(0, aa.getLastPart().getPath().length() - referenceResourcePath.length());
        if (!basePath.endsWith("/")) {
            basePath += "/";
        }

        final URLParts p = aa.getParent().append(basePath + "META-INF/nuts");
        int beforeSize = all.size();
        URL[] children = new URL[0];
        try {
            children = p.getChildren(false, true, new MyURLFilter());
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (URL url : children) {
            if (url != null) {
                try {
                    NutsDescriptor d = NutsDescriptorParser.of(session).parse(url).get(session);
                    NutsId id = d.getId();
                    if(id!=null && id.getVersion()!=null && !id.getVersion().isBlank()){
                        all.add(id);
                    }
                } catch (Exception ex) {
                    NutsLoggerOp.of(NutsPomXmlParser.class,session)
                            .verb(NutsLoggerVerb.WARNING)
                            .level(Level.FINEST)
                            .log(NutsMessage.ofCstyle("failed to parse pom file %s : %s", url, ex));
                }
            }
        }
        if (beforeSize == all.size()) {
            //no found !
            if (basePath.endsWith("/target/classes/")) {
                String s2 = basePath.substring(0, basePath.length() - "/target/classes/".length()) + "/pom.xml";
                //this is most likely to be a maven project

            }
        }
        return all.toArray(new NutsId[0]);
    }

    /**
     * resolve all Maven/Nuts artifact definitions in the classloader that has
     * loaded <code>clazz</code>
     *
     * @param clazz class
     * @return artifacts array in the form groupId:artfcatId#version
     */
    public NutsId[] resolvePomIds(Class clazz) {
        List<NutsId> all = new ArrayList<>();
        try {
            final String n = clazz.getName().replace('.', '/').concat(".class");
            final Enumeration<URL> r = clazz.getClassLoader().getResources(n);
            for (URL url : Collections.list(r)) {
                all.addAll(Arrays.asList(resolvePomId(
                        NutsPath.of(url,session), n, session)));
            }
        } catch (IOException ex) {
            NutsLoggerOp.of(NutsPomXmlParser.class,session)
                    .verb(NutsLoggerVerb.WARNING)
                    .level(Level.FINEST)
                    .log(NutsMessage.ofCstyle("failed to parse class %s : %s", clazz.getName(), ex));
        }
        if(all.isEmpty() && JavaClassUtils.isCGLib(clazz)){
            Class s = JavaClassUtils.unwrapCGLib(clazz);
            if(s!=null){
                return resolvePomIds(s);
            }
        }
        return all.toArray(new NutsId[0]);
    }


    public NutsPomId resolvePomId(Class clazz, String groupId, String artifactId, String defaultValue) {
        String ver = resolvePomVersion(clazz, groupId, artifactId, defaultValue);
        return new NutsPomId(
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

    public NutsPomId resolvePropertiesPomId(InputStream stream) {
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
            return new NutsPomId(groupId, artifactId, version);
        }
        return null;
    }

    public NutsPomId[] resolveJarPomIds(InputStream jarStream) throws IOException {
        final List<NutsPomId> list = new ArrayList<>();
        visitZipStream(jarStream, new InputStreamVisitor() {
            @Override
            public boolean visit(String path, InputStream inputStream) {
                if (path.startsWith("META-INF/")
                        && path.endsWith("/pom.properties")) {

                    NutsPomId id = resolvePropertiesPomId(inputStream);
                    if (id != null) {
                        list.add(new NutsPomId(
                                id.getGroupId(),
                                id.getArtifactId(),
                                id.getVersion()
                        ));
                    }
                }
                return true;
            }
        });
        return list.toArray(new NutsPomId[0]);
    }

    public NutsPomId resolveJarPomId(InputStream jarStream) throws IOException {
        NutsPomId[] v = resolveJarPomIds(jarStream);
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

    private static class MyURLFilter implements Predicate<URL> {

        public MyURLFilter() {
        }

        @Override
        public boolean test(URL path) {
            return new URLParts(path).getName().equals("nuts.json");
        }
    }

}
