package net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.helpers;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;
import net.thevpc.nuts.runtime.standalone.definition.filter.SafeNDefinitionFilter;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.util.SingleBaseIdFilterHelper;
import net.thevpc.nuts.runtime.standalone.util.NCoreLogUtils;
import net.thevpc.nuts.runtime.standalone.xtra.web.DefaultNWebCli;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Function;


public class TomcatRepoHelper implements ToolboxRepoHelper {

    public static final String HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT = "https://archive.apache.org/dist/tomcat/";
    protected SingleBaseIdFilterHelper baseIdFilterHelper = new SingleBaseIdFilterHelper("org.apache.catalina:apache-tomcat");

    @Override
    public boolean acceptId(NId id) {
        return baseIdFilterHelper.accept(id);
    }

    @Override
    public NIterator<NId> searchVersions(NId id, NDefinitionFilter filter, NRepository repository) {
        return search(id, filter, new NPath[]{null}, repository);
    }

    @Override
    public NIterator<NId> search(NId id, NDefinitionFilter filter, NPath[] basePaths, NRepository repository) {
        if (!baseIdFilterHelper.accept(id, basePaths)) {
            return null;
        }
        SafeNDefinitionFilter safeFilter = new SafeNDefinitionFilter(filter, NMsg.ofC("repo %s", "tomcat"));
        NIdBuilder idBuilder = NIdBuilder.of("org.apache.catalina", "apache-tomcat");

        // Browse the Apache Tomcat archive
        return NPath.of("htmlfs+" + HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT)
                .stream()
                .filter(p -> p.isDirectory() && p.name().matches("tomcat-[0-9.]+"))
                .flatMapStream(tomcatDir -> {
                    String major = tomcatDir.name().substring("tomcat-".length());
                    return tomcatDir.stream()
                            .filter(vDir -> vDir.isDirectory() && vDir.name().startsWith("v"))
                            .map(vDir -> {
                                String versionStr = vDir.name().substring(1); // remove leading 'v'
                                NVersion version = NVersion.get(versionStr).orNull();
                                if (version == null) return null;
                                // Build expected zip URL
                                String zipUrl = getUrl(version, ".zip");
                                if (NPath.of(zipUrl).exists()) {
                                    NId candidate = idBuilder.version(version).build();
                                    if (safeFilter.acceptDefinition(NDefinitionHelper.ofIdOnlyFromRepo(candidate, repository, "TomcatRepoHelper"))) {
                                        return candidate;
                                    }
                                }
                                return null;
                            });
                })
                .nonNull()
                .iterator();
    }

    @Override
    public NDescriptor fetchDescriptor(NId id, NRepository repository) {
        if (!acceptId(id)) return null;
        NVersion version = id.version();
        String zipUrl = getUrl(version, ".zip");
        if (!NPath.of(zipUrl).exists()) return null;

        // Determine required Java version based on Tomcat version
        String javaVersion = requiredJavaVersion(version);

        NEnv env = NEnv.of();
        boolean isWindows = env.osFamily().isWindow();

        // Build installer script (NSH) that unzips and creates the wrapper
        NStringBuilder installerScript = NStringBuilder.of();
        installerScript
                .println("echo 'Extracting Apache Tomcat...'")
                .println("unzip --skip-root \"$nutsIdContentPath\" -d \"$nutsIdBinPath/app\"")
                .println("chmod +x \"$nutsIdBinPath/app/bin/\"*.sh")
                .println()
                .println("make-wrapper --target \"$nutsIdBinPath/tomcat-wrapper\" \\")
                .println("    --default catalina=\"$nutsIdBinPath/app/bin/catalina.sh\" \\")
                .println("    --cmd startup=\"$nutsIdBinPath/app/bin/startup.sh\" \\")
                .println("    --cmd shutdown=\"$nutsIdBinPath/app/bin/shutdown.sh\" \\")
                .println("    --cmd version=\"$nutsIdBinPath/app/bin/version.sh\"")
                .println("echo 'Done.'");

        // For Windows, use .bat files
        if (isWindows) {
            installerScript.replaceAll("catalina.sh", "catalina.bat")
                    .replaceAll("startup.sh", "startup.bat")
                    .replaceAll("shutdown.sh", "shutdown.bat")
                    .replaceAll("version.sh", "version.bat");
        }

        return NDescriptorBuilder.of()
                .id(id.longId())
                .packaging("zip")
                .icons(
                        "https://upload.wikimedia.org/wikipedia/commons/f/fe/Apache_Tomcat_logo.svg",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/f/fe/Apache_Tomcat_logo.svg/595px-Apache_Tomcat_logo.svg.png"
                )
                .installer(NArtifactCallBuilder.of()
                        .id(NId.of(NConstants.Ids.NSH))
                        .arguments("$nutsIdInstallScriptPath")
                        .scriptName("install-tomcat.nsh")
                        .scriptContent(installerScript.build())
                        .build()
                )
                .executor(NArtifactCallBuilder.of()
                        .id(NId.of("exec"))
                        .arguments("$nutsIdBinPath/tomcat-wrapper")
                        .build()
                )
                .condition(NEnvConditionBuilder.of()
                        .platform(Collections.singletonList("java" + javaVersion))
                        .build()
                )
                .description("Apache Tomcat Official Zip Bundle (version " + version + ")")
                .setProperty(DYNAMIC_DESCRIPTOR, "true")
                .build();
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository) {
        if (!acceptId(id)) return null;
        String zipUrl = getUrl(id.version(), ".zip");
        NPath localPath = NPath.of(ToolboxRepositoryModel.getIdLocalFile(id.builder().faceContent().build(), repository));
        NCp.of().from(NPath.of(zipUrl)).to(localPath)
                .addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE)
                .run();
        // Optional: add checksum validation
        // String md5Url = zipUrl + ".md5";
        // String expectedMd5 = NPath.of(md5Url).readString().trim().split(" ")[0];
        // NCp.of().from(NPath.of(zipUrl)).addValidator(NValidator.ofChecksum(expectedMd5, "MD5")).to(localPath).run();
        return localPath;
    }

    private String getUrl(NVersion version, String extension) {
        int major = version.getPartAt(0).map(NVersionPart::value).map(Integer::parseInt).orElse(0);
        String prefix = (major < 5) ? "jakarta-tomcat-" : "apache-tomcat-";
        String binDir = (version.compareTo("4.1.27") == 0) ? "binaries" : "bin";
        return HTTPS_ARCHIVE_APACHE_ORG_DIST_TOMCAT
                + "tomcat-" + major + "/v" + version + "/" + binDir + "/" + prefix + version + extension;
    }

    private String requiredJavaVersion(NVersion version) {
        int major = version.getPartAt(0).map(NVersionPart::value).map(Integer::parseInt).orElse(0);
        if (major <= 3) return "#1.1";
        if (major <= 4) return "#1.3";
        if (major <= 5) return "#1.4";
        if (major <= 6) return "#1.5";
        if (major <= 7) return "#1.6";
        if (major <= 8) return "#1.7";
        if (major <= 9) return "#1.8";
        // Tomcat 10+ requires Java 11 or later
        return "#11";
    }
}