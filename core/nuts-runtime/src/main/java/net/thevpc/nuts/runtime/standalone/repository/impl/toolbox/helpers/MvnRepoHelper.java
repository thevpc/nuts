package net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.helpers;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;
import net.thevpc.nuts.runtime.standalone.definition.filter.SafeNDefinitionFilter;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.util.SingleBaseIdFilterHelper;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NIterator;
import net.thevpc.nuts.util.NStream;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.Collections;

public class MvnRepoHelper implements ToolboxRepoHelper {

    public static final String HTTPS_ARCHIVE_APACHE_ORG_DIST_MAVEN = "https://archive.apache.org/dist/maven/maven-";
    public static final String HELPER_GROUP = "org.apache.maven";
    public static final String HELPER_ARTIFACT = "mvn";

    protected SingleBaseIdFilterHelper baseIdFilterHelper = new SingleBaseIdFilterHelper(HELPER_GROUP+":"+HELPER_ARTIFACT);

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
        SafeNDefinitionFilter safeFilter = new SafeNDefinitionFilter(filter, NMsg.ofC("repo %s", "maven"));
        NIdBuilder idBuilder = NIdBuilder.of(HELPER_GROUP, HELPER_ARTIFACT);

        // Maven releases are under: https://archive.apache.org/dist/maven/maven-<major>/<version>/binaries/
        // We'll scan major versions 3 and 4 (and maybe 2)
        return NStream.ofIntArray(3, 4)
                .flatMapStream(major -> {
                    String majorUrl = HTTPS_ARCHIVE_APACHE_ORG_DIST_MAVEN + major + "/";
                    return NPath.of("htmlfs+" + majorUrl)
                            .stream()
                            .filter(p -> p.isDirectory())
                            .map(versionDir -> {
                                String versionStr = versionDir.name(); // e.g. "3.9.9"
                                NVersion version = NVersion.get(versionStr).orNull();
                                if (version == null) return null;
                                // Check if zip exists
                                String zipUrl = getZipUrl(version);
                                if (NPath.of(zipUrl).exists()) {
                                    NId candidate = idBuilder.version(version).build();
                                    if (safeFilter.acceptDefinition(NDefinitionHelper.ofIdOnlyFromRepo(candidate, repository, "MavenRepoHelper"))) {
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
        String zipUrl = getZipUrl(version);
        if (!NPath.of(zipUrl).exists()) return null;

        String requiredJavaVersion = requiredJavaVersion(version);
        NEnv env = NEnv.of();
        boolean isWindows = env.osFamily().isWindow();

        NStringBuilder installerScript = NStringBuilder.of();
        installerScript
                .println("echo 'Extracting Apache Maven...'")
                .println("unzip --skip-root \"$nutsIdContentPath\" -d \"$nutsIdBinPath/app\"")
                .println("chmod +x \"$nutsIdBinPath/app/bin/mvn\"")
                .println()
                .println("make-wrapper --target \"$nutsIdBinPath/mvn-wrapper\" \\")
                .println("    --default mvn=\"$nutsIdBinPath/app/bin/mvn\"")
                .println("echo 'Done.'");

        // For Windows, ensure .bat is used
        if (isWindows) {
            installerScript.replaceAll("mvn\"", "mvn.bat\"");
        }

        return NDescriptorBuilder.of()
                .id(id.longId())
                .packaging("zip")
                .icons("https://maven.apache.org/images/maven-logo-black-on-white.png")
                .installer(NArtifactCallBuilder.of()
                        .id(NId.of(NConstants.Ids.NSH))
                        .arguments("$nutsIdInstallScriptPath")
                        .scriptName("install-maven.nsh")
                        .scriptContent(installerScript.build())
                        .build()
                )
                .executor(NArtifactCallBuilder.of()
                        .id(NId.of("exec"))
                        .arguments("$nutsIdBinPath/mvn-wrapper")
                        .build()
                )
                .condition(NEnvConditionBuilder.of()
                        .platform(Collections.singletonList("java" + requiredJavaVersion))
                        .build()
                )
                .description("Apache Maven Official Binary Distribution (version " + version + ")")
                .setProperty(DYNAMIC_DESCRIPTOR, "true")
                .build();
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository) {
        if (!acceptId(id)) return null;
        String zipUrl = getZipUrl(id.version());
        NPath localPath = NPath.of(ToolboxRepositoryModel.getIdLocalFile(id.builder().faceContent().build(), repository));
        NCp.of().from(NPath.of(zipUrl)).to(localPath)
                .addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE)
                .run();
        // Optional checksum: apache provides .sha512 files
        return localPath;
    }

    private String getZipUrl(NVersion version) {
        int major = version.getPartAt(0).map(NVersionPart::value).map(Integer::parseInt).orElse(3);
        String base = HTTPS_ARCHIVE_APACHE_ORG_DIST_MAVEN + major + "/" + version + "/binaries/";
        return base + "apache-maven-" + version + "-bin.zip";
    }

    private String requiredJavaVersion(NVersion version) {
        int major = version.getPartAt(0).map(NVersionPart::value).map(Integer::parseInt).orElse(3);
        if (major <= 2) return "#1.4";
        // Maven 3.0 - 3.2 requires Java 1.5+
        // Maven 3.3 - 3.8 requires Java 1.7+
        // Maven 3.9+ requires Java 1.8+
        // Maven 4.x requires Java 11+
        if (major >= 4) return "#11";
        // For 3.x, check minor
        int minor = version.getPartAt(1).map(NVersionPart::value).map(Integer::parseInt).orElse(0);
        if (minor <= 2) return "#1.5";
        if (minor <= 8) return "#1.7";
        return "#1.8";
    }
}