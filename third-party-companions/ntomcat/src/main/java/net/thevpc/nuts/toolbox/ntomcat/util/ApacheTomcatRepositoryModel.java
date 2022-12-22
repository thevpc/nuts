package net.thevpc.nuts.toolbox.ntomcat.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NutsCp;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.io.NutsPathOption;
import net.thevpc.nuts.util.NutsFunction;
import net.thevpc.nuts.util.NutsIterator;
import net.thevpc.nuts.util.NutsStream;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Logger;

public class ApacheTomcatRepositoryModel implements NutsRepositoryModel {
    public static final String HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT = "https://archive.apache.org/dist/tomcat/";
    private static final Logger LOG = Logger.getLogger(ApacheTomcatRepositoryModel.class.getName());

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
    public NutsIterator<NutsId> searchVersions(NutsId id, NutsIdFilter filter, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            return search(filter, new NutsPath[]{null}, fetchMode, repository, session);
        }
        return null;
    }

    @Override
    public NutsDescriptor fetchDescriptor(NutsId id, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {

//            String r = getUrl(id.getVersion(), ".zip.md5");
            String r = getUrl(id.getVersion(), ".zip");
            URL url = null;
            boolean found = false;
            try {
                url = new URL(r);
            } catch (MalformedURLException e) {

            }
            if (url != null) {
                session.getTerminal().printProgress(NutsMessage.ofCstyle("peek %s", url));
                try (InputStream inputStream = url.openStream()) {
                    //ws.io().copy().from(r).getByteArrayResult();
                    found = true;
                } catch (Exception ex) {
                    found = false;
                }
            }
            if (found) {
                // http://tomcat.apache.org/whichversion.html
                int i = id.getVersion().getNumber(0).flatMap(NutsValue::asInt).orElse(-1);
                int j = id.getVersion().getNumber(1).flatMap(NutsValue::asInt).orElse(-1);
                String javaVersion = "";
                if (i <= 0) {
                    //
                } else if (i <= 3) {
                    javaVersion = "#1.1";
                } else if (i <= 4) {
                    javaVersion = "#1.3";
                } else if (i <= 5) {
                    javaVersion = "#1.4";
                } else if (i <= 6) {
                    javaVersion = "#1.5";
                } else if (i <= 7) {
                    javaVersion = "#1.6";
                } else if (i <= 8) {
                    javaVersion = "#1.7";
                } else if (i <= 9) {
                    javaVersion = "#1.8";
                } else /*if(i<=10)*/ {
                    if (j <= 0) {
                        javaVersion = "#1.8";
                    } else {
                        javaVersion = "#11";
                    }
                }

                return new DefaultNutsDescriptorBuilder()
                        .setId(id.getLongId())
                        .setPackaging("zip")
                        .setCondition(
                                new DefaultNutsEnvConditionBuilder()
                                        .setPlatform(Arrays.asList("java" + javaVersion))
                        )
                        .setDescription("Apache Tomcat Official Zip Bundle")
                        .setProperty("dynamic-descriptor", "true")
                        .build();
            }
        }
        return null;
    }

    @Override
    public NutsPath fetchContent(NutsId id, NutsDescriptor descriptor, String localPath, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            String r = getUrl(id.getVersion(), ".zip");
            if (localPath == null) {
                localPath = getIdLocalFile(id.builder().setFaceContent().build(), fetchMode, repository, session);
            }
            NutsCp.of(session).from(NutsPath.of(r, session)).to(NutsPath.of(localPath, session))
                    .addOptions(NutsPathOption.SAFE, NutsPathOption.LOG, NutsPathOption.TRACE).run();
            return NutsPath.of(localPath, session);
        }
        return null;
    }

    @Override
    public NutsIterator<NutsId> search(NutsIdFilter filter, NutsPath[] basePaths, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        if (fetchMode != NutsFetchMode.REMOTE) {
            return NutsIterator.ofEmpty(session);
        }
        //List<NutsId> all = new ArrayList<>();
//        NutsWorkspace ws = session.getWorkspace();
        NutsIdBuilder idBuilder = NutsIdBuilder.of("org.apache.catalina", "apache-tomcat");
        return NutsPath.of("htmlfs:https://archive.apache.org/dist/tomcat/", session)
                .list()
                .filter(x -> x.isDirectory() && x.getName().matches("tomcat-[0-9.]+"), "directory && tomcat")
                .flatMapStream(NutsFunction.of(
                        s -> s.list()
                                .filter(x2 -> x2.isDirectory() && x2.getName().startsWith("v"), "isDirectory")
                                .flatMapStream(
                                        NutsFunction.of(
                                                x3 -> {
                                                    String s2n = x3.getName();
                                                    String prefix = "apache-tomcat-";
                                                    String bin = "bin";
                                                    if (
                                                            s2n.endsWith("-alpha/") || s2n.endsWith("-beta/")
                                                                    || s2n.endsWith("-copyforpermissions/") || s2n.endsWith("-original/")
                                                                    || s2n.matches(".*-RC[0-9]+/") || s2n.matches(".*-M[0-9]+/")
                                                    ) {
                                                        //will ignore all alpha versions
                                                        return NutsStream.ofEmpty(session);
                                                    }
                                                    NutsVersion version = NutsVersion.of(s2n.substring(1, s2n.length() - 1)).get(session);
                                                    if (version.compareTo("4.1.32") < 0) {
                                                        prefix = "jakarta-tomcat-";
                                                    }
                                                    if (version.compareTo("4.1.27") == 0) {
                                                        bin = "binaries";
                                                    }
                                                    boolean checkBin = false;
                                                    if (checkBin) {
                                                        String finalPrefix = prefix;
                                                        return x3.resolve(bin)
                                                                .list()
                                                                .filter(x4 -> x4.getName().matches(finalPrefix + "[0-9]+\\.[0-9]+\\.[0-9]+\\.zip"), "name.isZip")
                                                                .map(x5 -> {
                                                                    String s3 = x5.getName();
                                                                    String v0 = s3.substring(finalPrefix.length(), s3.length() - 4);
                                                                    NutsVersion v = NutsVersion.of(v0).get(session);
                                                                    NutsId id2 = idBuilder.setVersion(v).build();
                                                                    if (filter == null || filter.acceptId(id2, session)) {
                                                                        return id2;
                                                                    }
                                                                    return null;
                                                                }, "toZip")
                                                                .nonNull();
                                                    } else {
                                                        NutsId id2 = idBuilder.setVersion(version).build();
                                                        if (filter == null || filter.acceptId(id2, session)) {
                                                            return NutsStream.ofSingleton(id2, session);
                                                        }
                                                        return NutsStream.ofEmpty(session);
                                                    }
                                                }
                                                , "flatMap"))
                        , "flatMap")).iterator();
    }

    private String getUrl(NutsVersion version, String extension) {
        String bin = "bin";
        String prefix = "apache-tomcat-";
        if (version.compareTo("4.1.32") < 0) {
            prefix = "jakarta-tomcat-";
        }
        if (version.compareTo("4.1.27") == 0) {
            bin = "binaries";
        }
        return HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT + "tomcat-" + version.get(0).flatMap(NutsValue::asString).orElse("unknown") + "/v" + version + "/" + bin + "/" + prefix + version + extension;
    }

    public String getIdLocalFile(NutsId id, NutsFetchMode fetchMode, NutsRepository repository, NutsSession session) {
        return repository.config().getStoreLocation()
                .resolve(session.locations().getDefaultIdBasedir(id))
                .resolve(session.locations().getDefaultIdFilename(id))
                .toString();
    }

