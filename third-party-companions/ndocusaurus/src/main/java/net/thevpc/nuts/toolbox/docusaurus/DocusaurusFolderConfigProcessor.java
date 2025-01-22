package net.thevpc.nuts.toolbox.docusaurus;

import net.thevpc.nuts.elem.NArrayElement;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.mimetype.MimeTypeConstants;
import net.thevpc.nuts.lib.doc.processor.NDocProcessor;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.util.FileProcessorUtils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Objects;

class DocusaurusFolderConfigProcessor implements NDocProcessor {
    private final DocusaurusCtrl docusaurusCtrl;

    public DocusaurusFolderConfigProcessor(DocusaurusCtrl docusaurusCtrl) {
        this.docusaurusCtrl = docusaurusCtrl;
    }

    @Override
    public void processStream(InputStream source, OutputStream target, NDocContext context) {
        throw new IllegalArgumentException("Unsupported");
    }

    @Override
    public void processPath(NPath source, String mimeType, NDocContext context) {
        NObjectElement config = DocusaurusFolder.ofFolder(source.getParent(),
                        NPath.of(context.getRootDirRequired()).resolve("docs"),
                        docusaurusCtrl.getPreProcessorBaseDir().resolve("src"),
                        0)
                .getConfig().getObject("type").get();
        if (
                "javadoc".equals(config.getString("name").orNull())
                        || "doc".equals(config.getString("name").orNull())
        ) {
            String[] sources = config.getArray("sources").orElse(NArrayElement.ofEmpty())
                    .stream().map(x -> x.asString().orElse(null))
                    .filter(Objects::nonNull).toArray(String[]::new);
            if (sources.length == 0) {
                throw new IllegalArgumentException("missing doc sources in " + source);
            }
            String[] packages = config.getArray("packages").orElse(NArrayElement.ofEmpty())
                    .stream().map(x -> x.asString().orNull()).filter(Objects::nonNull).toArray(String[]::new);
            String target = context.getPathTranslator().translatePath(source.getParent().toString());
            if (target == null) {
                throw new IllegalArgumentException("invalid source " + source.getParent());
            }
            ArrayList<String> cmd = new ArrayList<>();
//                cmd.add("--bot");
            cmd.add("ndoc" + ((docusaurusCtrl.getNdocVersion() == null || docusaurusCtrl.getNdocVersion().isEmpty()) ? "" : "#" + (docusaurusCtrl.getNdocVersion())));
            cmd.add("--backend=docusaurus");
            for (String s : sources) {
                s = context.getProcessorManager().processString(s, MimeTypeConstants.PLACEHOLDER_DOLLAR);
                cmd.add("--source");
                cmd.add(FileProcessorUtils.toAbsolutePath(NPath.of(s), source.getParent()).toString());
            }
            for (String s : packages) {
                s = context.getProcessorManager().processString(s, MimeTypeConstants.PLACEHOLDER_DOLLAR);
                cmd.add("--package");
                cmd.add(s);
            }
            cmd.add("--target");
            cmd.add(target);
            FileProcessorUtils.mkdirs(NPath.of(target));
            docusaurusCtrl.runCommand(NPath.of(target), docusaurusCtrl.isAutoInstallNutsPackages(), cmd.toArray(new String[0]));
        }

    }

    @Override
    public String toString() {
        return "DocusaurusFolderConfig";
    }
}
