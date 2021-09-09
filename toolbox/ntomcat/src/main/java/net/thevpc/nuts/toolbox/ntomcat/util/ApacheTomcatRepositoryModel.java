package net.thevpc.nuts.toolbox.ntomcat.util;

import net.thevpc.nuts.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
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
    public Iterator<NutsId> search(NutsIdFilter filter, String[] roots, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if(fetchMode!=NutsFetchMode.REMOTE){
            return null;
        }
        List<NutsId> all = new ArrayList<>();
        NutsWorkspace ws = session.getWorkspace();
        NutsIdBuilder idBuilder = ws.id().builder().setGroupId("org.apache.catalina").setArtifactId("apache-tomcat");
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
                NutsVersion version = ws.version().parser().parse(s2.substring(1, s2.length() - 1));
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
                        NutsVersion v = ws.version().parser().parse(v0);
                        NutsId id2 = idBuilder.setVersion(v).build();
                        if (filter == null || filter.acceptId(id2, session)) {
                            all.add(id2);
                        }
                    }
                }else{
                    NutsId id2 = idBuilder.setVersion(version).build();
                    if (filter == null || filter.acceptId(id2, session)) {
                        all.add(id2);
                    }
                }
            }

        }
        return all.iterator();
    }

    @Override
    public Iterator<NutsId> searchVersions(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if(fetchMode!=NutsFetchMode.REMOTE){
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            return search(filter, new String[]{"/"}, fetchMode, repository, session);
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
    public NutsDescriptor fetchDescriptor(NutsId id, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if(fetchMode!=NutsFetchMode.REMOTE){
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {

            NutsWorkspace ws = session.getWorkspace();
//            String r = getUrl(id.getVersion(), ".zip.md5");
            String r = getUrl(id.getVersion(), ".zip");
            URL url = null;
            boolean found = false;
            try {
                url = new URL(r);
            } catch (MalformedURLException e) {

            }
            if(url!=null) {
                session.getTerminal().printProgress(NutsMessage.cstyle("peek %s", url));
                try (InputStream inputStream = url.openStream()) {
                    //ws.io().copy().from(r).getByteArrayResult();
                    found = true;
                } catch (Exception ex) {
                    found = false;
                }
            }
            if (found) {
                // http://tomcat.apache.org/whichversion.html
                int i = id.getVersion().getInt(0,-1);
                int j = id.getVersion().getInt(1,-1);
                String javaVersion="";
                if (i<=0){
                     //
                }else if(i<=3){
                    javaVersion="#1.1";
                }else if(i<=4){
                    javaVersion="#1.3";
                }else if(i<=5){
                    javaVersion="#1.4";
                }else if(i<=6){
                    javaVersion="#1.5";
                }else if(i<=7){
                    javaVersion="#1.6";
                }else if(i<=8){
                    javaVersion="#1.7";
                }else if(i<=9){
                    javaVersion="#1.8";
                }else /*if(i<=10)*/{
                    if(j<=0) {
                        javaVersion="#1.8";
                    }else{
                        javaVersion = "#11";
                    }
                }

                return ws.descriptor()
                        .descriptorBuilder()
                        .setId(id.getLongNameId())
                        .setPackaging("zip")
                        .setCondition(
                                NutsEnvConditionBuilder.of(session)
                                        .setPlatform("java"+javaVersion)
                        )
                        .setDescription("Apache Tomcat Official Zip Bundle")
                        .setProperty("dynamic-descriptor", "true")
                        .build();
            }
        }
        return null;
    }

    @Override
    public NutsContent fetchContent(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if(fetchMode!=NutsFetchMode.REMOTE){
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            NutsWorkspace ws = session.getWorkspace();
            String r = getUrl(id.getVersion(), ".zip");
            if (localPath == null) {
                localPath = getIdLocalFile(id.builder().setFaceContent().build(), fetchMode, repository, session);
            }
            ws.io().copy().from(r).to(localPath).setSafe(true).setSession(session).setLogProgress(true).run();
            return new NutsDefaultContent(
                    session.getWorkspace().io().path(localPath), false, false);
        }
        return null;
    }

    public String getIdLocalFile(NutsId id, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        NutsWorkspace ws = session.getWorkspace();
        return Paths.get(repository.config().getStoreLocation())
                .resolve(
                ws.locations().getDefaultIdBasedir(id)
        ).resolve(ws.locations().getDefaultIdFilename(id)).toString();
    }

    private String[] list(String url, String regexp, NutsSession session) {
        Pattern filterPattern = regexp == null ? null : Pattern.compile(regexp);
        List<String> all = new ArrayList<>();
        try {
            //NutsWorkspace ws = session.getWorkspace();
            NutsWorkspace ws = session.getWorkspace();
            byte[] bytes = ws.io().copy().from(url).setLogProgress(true).getByteArrayResult();
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
        } catch (UncheckedIOException|NutsIOException io) {
            LOG.log(Level.FINER, "inaccessible url " + url + " (" + io.getCause().toString());
        }
        return all.toArray(new String[0]);
    }
}
