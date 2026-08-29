package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NStoreKey;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.*;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public abstract class AbstractDiscoJavaProvider implements JavaProvider {

    protected abstract String getDiscoDistributionName();

    @Override
    public NOptional<NPath> resolveAndInstall(String product, int version, NOsFamily os, NArchFamily arch) {
        NOptional<Info> p = resolveDownloadUrl(product, version, os, arch);
        if (p.isPresent()) {
            NPath folderCache = NPath.of(NStoreKey.ofCache(NWorkspace.of().apiId()))
                    .resolve("sdk/java/" + getName() + "/")
                    .resolve(getName() + "-" + version + "-" + os.id() + "-" + arch.id());
            NPath folderBin = NPath.of(NStoreKey.ofBin(NWorkspace.of().apiId()))
                    .resolve("sdk/java/" + getName() + "/")
                    .resolve(getName() + "-" + version + "-" + os.id() + "-" + arch.id());

            if (folderBin.resolve("dist/nuts-install-info.tson").isRegularFile()) {
                return NOptional.of(folderBin.resolve("dist"));
            }

            NPath toCache = folderCache.resolve(p.get().path.name());
            if (!toCache.exists()) {
                NCp.of().from(p.get().path)
                        .to(toCache.mkParentDirs())
                        .options(NPathOption.LOG, NPathOption.TRACE)
                        .run();
            }

            boolean checkFolder = false;
            if (toCache.name().endsWith(".zip")) {
                NUncompress.of().from(toCache).to(folderBin.mkdirs()).run();
                checkFolder = true;
            } else if (toCache.name().endsWith(".tar.gz") || toCache.name().endsWith(".tgz")) {
                NExec.of()
                        .command("tar", "-xzf", toCache.toString(), "-C", folderBin.mkdirs().toString())
                        .run();
                checkFolder = true;
            }

            if (checkFolder) {
                List<NPath> singleRoot = folderBin.list().stream()
                        .filter(x -> x.isDirectory() && !"dist".equals(x.name()))
                        .collect(Collectors.toList());

                if (singleRoot.size() == 1) {
                    NPath finalFolder = folderBin.resolve("dist");
                    singleRoot.get(0).moveTo(finalFolder);
                    NElementWriter.ofPlainTson()
                            .print(NElement.ofObjectBuilder()
                                            .add("vendor", getName())
                                            .add("version", String.valueOf(version))
                                            .add("os", NElement.ofEnum(os))
                                            .add("arch", NElement.ofEnum(arch))
                                            .add("distFolderName", singleRoot.get(0).name())
                                            .add("downloadUrl", p.get().path.toString())
                                            .add("downloadDate", NElement.ofInstant(Instant.now()))
                                            .add("localCachePath", toCache.toString()),
                                    finalFolder.resolve("nuts-install-info.tson"));
                    return NOptional.of(finalFolder);
                }
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("Java package not found for %s %s %s", getName(), version, arch.id()));
    }

    protected static class Info {
        NPath path;
        String checksum;
    }

    public NOptional<Info> resolveDownloadUrl(String product, int version, NOsFamily os, NArchFamily arch) {
        String discoOs = null;
        switch (os) {
            case WINDOWS:
                discoOs = "windows";
                break;
            case MACOS:
                discoOs = "macos";
                break;
            case LINUX:
                discoOs = "linux";
                break;
        }
        if (discoOs == null) {
            return NOptional.ofEmpty();
        }

        String discoArch = arch.id();
        switch (arch) {
            case X86_64 : discoArch ="x64";break;
            case X86_32 : discoArch ="x86";break;
            case ARM_64 : discoArch ="aarch64";break;
            case ARM_32 : discoArch ="arm";break;
        };

        String pkgType = "jre".equalsIgnoreCase(product) ? "jre" : "jdk";

        String url = "https://api.foojay.io/disco/v3.0/packages?"
                + "version=" + version
                + "&distro=" + getDiscoDistributionName()
                + "&operating_system=" + discoOs
                + "&architecture=" + discoArch
                + "&package_type=" + pkgType
                + "&release_status=ga"
                + "&directly_downloadable=true";

        NElement elem = NElementReader.ofJson().read(NPath.of(url));
        if (elem.isAnyObject()) {
            NArrayElement results = elem.asObject().get().getArray("result").orNull();
            if (results != null) {
                for (NElement item : results) {
                    if (item.isAnyObject()) {
                        NObjectElement obj = item.asObject().get();
                        String archiveType = obj.getStringValue("archive_type").orNull();
                        // Prefer archive formats that do not require OS installers (.tar.gz / .zip)
                        if ("tar.gz".equalsIgnoreCase(archiveType) || "zip".equalsIgnoreCase(archiveType) || "tgz".equalsIgnoreCase(archiveType)) {
                            String packageUrl = obj.getStringValue("links/pkg_download_redirect").orNull();
                            if (packageUrl == null) {
                                packageUrl = obj.getStringValue("download_url").orNull();
                            }
                            if (packageUrl != null) {
                                Info info = new Info();
                                info.path = NPath.of(packageUrl);
                                info.checksum = obj.getStringValue("checksum").orNull();
                                return NOptional.of(info);
                            }
                        }
                    }
                }
            }
        }
        return NOptional.ofEmpty();
    }
}