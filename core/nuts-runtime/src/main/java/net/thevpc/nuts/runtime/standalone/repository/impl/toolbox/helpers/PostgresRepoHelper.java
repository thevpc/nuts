package net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.helpers;

import net.thevpc.nuts.app.NApp;
import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.runtime.standalone.definition.filter.SafeNDefinitionFilter;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.util.SingleBaseIdFilterHelper;
import net.thevpc.nuts.spi.NDefinitionFactory;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.*;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class PostgresRepoHelper implements ToolboxRepoHelper {

    public static final String ZONKY_BASE_URL =
            "https://repo1.maven.org/maven2/io/zonky/test/postgres/embedded-postgres-binaries-";
    public static final String ZONKY_GROUP = "io.zonky.test.postgres";
    public static final String ZONKY_ARTIFACT = "embedded-postgres-binaries-";

    protected SingleBaseIdFilterHelper baseIdFilterHelper =
            new SingleBaseIdFilterHelper("org.postgresql:postgresql-server");

    // -------------------------------------------------------------------------
    // OS/arch → zonky classifier
    // -------------------------------------------------------------------------

    private static String zonkyPlatform() {
        NEnv env = NEnv.of();
        NOsFamily os = env.osFamily();
        NArchFamily arch = env.archFamily();

        String osPart;
        switch (os) {
            case LINUX:
                osPart = "linux";
                break;
            case WINDOWS:
                osPart = "windows";
                break;
            case MACOS:
                osPart = "darwin";
                break;
            default:
                osPart = "linux";
                break; // best-effort fallback
        }

        String archPart;
        switch (arch) {
            case X86_64:
                archPart = "amd64";
                break;
            case AARCH_64:
            case ARM_64:
                archPart = "arm64v8";
                break;
            case X86_32:
                archPart = "i386";
                break;
            case PPC_64:
                archPart = "ppc64le";
                break;
            default:
                archPart = "amd64";
                break; // best-effort fallback
        }

        return osPart + "-" + archPart; // e.g. "linux-amd64", "darwin-arm64v8"
    }

    /**
     * Full Maven artifact id for current platform, e.g.
     * "embedded-postgres-binaries-linux-amd64"
     */
    private static String zonkyArtifactId() {
        return ZONKY_ARTIFACT + zonkyPlatform();
    }

    /**
     * Base URL for maven-metadata.xml of current platform
     */
    private static String zonkyMetadataUrl() {
        return ZONKY_BASE_URL + zonkyPlatform() + "/maven-metadata.xml";
    }

    /**
     * Download URL for a specific version jar
     */
    private static String zonkyJarUrl(String version) {
        String artifactId = zonkyArtifactId();
        return ZONKY_BASE_URL + zonkyPlatform()
                + "/" + version
                + "/" + artifactId + "-" + version + ".jar";
    }

    // -------------------------------------------------------------------------
    // Version listing via maven-metadata.xml
    // -------------------------------------------------------------------------

    private static List<String> fetchZonkyVersions() {
        String metaUrl = zonkyMetadataUrl();
        try {
            String xml = NPath.of(metaUrl).readString();
            // parse <version>...</version> tags — no XML lib needed
            List<String> versions = new ArrayList<>();
            int pos = 0;
            while (true) {
                int start = xml.indexOf("<version>", pos);
                if (start < 0) break;
                int end = xml.indexOf("</version>", start);
                if (end < 0) break;
                String v = xml.substring(start + "<version>".length(), end).trim();
                if (v.matches("[0-9]+\\.[0-9]+(\\.[0-9]+)?")) {
                    versions.add(v);
                }
                pos = end + 1;
            }
            return versions;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    // -------------------------------------------------------------------------
    // ToolboxRepoHelper impl
    // -------------------------------------------------------------------------

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
        SafeNDefinitionFilter safeFilter = new SafeNDefinitionFilter(
                filter, NMsg.ofC("repo %s", "postgres-zonky"));

        NIdBuilder idBuilder = NIdBuilder.of("org.postgresql", "postgresql-server");
        List<String> versions = fetchZonkyVersions();
        List<NId> ids = new ArrayList<>();
        for (String v : versions) {
            NId id0 = idBuilder.version(NVersion.of(v)).build();
            NDefinition dd = NDefinitionFactory.of().byId(id0, repository);
            if (safeFilter.acceptDefinition(dd)) {
                ids.add(id0);
            }
        }
        return NIterator.of(ids.iterator())
                .withDescription(NDescribables.ofDesc("NIterator postgres-zonky versions"));
    }

    @Override
    public NDescriptor fetchDescriptor(NId id, NRepository repository) {
        if (!baseIdFilterHelper.accept(id)) {
            return null;
        }
        String version = id.version().value();
        String jarUrl = zonkyJarUrl(version);

        // quick HEAD-like check: try opening the stream
        boolean found = false;
        try {
            URL url = new URL(jarUrl);
            NSession session = NSession.of();
            session.terminal().printProgress(NMsg.ofC("peek %s", jarUrl));
            try (InputStream is = url.openStream()) {
                found = true;
            }
        } catch (Exception ex) {
            found = false;
        }
        if (!found) {
            return null;
        }

        NEnv env = NEnv.of();
        String execBin = env.osFamily().isWindow()
                ? "$nutsIdBinPath/pgsql/bin/postgres.exe"
                : "$nutsIdBinPath/pgsql/bin/postgres";

        return NDescriptorBuilder.of()
                .id(id.longId())
                .packaging("jar")          // zonky ships a .jar wrapping a .tar.gz
                .description("PostgreSQL portable binary bundle (Zonky, " + zonkyPlatform() + ")")
                .condition(
                        NEnvConditionBuilder.of()
                                .os(Collections.singletonList(env.osFamily().id()))
                                .arch(Collections.singletonList(env.archFamily().id()))
                )
                .installer(NArtifactCallBuilder.of()
                        .id(NId.of(NConstants.Ids.NSH))
                        .arguments("$nutsIdInstallScriptPath")
                        .scriptName("post-install.sh")
                        .scriptContent(
                                NStringBuilder.of()
                                        .println("echo 'Extracting Zonky PostgreSQL jar...'")
                                        .println("unzip -o \"$nutsIdContentPath\" -d \"$nutsIdBinPath/tmp\"")
                                        .println("mkdir -p \"$nutsIdBinPath/pgsql\"")
                                        .println("tar -xJf \"$nutsIdBinPath/tmp\"/*.txz -C \"$nutsIdBinPath/pgsql\"")
                                        .println("rm -rf \"$nutsIdBinPath/tmp\"")
                                        .println("make-wrapper --target \"$nutsIdBinPath/postgresql-wrapper\" --default \"$nutsIdBinPath/pgsql/bin/postgres\" --cmd \"$nutsIdBinPath/pgsql/bin/initdb\" --cmd \"$nutsIdBinPath/pgsql/bin/pg_ctl\"")
                                        .println("echo 'Done.'")
                                        .build()
                        )
                        .build()
                )
                .executor(NArtifactCallBuilder.of()
                        .id(NId.of("exec"))
                        .arguments("$nutsIdBinPath/postgresql-wrapper")
                        .build()
                )
                .setProperty(DYNAMIC_DESCRIPTOR, "true")
                .build();
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository) {
        if (!baseIdFilterHelper.accept(id)) {
            return null;
        }
        String version = id.version().value();
        String jarUrl = zonkyJarUrl(version);

        NPath localPath = NPath.of(
                ToolboxRepositoryModel.getIdLocalFile(
                        id.builder().faceContent().build(), repository));

        NPath path = NPath.of(jarUrl);
        if(true){
            path=NPath.ofUserHome().resolve("Downloads/embedded-postgres-binaries-linux-amd64-18.4.0.jar");
        }
        NCp.of().from(path).to(localPath)
                .addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE)
                .run();
        return localPath;
    }
}