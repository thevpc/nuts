package net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.helpers;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.concurrent.NCachedValue;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.elem.NElementReader;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.platform.NEnv;
import net.thevpc.nuts.runtime.standalone.platform.CorePlatformUtils;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.util.SingleBaseIdFilterHelper;
import net.thevpc.nuts.time.NDuration;
import net.thevpc.nuts.util.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;


public class NetbeansRepoHelper implements ToolboxRepoHelper {
    protected SingleBaseIdFilterHelper baseIdFilterHelper = new SingleBaseIdFilterHelper("org.apache.netbeans:netbeans");
    protected static Map<Integer, NCachedValue<LocalDate>> cachedNetbeansReleaseDates = new ConcurrentHashMap<>();

    @Override
    public NIterator<NId> searchVersions(NId id, NDefinitionFilter filter, NRepository repository) {
        return search(id, filter, new NPath[]{null}, repository);
    }

    @Override
    public boolean acceptId(NId id) {
        return baseIdFilterHelper.accept(id);
    }

    @Override
    public NDescriptor fetchDescriptor(NId id, NRepository repository) {
        //            String r = getUrl(id.getVersion(), ".zip.md5");
        if (!baseIdFilterHelper.accept(id)) {
            return null;
        }
        String r = getUrl(id.version());
        if (r == null) {
            return null;
        }
        int i = id.version().getIntAt(0).orElse(-1);
        int j = id.version().getIntAt(1).orElse(-1);
        String javaVersion = "java#" + minJdkForNetbeans(i);
        return NDescriptorBuilder.of()
                .id(id.longId())
                .icons(
                        "https://upload.wikimedia.org/wikipedia/commons/9/98/Apache_NetBeans_Logo.svg",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Apache_NetBeans_Logo.svg/416px-Apache_NetBeans_Logo.svg.png"
                )
                .packaging("zip")
                .condition(
                        NEnvConditionBuilder.of()
                                .platform(Collections.singletonList(javaVersion))
                )
                .installer(NArtifactCallBuilder.of()
                        .id(NId.of(NConstants.Ids.NSH))
                        .arguments("$nutsIdInstallScriptPath")
                        .scriptName("post-install.sh")
                        .scriptContent(
                                "####"
                                        + "\necho unzip to ${nutsIdBinPath}/app..."
                                        + "\nunzip --skip-root \"$nutsIdContentPath\" -d \"$nutsIdBinPath/app\""
                                        + "\necho prepare executables..."
                                        + "\nchmod +x \"$nutsIdBinPath/app/bin/netbeans\""
                        )
                        .build()
                )
                .executor(NArtifactCallBuilder.of()
                        .id(NId.of("exec"))
                        .arguments(
                                NEnv.of().osFamily().isWindow()
                                        ? "$nutsIdBinPath/app/bin/netbeans.exe"
                                        : "$nutsIdBinPath/app/bin/netbeans"
                        )
                        .build()
                )
                .description("Apache Netbeans Official Zip Bundle")
                .setProperty(DYNAMIC_DESCRIPTOR, "true")
                .build();
    }

    @Override
    public NIterator<NId> search(NId id, NDefinitionFilter filter, NPath[] basePaths, NRepository repository) {
        if (!baseIdFilterHelper.accept(id, basePaths)) {
            return null;
        }
        //List<NutsId> all = new ArrayList<>();
//        NutsWorkspace ws = session.getWorkspace();
        NIdBuilder idBuilder = NIdBuilder.of("org.apache.netbeans", "netbeans");
        NStream<NId> stream1 = NPath.of("htmlfs+https://archive.apache.org/dist/netbeans/netbeans/").stream()
                .filter(path -> path.isDirectory())
                .map(p -> {
                    ///12.0/netbeans-12.0-bin.zip
                    String version = p.name();
                    NPath b = NPath.of("https://archive.apache.org/dist/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
                    if (b.exists()) {
                        return idBuilder.version(version).build();
                    }
                    return null;
                }).nonNull();
        NStream<NId> stream2 = NPath.of("htmlfs+https://downloads.apache.org/netbeans/netbeans/").stream()
                .filter(path -> path.isDirectory())
                .map(p -> {
                    ///12.0/netbeans-12.0-bin.zip
                    String version = p.name();
                    NPath b = NPath.of("https://downloads.apache.org/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
                    if (b.exists()) {
                        return idBuilder.version(version).build();
                    }
                    return null;
                }).nonNull();
        return stream1.concat(stream2).iterator();
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository) {
        if (!baseIdFilterHelper.accept(id)) {
            return null;
        }
        String r = getUrl(id.version());
        NPath localPath = NPath.of(ToolboxRepositoryModel.getIdLocalFile(id.builder().faceContent().build(), repository));
        NCp.of().from(NPath.of(r)).to(localPath)
                .addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
        return localPath;
    }

    private String getUrl(NVersion version) {
//        if (true) {
//            // for test purposes
//            return NPath.ofUserHome()+"/Downloads/netbeans-" + version + "-bin.zip";
//        }
        //nuts supports out of the box navigating apache website using htmlfs
        NPath b = NPath.of("https://archive.apache.org/dist/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
        if (b.exists()) {
            return "https://archive.apache.org/dist/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip";
        }
        b = NPath.of("https://downloads.apache.org/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
        if (b.exists()) {
            return "https://downloads.apache.org/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip";
        }
        return null;
    }

    private static LocalDate fetchNetbeansReleaseDate(int nbVersion) {
        return cachedNetbeansReleaseDates.computeIfAbsent(nbVersion, ii -> NCachedValue.of(() -> {
            NPath api = NPath.of("https://api.github.com/repos/apache/netbeans/releases/tags/" + ii);
            String json = api.readString();
            String published = NElementReader.of().read(json).asObject().flatMap(x -> x.getStringValue("published_at")).orNull();
            if (published == null) {
                return LocalDate.now();
            }
            return LocalDate.parse(published.substring(0, 10));
        }).expiry(NDuration.ofHours(1))).get();
    }

    public static int minJdkForNetbeans(int nbVersion) {
        if (nbVersion <= 18) return 8;
        if (nbVersion <= 21) return 11;
        LocalDate nbReleaseDate = fetchNetbeansReleaseDate(nbVersion);
        List<Integer> availableLts = CorePlatformUtils.JAVA_LTS_RELEASES.stream()
                .filter(e -> !e.releaseDate().isAfter(nbReleaseDate))
                .map(CorePlatformUtils.JdkVersionAndReleaseDate::majorVersion)
                .sorted()
                .collect(Collectors.toList());
        if (availableLts.isEmpty()) return 11; // safe floor for NB 22+
        if (availableLts.size() >= 2) {
            return availableLts.get(availableLts.size() - 2); // LTS-1
        }
        return availableLts.get(availableLts.size() - 1); // fallback
    }

}
