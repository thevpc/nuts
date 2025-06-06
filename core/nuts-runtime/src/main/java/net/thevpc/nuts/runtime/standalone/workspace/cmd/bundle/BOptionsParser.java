package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.cmdline.NCmdLineConsumer;
import net.thevpc.nuts.cmdline.NCmdLineContext;

public class BOptionsParser {
    void parseBOptions(BOptions boptions, NCmdLine cmdLine) {

        while (cmdLine.hasNext()) {
            if (!cmdLine.withFirst(
                    c -> c.with("--optional").nextFlag((v, ar) -> boptions.optional = (v))
                    , c -> c.with("--app-version").nextEntry((v, ar) -> boptions.appVersion = (v))
                    , c -> c.with("--app-name", "--name").nextEntry((v, ar) -> boptions.appName = (v))
                    , c -> c.with("--app-desc", "--desc").nextEntry((v, ar) -> boptions.appDesc = (v))
                    , c -> c.with("--app-title", "--title").nextEntry((v, ar) -> boptions.appTitle = (v))
                    , c -> c.with("--target").nextEntry((v, ar) -> boptions.withTarget = (v))
                    , c -> c.with("--lib").nextEntry((v, ar) -> boptions.lib.add(v))
                    , c -> c.with("--app").nextEntry((v, ar) -> boptions.ids.add(v))
                    , c -> c.with("--dir", "--as-dir").nextFlag((v, ar) -> {
                        if (v) boptions.format = BundleType.DIR;
                    })
                    , c -> c.with("--exploded", "--as-exploded").nextFlag((v, ar) -> {
                        if (v) boptions.format = BundleType.EXPLODED;
                    })
                    , c -> c.with("--jar", "--as-jar").nextFlag((v, ar) -> {
                        if (v) boptions.format = BundleType.JAR;
                    })
                    , c -> c.with("--zip", "--as-zip").nextFlag((v, ar) -> {
                        if (v) boptions.format = BundleType.ZIP;
                    })
                    , c -> c.with("--embedded").nextFlag((v, ar) -> boptions.embedded = v)
                    , c -> c.with("--verbose").nextFlag((v, ar) -> boptions.verbose = v)
                    , c -> c.with("-y", "--yes").nextFlag((v, ar) -> boptions.yes = v)
                    , c -> c.with("-z", "--reset").nextFlag((v, ar) -> boptions.reset = v)
                    , c -> c.with("--clean").nextFlag((v, ar) -> boptions.clean = v)
            )) {
                if (cmdLine.peekNonOption().isPresent()) {
                    boptions.ids.add(cmdLine.next().get().toString());
                } else {
                    NSession.of().configureLast(cmdLine);
                }
            }
        }
    }

}
