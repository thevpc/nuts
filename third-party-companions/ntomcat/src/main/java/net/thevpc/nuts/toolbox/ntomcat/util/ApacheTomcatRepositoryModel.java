package net.thevpc.nuts.toolbox.ntomcat.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.NEnvConditionBuilder;

import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;
import java.util.function.Function;

public class ApacheTomcatRepositoryModel implements NRepositoryModel {
    public static final String HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT = "https://archive.apache.org/dist/tomcat/";
    private NWorkspace workspace;
    public ApacheTomcatRepositoryModel(NWorkspace workspace) {
        this.workspace=workspace;
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
    public NIterator<NId> searchVersions(NId id, NIdFilter filter, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            return search(filter, new NPath[]{null}, fetchMode, repository);
        }
        return null;
    }

    @Override
    public NDescriptor fetchDescriptor(NId id, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
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
            NSession session = workspace.currentSession();
            if (url != null) {
                session.getTerminal().printProgress(NMsg.ofC("peek %s", url));
                try (InputStream inputStream = url.openStream()) {
                    //ws.io().copy().from(r).getByteArrayResult();
                    found = true;
                } catch (Exception ex) {
                    found = false;
                }
            }
            if (found) {
                // http://tomcat.apache.org/whichversion.html
                int i = id.getVersion().getNumber(0).flatMap(NLiteral::asInt).orElse(-1);
                int j = id.getVersion().getNumber(1).flatMap(NLiteral::asInt).orElse(-1);
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

                return NDescriptorBuilder.of()
                        .setId(id.getLongId())
                        .setPackaging("zip")
                        .setCondition(
                                NEnvConditionBuilder.of()
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
    public NPath fetchContent(NId id, NDescriptor descriptor, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
            return null;
        }
        if ("org.apache.catalina:apache-tomcat".equals(id.getShortName())) {
            String r = getUrl(id.getVersion(), ".zip");
            NPath localPath = NPath.of(getIdLocalFile(id.builder().setFaceContent().build(), fetchMode, repository));
            NCp.of().from(NPath.of(r)).to(localPath)
                    .addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
            return localPath;
        }
        return null;
    }

    @Override
    public NIterator<NId> search(NIdFilter filter, NPath[] basePaths, NFetchMode fetchMode, NRepository repository) {
        if (fetchMode != NFetchMode.REMOTE) {
            return NIterator.ofEmpty();
        }
        //List<NutsId> all = new ArrayList<>();
//        NutsWorkspace ws = session.getWorkspace();
        NIdBuilder idBuilder = NIdBuilder.of("org.apache.catalina", "apache-tomcat");
        return NPath.of("htmlfs:https://archive.apache.org/dist/tomcat/")
                .stream()
                .filter(NPredicate.of((NPath x) -> x.isDirectory() && x.getName().matches("tomcat-[0-9.]+")).withDesc(NEDesc.of("directory && tomcat")))
                .flatMapStream(NFunction.of(
                        (NPath s) -> s.stream()
                                .filter(NPredicate.of((NPath x2) -> x2.isDirectory() && x2.getName().startsWith("v")).withDesc(NEDesc.of("isDirectory")))
                                .flatMapStream(
                                        NFunction.of(
                                                new Function<NPath, NStream<NId>>() {
                                                    @Override
                                                    public NStream<NId> apply(NPath x3) {
                                                        String s2n = x3.getName();
                                                        String prefix = "apache-tomcat-";
                                                        String bin = "bin";
                                                        if (
                                                                s2n.endsWith("-alpha/") || s2n.endsWith("-beta/")
                                                                        || s2n.endsWith("-copyforpermissions/") || s2n.endsWith("-original/")
                                                                        || s2n.matches(".*-RC[0-9]+/") || s2n.matches(".*-M[0-9]+/")
                                                        ) {
                                                            //will ignore all alpha versions
                                                            return NStream.ofEmpty();
                                                        }
                                                        NVersion version = NVersion.get(s2n.substring(1, s2n.length() - 1)).get();
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
                                                                    .stream()
                                                                    .filter(
                                                                            NPredicate.<NPath>of((NPath x4) -> x4.getName().matches(finalPrefix + "[0-9]+\\.[0-9]+\\.[0-9]+\\.zip"))
                                                                                    .withDesc(NEDesc.of("name.isZip"))

                                                                    )
                                                                    .map(NFunction.<NPath, NId>of(
                                                                            (NPath x5) -> {
                                                                                String s3 = x5.getName();
                                                                                String v0 = s3.substring(finalPrefix.length(), s3.length() - 4);
                                                                                NVersion v = NVersion.get(v0).get();
                                                                                NId id2 = idBuilder.setVersion(v).build();
                                                                                if (filter == null || filter.acceptId(id2)) {
                                                                                    return id2;
                                                                                }
                                                                                return null;
                                                                            }).<NId>withDesc(NEDesc.of("toZip")))
                                                                    .<NId>nonNull();
                                                        } else {
                                                            NId id2 = idBuilder.setVersion(version).build();
                                                            if (filter == null || filter.acceptId(id2)) {
                                                                return NStream.ofSingleton(id2);
                                                            }
                                                            return NStream.ofEmpty();
                                                        }

                                                    }
                                                }).withDesc(NEDesc.of("flatMap")))
                ).withDesc(NEDesc.of("flatMap"))).iterator();
    }

    private String getUrl(NVersion version, String extension) {
        String bin = "bin";
        String prefix = "apache-tomcat-";
        if (version.compareTo("4.1.32") < 0) {
            prefix = "jakarta-tomcat-";
        }
        if (version.compareTo("4.1.27") == 0) {
            bin = "binaries";
        }
        return HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT + "tomcat-" + version.get(0).flatMap(NLiteral::asString).orElse("unknown") + "/v" + version + "/" + bin + "/" + prefix + version + extension;
    }

    public String getIdLocalFile(NId id, NFetchMode fetchMode, NRepository repository) {
        return repository.config().getStoreLocation()
                .resolve(NWorkspace.of().getDefaultIdBasedir(id))
                .resolve(NWorkspace.of().getDefaultIdFilename(id))
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
