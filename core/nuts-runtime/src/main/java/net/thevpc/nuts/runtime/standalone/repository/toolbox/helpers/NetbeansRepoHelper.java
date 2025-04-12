package net.thevpc.nuts.runtime.standalone.repository.toolbox.helpers;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.repository.toolbox.ToolboxRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.toolbox.ToolboxRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.util.SingleBaseIdFilterHelper;
import net.thevpc.nuts.util.*;

import java.util.Arrays;


public class NetbeansRepoHelper implements ToolboxRepoHelper {
    protected SingleBaseIdFilterHelper baseIdFilterHelper = new SingleBaseIdFilterHelper("org.apache.netbeans:netbeans");

    @Override
    public NIterator<NId> searchVersions(NId id, NDefinitionFilter filter, NRepository repository) {
        return search(filter, new NPath[]{null}, repository);
    }

    @Override
    public boolean acceptId(NId id) {
        return baseIdFilterHelper.accept(id);
    }

    @Override
    public NDescriptor fetchDescriptor(NId id, NRepository repository) {
        //            String r = getUrl(id.getVersion(), ".zip.md5");
        if(!baseIdFilterHelper.accept(id)){
            return null;
        }
        String r = getUrl(id.getVersion());
        if (r == null) {
            return null;
        }
        int i = id.getVersion().getIntegerAt(0).orElse(-1);
        int j = id.getVersion().getIntegerAt(1).orElse(-1);
        String javaVersion = "java";
        if (i <= 0) {
            //
        } else if (i <= 16) {
            javaVersion = "java#1.8";
        } else /*if(i<=10)*/ {
            javaVersion = "java#17";
        }
        return NDescriptorBuilder.of()
                .setId(id.getLongId())
                .setIcons(
                        "https://upload.wikimedia.org/wikipedia/commons/9/98/Apache_NetBeans_Logo.svg",
                        "https://upload.wikimedia.org/wikipedia/commons/thumb/9/98/Apache_NetBeans_Logo.svg/416px-Apache_NetBeans_Logo.svg.png"
                )
                .setPackaging("zip")
                .setCondition(
                        NEnvConditionBuilder.of()
                                .setPlatform(Arrays.asList(javaVersion))
                )
                .setInstaller(NArtifactCallBuilder.of()
                        .setId(NId.of(NConstants.Ids.NSH))
                        .setArguments(
                                "-c", "unzip",
                                "--skip-root",
                                "$nutsIdContentPath",
                                "-d",
                                "$nutsIdBinPath/app"
                        )
                        .build()
                )
                .setExecutor(NArtifactCallBuilder.of()
                        .setId(NId.of("exec"))
                        .setArguments(
                                NWorkspace.of().getOsFamily().isWindow()
                                        ? "$nutsIdBinPath/app/bin/netbeans.exe"
                                        : "$nutsIdBinPath/app/bin/netbeans"
                        )
                        .build()
                )
                .setDescription("Apache Netbeans Official Zip Bundle")
                .setProperty("dynamic-descriptor", "true")
                .build();
    }

    @Override
    public NIterator<NId> search(NDefinitionFilter filter, NPath[] basePaths, NRepository repository) {
        if(!baseIdFilterHelper.accept(basePaths)){
            return null;
        }
        //List<NutsId> all = new ArrayList<>();
//        NutsWorkspace ws = session.getWorkspace();
        NIdBuilder idBuilder = NIdBuilder.of("org.apache.netbeans", "netbeans");
        NStream<NId> stream1 = NPath.of("htmlfs:https://archive.apache.org/dist/netbeans/netbeans/").stream()
                .filter(path -> path.isDirectory())
                .map(p -> {
                    ///12.0/netbeans-12.0-bin.zip
                    String version = p.getName();
                    NPath b = NPath.of("https://archive.apache.org/dist/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
                    if (b.exists()) {
                        return idBuilder.setVersion(version).build();
                    }
                    return null;
                }).nonNull();
        NStream<NId> stream2 = NPath.of("htmlfs:https://downloads.apache.org/netbeans/netbeans/").stream()
                .filter(path -> path.isDirectory())
                .map(p -> {
                    ///12.0/netbeans-12.0-bin.zip
                    String version = p.getName();
                    NPath b = NPath.of("https://downloads.apache.org/netbeans/netbeans/" + version + "/netbeans-" + version + "-bin.zip");
                    if (b.exists()) {
                        return idBuilder.setVersion(version).build();
                    }
                    return null;
                }).nonNull();
        return stream1.concat(stream2).iterator();
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository) {
        if(!baseIdFilterHelper.accept(id)){
            return null;
        }
        String r = getUrl(id.getVersion());
        NPath localPath = NPath.of(ToolboxRepositoryModel.getIdLocalFile(id.builder().setFaceContent().build(), repository));
        NCp.of().from(NPath.of(r)).to(localPath)
                .addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
        return localPath;
    }

    private String getUrl(NVersion version) {
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

}
