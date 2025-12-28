package net.thevpc.nuts.runtime.standalone.util.jclass;

import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.io.NUncompress;
import net.thevpc.nuts.platform.NArchFamily;
import net.thevpc.nuts.platform.NOsFamily;
import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TemurinProvider implements JavaProvider {
    @Override
    public String getName() {
        return "temurin";
    }

    @Override
    public NOptional<NPath> resolveAndInstall(String product, int version, NOsFamily os, NArchFamily arch) {
        NOptional<Info> p = resolveDownloadUrl(product, version, os, arch);
        if (p.isPresent()) {
            NPath folderCache = NPath.ofIdStore(NWorkspace.of().getApiId(), NStoreType.CACHE).resolve("sdk/java/" + getName() + "/").resolve(getName() + "-" + version + "-" + os.id() + "-" + arch.id());
            NPath folderBin = NPath.ofIdStore(NWorkspace.of().getApiId(), NStoreType.BIN).resolve("sdk/java/" + getName() + "/").resolve(getName() + "-" + version + "-" + os.id() + "-" + arch.id());
            if (folderCache.resolve("dist/nuts-install-info.tson").isRegularFile()) {
                return NOptional.of(folderCache.resolve("dist"));
            }
            NPath toCache = folderCache.resolve(p.get().path.getName());
            if (!toCache.exists()) {
                NCp.of().from(p.get().path)
                        .to(toCache.mkParentDirs())
                        .addOptions(NPathOption.LOG, NPathOption.TRACE)
                        .run();
            }
            boolean checkFolder = false;
            if (toCache.getName().endsWith(".zip")) {
                NUncompress.of().from(toCache).to(folderCache).run();
                checkFolder = true;
            } else if (toCache.getName().endsWith(".tar.gz")) {
                NExec.of()
                        .addCommand("tar", "-xzf", toCache.toString(), "-C", folderBin.mkdirs().toString())
                        .run();
                checkFolder = true;
            }
            if (checkFolder) {
                List<NPath> singleRoot = folderBin.list().stream().filter(x -> x.isDirectory()).collect(Collectors.toList());
                if (singleRoot.size() == 1) {
                    NPath finalFolder = singleRoot.get(0).resolveSibling("dist");
                    singleRoot.get(0).moveTo(finalFolder);
                    NElementFormat.ofPlainTson(
                                    NElement.ofObjectBuilder()
                                            .add("vendor", getName())
                                            .add("version", String.valueOf(version))
                                            .add("os", NElement.ofEnum(os))
                                            .add("arch", NElement.ofEnum(arch))
                                            .add("distFolderName", singleRoot.get(0).getName())
                                            .add("downloadUrl", p.get().path.toString())
                                            .add("downloadDate", NElement.ofInstant(Instant.now()))
                                            .add("localCachePath", toCache.toString())
                            )
                            .print(finalFolder.resolve("nuts-install-info.tson"));
                    return NOptional.of(finalFolder);
                }
            }
        }
        return NOptional.ofEmpty(NMsg.ofC("java not found : %s"));
    }

    private static class Info {
        NPath path;
        String checksum;
    }

    public NOptional<Info> resolveDownloadUrl(
            String product,
            int version,
            NOsFamily os,
            NArchFamily arch
    ) {
        NElement elem = NElementParser.ofJson().parse(NPath.of("https://api.adoptium.net/v3/assets/feature_releases/" + version + "/ga"));
        String acceptableOs = "";
        switch (os) {
            case WINDOWS: {
                acceptableOs = "windows";
                break;
            }
            case LINUX: {
                acceptableOs = "linux";
                break;
            }
            case MACOS: {
                acceptableOs = "mac";
                break;
            }
            case UNIX: {
                acceptableOs = "aix";
                break;
            }
        }
        if (elem.isAnyArray()) {
            NArrayElement arr = elem.asArray().get();
            for (NElement a : arr) {
                if (a.isAnyObject()) {
                    NObjectElement o = a.asObject().get();
                    NArrayElement binaries = o.getArray("binaries").orNull();
                    if (binaries != null) {
                        for (NElement binary : binaries) {
                            if (binary.isAnyObject()) {
                                NObjectElement bo = binary.asObject().get();
                                NArchFamily foundArch = NArchFamily.parse(bo.getStringValue("architecture").orNull()).orNull();
                                String foundOs = bo.getStringValue("os").orNull();
                                String foundProduct = bo.getStringValue("image_type").orNull();
                                String foundjvm_impl = bo.getStringValue("jvm_impl").orNull();
                                if (arch.equals(foundArch) && acceptableOs.equals(foundOs) && product.equals(foundProduct)
                                        && "hotspot".equals(foundjvm_impl)
                                ) {
                                    NObjectElement pck = bo.getObject("package").orNull();
                                    if (pck != null) {
                                        String link = pck.getStringValue("link").orNull();
                                        String checksum = pck.getStringValue("checksum").orNull();
                                        if (link != null && checksum != null) {
                                            Info i = new Info();
                                            i.path = NPath.of(link);
                                            i.checksum = checksum;
                                            return NOptional.of(i);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return NOptional.ofEmpty();
    }
}