//    public boolean catalinaMatchesJavaVersion(NutsVersion cv, String javaVersion, NutsSession session) {
//        if (NutsBlankable.isBlank(javaVersion)) {
//            return true;
//        }
//        if (javaVersion.compareTo("1.8") >= 0) {
//            return NutsVersion.of("[9,10.1[").get().filter(session).acceptVersion(cv, session);
//        } else if (javaVersion.compareTo("1.7") >= 0) {
//            return NutsVersion.of("[8.5,9[").get().filter(session).acceptVersion(cv, session);
//        } else if (javaVersion.compareTo("1.6") >= 0) {
//            return NutsVersion.of("[7,8[").get().filter(session).acceptVersion(cv, session);
//        } else if (javaVersion.compareTo("1.5") < 0) {
//            return NutsVersion.of("[6,7[").get().filter(session).acceptVersion(cv, session);
//        } else if (javaVersion.compareTo("1.4") < 0) {
//            return NutsVersion.of("[5.5,6[").get().filter(session).acceptVersion(cv, session);
//        } else if (javaVersion.compareTo("1.3") < 0) {
//            return NutsVersion.of("[4.1,5[").get().filter(session).acceptVersion(cv, session);
//        } else {
//            return NutsVersion.of("[3.3,4[").get().filter(session).acceptVersion(cv, session);
//        }
//    }

}
