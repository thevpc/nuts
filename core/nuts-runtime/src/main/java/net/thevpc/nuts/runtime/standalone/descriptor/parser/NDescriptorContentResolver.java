package net.thevpc.nuts.runtime.standalone.descriptor.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNDescriptorContentParserContext;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NDescriptorContentParserComponent;
import net.thevpc.nuts.spi.NDescriptorContentParserContext;

import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.util.NMsg;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NDescriptorContentResolver {
    /**
     * @param localPath    localPath
     * @param parseOptions may include --all-mains to force lookup of all main
     *                     classes if available
     * @return descriptor
     */
    public static NDescriptor resolveNutsDescriptorFromFileContent(Path localPath, List<String> parseOptions) {
        if (parseOptions == null) {
            parseOptions = new ArrayList<>();
        }
        if (localPath != null) {
            String fileExtension = NIOUtils.getFileExtension(localPath.getFileName().toString());
            NDescriptorContentParserContext ctx = new DefaultNDescriptorContentParserContext(
                    localPath, fileExtension, null, parseOptions);
            List<NDescriptorContentParserComponent> allParsers = NExtensions.of()
                    .createComponents(NDescriptorContentParserComponent.class, ctx);
            if (allParsers.size() > 0) {
                for (NDescriptorContentParserComponent parser : allParsers) {
                    NDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        NLog.of(CoreIOUtils.class)

                                .log(NMsg.ofC("error parsing %s with %s", localPath, parser.getClass().getSimpleName() + ". Error ignored").asFine(e)
                                        .withIntent(NMsgIntent.ALERT)
                                );
                        //e.printStackTrace();
                    }
                    if (desc != null) {
                        if (!desc.isBlank()) {
                            return desc;
                        }
                        return NDescriptorUtils.checkDescriptor(desc);
                    }
                }
            }
        }
        return null;
    }
}
