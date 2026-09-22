package net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.helpers;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.core.NConstants;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.core.NRepository;
import net.thevpc.nuts.pipeline.NIterator;
import net.thevpc.nuts.pipeline.NStream;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepoHelper;
import net.thevpc.nuts.runtime.standalone.repository.impl.toolbox.ToolboxRepositoryModel;
import net.thevpc.nuts.runtime.standalone.repository.impl.util.LocalUrlHelper;
import net.thevpc.nuts.runtime.standalone.repository.util.SingleBaseIdFilterHelper;
import net.thevpc.nuts.util.NStringBuilder;

/**
 * Toolbox helper for jEdit (https://sourceforge.net/projects/jedit/).
 * <p>
 * jEdit publishes no plain "zip, no installer" artifact on any OS - only an
 * IzPack installer (install.jar / install.exe / install.dmg) and a source
 * tarball. A raw .deb exists for ~5.3.0+ but that's Linux-only and covers
 * only recent versions, which breaks the "3 OS families" requirement.
 * <p>
 * Instead this uses IzPack's own documented unattended mode:
 *   java -jar jeditXinstall.jar auto &lt;install-dir&gt;
 * This is plain JVM invocation (no ar/tar/dpkg/native shell tooling), fully
 * headless/non-interactive, and works identically on Windows, Linux and
 * macOS since install.jar itself is cross-platform Java. It's still
 * technically "running the installer", just silently and scripted, which
 * is the closest cross-platform equivalent to "unzip and go" that jEdit
 * actually offers.
 * <p>
 * VERIFIED against jedit 5.7.0 (IzPack 5.x): the installer rejects the
 * legacy "-DINSTALL_PATH + -options-system" flags and only accepts the
 * "auto &lt;install-dir&gt;" subcommand, which unpacks the conventional
 * jEdit layout (jedit.jar at the install root, alongside jars/, doc/,
 * macros/) non-interactively (exit 0).
 * <p>
 * NUTS_DEPLOY_CONF is the env var Nuts injects for the XDG-compliant
 * config dir (see NExecHelper.defVarMap).
 */
public class JeditRepoHelper implements ToolboxRepoHelper {

    private static final String SF_BASE = "https://sourceforge.net/projects/jedit/files/jedit/";

    protected SingleBaseIdFilterHelper baseIdFilterHelper = new SingleBaseIdFilterHelper("org.jedit:jedit");

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
        if (!baseIdFilterHelper.accept(id)) {
            return null;
        }
        String installerUrl = getInstallerUrl(id.version());
        if (installerUrl == null) {
            return null;
        }
        return NDescriptorBuilder.of()
                .id(id.longId())
                .packaging("jar")
                .installer(NArtifactCallBuilder.of()
                        .id(NId.of(NConstants.Ids.NSH))
                        .arguments("${NUTS_DEPLOY_INSTALL_SCRIPT}")
                        .scriptName("post-install.sh")
                        .scriptContent(
                                NStringBuilder.of()
                                        .println("####")
                                        .println("echo running jEdit IzPack installer in unattended mode into ${NUTS_DEPLOY_BIN}/app ...")
                                        .println("mkdir -p \"${NUTS_DEPLOY_BIN}/app\"")
                                        .println("java -jar \"${NUTS_DEPLOY_CONTENT}\" auto \"${NUTS_DEPLOY_BIN}/app\"")
                                        .println("echo mapping jEdit settings into the Nuts XDG-compliant config folder ...")
                                        .println("mkdir -p \"${NUTS_DEPLOY_CONF}/jedit\"")
                                        .build()
                        )
                        .build()
                )
                .executor(NArtifactCallBuilder.of()
                        .id(NId.of("exec"))
                        .arguments(
                                // -settings redirects jEdit's default ~/.jedit into the
                                // Nuts-managed, XDG-compliant config dir for this workspace.
                                "java",
                                "-jar",
                                "${NUTS_DEPLOY_BIN}/app/jedit.jar",
                                "-settings=${NUTS_DEPLOY_CONF}/jedit"
                        )
                        .build()
                )
                .description("jEdit programmer's text editor, deployed via IzPack's unattended " +
                        "install mode - no GUI, works on Windows/Linux/macOS - " +
                        "with settings redirected into the Nuts XDG config folder")
                .setProperty(DYNAMIC_DESCRIPTOR, "true")
                .build();
    }

    @Override
    public NIterator<NId> search(NId id, NDefinitionFilter filter, NPath[] basePaths, NRepository repository) {
        if (!baseIdFilterHelper.accept(id, basePaths)) {
            return null;
        }
        NIdBuilder idBuilder = NIdBuilder.of("org.jedit", "jedit");
        NStream<NId> stream = NPath.of("htmlfs+" + SF_BASE).stream()
                .filter(NPath::isDirectory)
                .map(p -> {
                    String version = p.name();
                    NId candidate = idBuilder.version(version).build();
                    if (getInstallerUrl(candidate.version()) != null) {
                        return candidate;
                    }
                    return null;
                }).nonNull();
        return stream.iterator();
    }

    @Override
    public NPath fetchContent(NId id, NDescriptor descriptor, NRepository repository) {
        if (!baseIdFilterHelper.accept(id)) {
            return null;
        }
        // existence is checked against the plain file-info URL (reliably
        // 404s when absent); the actual bytes are only served through the
        // "/download" mirror-redirect endpoint, so the two are split.
        if (getInstallerUrl(id.version()) == null) {
            return null;
        }
        String downloadUrl = getInstallerDownloadUrl(id.version());
        NPath localPath = NPath.of(ToolboxRepositoryModel.getIdLocalFile(id.builder().faceContent().build(), repository));
        NCp.of().from(NPath.of(downloadUrl)).to(localPath)
                .options(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
        return localPath;
    }

    private String getInstallerUrl(NVersion version) {
        // observed naming for modern releases, e.g. jedit5.7.0install.jar.
        // older releases (pre-4.x / 2.x-3.x eras) may use a different
        // pattern and will simply be skipped here until adjusted. NOTE:
        // this is the file-info page, not the download endpoint - it's
        // only used for existence checks (see getInstallerDownloadUrl).
        String url = SF_BASE + version + "/jedit" + version + "install.jar";
        NPath p = LocalUrlHelper.getOverridePath(url);
        if (p.exists()) {
            return p.toString();
        }
        return null;
    }

    private String getInstallerDownloadUrl(NVersion version) {
        // "/download" is what actually triggers SourceForge's mirror
        // redirect and serves the real bytes - NCp must fetch this, not
        // the bare file-info URL (which is an HTML page, not the jar).
        return SF_BASE + version + "/jedit" + version + "install.jar/download";
    }
}
