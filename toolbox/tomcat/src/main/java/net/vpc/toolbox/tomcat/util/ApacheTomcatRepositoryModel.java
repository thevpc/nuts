package net.vpc.toolbox.tomcat.util;

import net.vpc.app.nuts.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class ApacheTomcatRepositoryModel implements NutsRepositoryModel {
    private static Logger LOG = Logger.getLogger(ApacheTomcatRepositoryModel.class.getName());
    public static final String HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT = "https://archive.apache.org/dist/tomcat/";

    public ApacheTomcatRepositoryModel() {
    }

    @Override
    public String getName() {
        return "apache-tomcat";
    }

    @Override
    public String getUuid() {
        return UUID.nameUUIDFromBytes(getName().getBytes()).toString();
    }

    @Override
    public Iterator<NutsId> search(NutsIdFilter filter, String[] roots, NutsRepositorySession session) {
        if(session.getFetchMode()!=NutsFetchMode.REMOTE){
            return null;
        }
        List<NutsId> all = new ArrayList<>();
        NutsWorkspace ws = session.getWorkspace();
        NutsIdBuilder idBuilder = ws.id().builder().groupId("org.apache.catalina").artifactId("apache-tomcat");
        for (String s : list(HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT, "tomcat-[0-9.]+/", session)) {
            for (String s2 : list(HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT + s, "v.+/", session)) {
                String prefix = "apache-tomcat-";
                String bin = "bin";
                if (
                        s2.endsWith("-alpha/") || s2.endsWith("-beta/")
                        || s2.endsWith("-copyforpermissions/") || s2.endsWith("-original/")
                        || s2.matches(".*-RC[0-9]+/") || s2.matches(".*-M[0-9]+/")
                ) {
                    //will ignore all alpha versions
                    continue;
                }
                NutsVersion version = ws.version().parse(s2.substring(1, s2.length() - 1));
                if (version.compareTo("4.1.32") < 0) {
                    prefix = "jakarta-tomcat-";
                }
                if (version.compareTo("4.1.27")==0){
                    bin = "binaries";
                }
                boolean checkBin=false;
                if(checkBin) {
                    for (String s3 : list(HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT + s + s2 + bin,
                            prefix + "[0-9]+\\.[0-9]+\\.[0-9]+\\.zip",
                            session)) {
                        String v0 = s3.substring(prefix.length(), s3.length() - 4);
                        NutsVersion v = ws.version().parse(v0);
                        NutsId id2 = idBuilder.setVersion(v).build();
                        if (filter == null || filter.accept(id2, session.getSession())) {
                            all.add(id2);
                        }
                    }
                }else{
                    NutsId id2 = idBuilder.setVersion(version).build();
                    if (filter == null || filter.accept(id2, session.getSession())) {
                        all.add(id2);
                    }
                }
            }

        }
        return all.iterator();
    }

    @Override
    public Iterator<NutsId> searchVersions(NutsId id, NutsIdFilter filter, NutsRepositorySession session) {
        if(session.getFetchMode()!=NutsFetchMode.REMOTE){
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            return search(filter, new String[]{"/"}, session);
        }
        return null;
    }

    private String getUrl(NutsVersion version, String extension) {
        String bin = "bin";
        String prefix = "apache-tomcat-";
        if (version.compareTo("4.1.32") < 0) {
            prefix = "jakarta-tomcat-";
        }
        if (version.compareTo("4.1.27")==0) {
            bin = "binaries";
        }
        return HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT + "tomcat-" + version.get(0) + "/v" + version + "/" + bin + "/" + prefix + version + extension;
    }

    @Override
    public NutsDescriptor fetchDescriptor(NutsId id, NutsRepositorySession session) {
        if(session.getFetchMode()!=NutsFetchMode.REMOTE){
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            NutsWorkspace ws = session.getWorkspace();
//            String r = getUrl(id.getVersion(), ".zip.md5");
            String r = getUrl(id.getVersion(), ".zip");
            boolean found = false;
            try (InputStream inputStream= new URL(r).openStream()){
                //ws.io().copy().from(r).getByteArrayResult();
                found = true;
            } catch (Exception ex) {
                found = false;
            }
            if (found) {
                return ws.descriptor()
                        .descriptorBuilder()
                        .id(id.getLongNameId())
                        .packaging("zip")
                        .platform(new String[]{"java"})
                        .description("Apache Tomcat Official Zip Bundle")
                        .setProperty("dynamic-descriptor", "true")
                        .build();
            }
        }
        return null;
    }

    @Override
    public NutsContent fetchContent(NutsId id, NutsDescriptor descriptor, Path localPath, NutsRepositorySession session) {
        if(session.getFetchMode()!=NutsFetchMode.REMOTE){
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            NutsWorkspace ws = session.getWorkspace();
            String r = getUrl(id.getVersion(), ".zip");
            if (localPath == null) {
                localPath = getIdLocalFile(id.builder().faceContent().build(), session);
            }
            ws.io().copy().from(r).to(localPath).safe(true).run();
            return new NutsDefaultContent(localPath, false, false);
        }
        return null;
    }

    public Path getIdLocalFile(NutsId id, NutsRepositorySession session) {
        NutsWorkspace ws = session.getWorkspace();
        return session.getRepository().config().getStoreLocation().resolve(
                ws.config().getDefaultIdBasedir(id)
        ).resolve(ws.config().getDefaultIdFilename(id));
    }

    private String[] list(String url, String regexp, NutsRepositorySession session) {
        Pattern filterPattern = regexp == null ? null : Pattern.compile(regexp);
        List<String> all = new ArrayList<>();
        try {
            //NutsWorkspace ws = session.getSession().getWorkspace();
            NutsWorkspace ws = session.getWorkspace();
            byte[] bytes = ws.io().copy().from(url).logProgress().getByteArrayResult();
            Document doc = null;
            try {
                doc = Jsoup.parse(new ByteArrayInputStream(bytes), "UTF-8", url);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            Elements newsHeadlines = doc.getElementsByTag("pre");
            for (Element headline : newsHeadlines) {
                String last = null;
                for (Element child : headline.children()) {
                    if (child instanceof Element) {
                        switch (child.tagName()) {
                            case "img": {
                                last = "img";
                                break;
                            }
                            case "a": {
                                if ("img".equals(last)) {
                                    String href = child.attr("href");
                                    if (href != null && !href.startsWith("/") && !href.startsWith("?")) {
                                        if (filterPattern == null || filterPattern.matcher(href).matches()) {
                                            all.add(href);
                                        }
                                    }
                                }
                                last = "a";
                                break;
                            }
                        }
                    }
                }
            }
        } catch (UncheckedIOException io) {
            LOG.log(Level.FINER, "Inaccessible url " + url + " (" + io.getCause().toString());
        }
        return all.toArray(new String[0]);
    }
}
