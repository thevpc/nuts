package net.thevpc.nuts.runtime.standalone.descriptor.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNDescriptorContentParserContext;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NDescriptorContentParserComponent;
import net.thevpc.nuts.spi.NDescriptorContentParserContext;
import net.thevpc.nuts.util.NLogOp;
import net.thevpc.nuts.util.NLogVerb;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class NDescriptorContentResolver {
    /**
     * @param localPath    localPath
     * @param parseOptions may include --all-mains to force lookup of all main
     *                     classes if available
     * @param session      session
     * @return descriptor
     */
    public static NDescriptor resolveNutsDescriptorFromFileContent(Path localPath, List<String> parseOptions, NSession session) {
        if (parseOptions == null) {
            parseOptions = new ArrayList<>();
        }
        if (localPath != null) {
            String fileExtension = CoreIOUtils.getFileExtension(localPath.getFileName().toString());
            NDescriptorContentParserContext ctx = new DefaultNDescriptorContentParserContext(session,
                    localPath, fileExtension, null, parseOptions);
            List<NDescriptorContentParserComponent> allParsers = session.extensions()
                    .setSession(session)
                    .createComponents(NDescriptorContentParserComponent.class, ctx);
            if (allParsers.size() > 0) {
                for (NDescriptorContentParserComponent parser : allParsers) {
                    NDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        NLogOp.of(CoreIOUtils.class, session)
                                .level(Level.FINE)
                                .verb(NLogVerb.WARNING)
                                .error(e)
                                .log(NMsg.ofC("error parsing %s with %s", localPath, parser.getClass().getSimpleName() + ". Error ignored"));
                        //e.printStackTrace();
                    }
                    if (desc != null) {
                        if (!desc.isBlank()) {
                            return desc;
                        }
                        return NDescriptorUtils.checkDescriptor(desc, session);
                    }
                }
            }
        }
        return null;
    }
}
