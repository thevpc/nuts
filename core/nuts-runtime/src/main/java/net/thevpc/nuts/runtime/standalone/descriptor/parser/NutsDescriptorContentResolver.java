package net.thevpc.nuts.runtime.standalone.descriptor.parser;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.descriptor.DefaultNutsDescriptorContentParserContext;
import net.thevpc.nuts.runtime.standalone.descriptor.util.NutsDescriptorUtils;
import net.thevpc.nuts.runtime.standalone.io.util.CoreIOUtils;
import net.thevpc.nuts.spi.NutsDescriptorContentParserComponent;
import net.thevpc.nuts.spi.NutsDescriptorContentParserContext;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class NutsDescriptorContentResolver {
    /**
     * @param localPath    localPath
     * @param parseOptions may include --all-mains to force lookup of all main
     *                     classes if available
     * @param session      session
     * @return descriptor
     */
    public static NutsDescriptor resolveNutsDescriptorFromFileContent(Path localPath, List<String> parseOptions, NutsSession session) {
        if (parseOptions == null) {
            parseOptions = new ArrayList<>();
        }
        if (localPath != null) {
            String fileExtension = CoreIOUtils.getFileExtension(localPath.getFileName().toString());
            NutsDescriptorContentParserContext ctx = new DefaultNutsDescriptorContentParserContext(session,
                    localPath, fileExtension, null, parseOptions);
            List<NutsDescriptorContentParserComponent> allParsers = session.extensions()
                    .setSession(session)
                    .createAllSupported(NutsDescriptorContentParserComponent.class, ctx);
            if (allParsers.size() > 0) {
                for (NutsDescriptorContentParserComponent parser : allParsers) {
                    NutsDescriptor desc = null;
                    try {
                        desc = parser.parse(ctx);
                    } catch (Exception e) {
                        NutsLoggerOp.of(CoreIOUtils.class, session)
                                .level(Level.FINE)
                                .verb(NutsLogVerb.WARNING)
                                .error(e)
                                .log(NutsMessage.cstyle("error parsing %s with %s", localPath, parser.getClass().getSimpleName() + ". Error ignored"));
                        //e.printStackTrace();
                    }
                    if (desc != null) {
                        if (!desc.isBlank()) {
                            return desc;
                        }
                        return NutsDescriptorUtils.checkDescriptor(desc, session);
                    }
                }
            }
        }
        return null;
    }
}
