package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;

public class BOptionsParser {
    void parseBOptions(BOptions boptions, NCmdLine cmdLine) {

        while (cmdLine.hasNext()) {
            if (!cmdLine.withFirst(
                    (arg, c) -> c.with("--optional").nextFlag((v) -> boptions.optional = (v.booleanValue()))
                    , (arg, c) -> c.with("--app-version").nextEntry((v) -> boptions.appVersion = (v.stringValue()))
                    , (arg, c) -> c.with("--app-name", "--name").nextEntry((v) -> boptions.appName = (v.stringValue()))
                    , (arg, c) -> c.with("--app-desc", "--desc").nextEntry((v) -> boptions.appDesc = (v.stringValue()))
                    , (arg, c) -> c.with("--app-title", "--title").nextEntry((v) -> boptions.appTitle = (v.stringValue()))
                    , (arg, c) -> c.with("--target").nextEntry((v) -> boptions.withTarget = (v.stringValue()))
                    , (arg, c) -> c.with("--lib").nextEntry((v) -> boptions.lib.add(v.stringValue()))
                    , (arg, c) -> c.with("--app").nextEntry((v) -> boptions.ids.add(v.stringValue()))
                    , (arg, c) -> c.with("--dir", "--as-dir").nextTrueFlag((v) -> {
                        boptions.format = BundleType.DIR;
                    })
                    , (arg, c) -> c.with("--exploded", "--as-exploded").nextTrueFlag((v) -> {
                        boptions.format = BundleType.EXPLODED;
                    })
                    , (arg, c) -> c.with("--jar", "--as-jar").nextTrueFlag((v) -> {
                        boptions.format = BundleType.JAR;
                    })
                    , (arg, c) -> c.with("--zip", "--as-zip").nextTrueFlag((v) -> {
                        boptions.format = BundleType.ZIP;
                    })
                    , (arg, c) -> c.with("--embedded").nextFlag((v) -> boptions.embedded = v.booleanValue())
                    , (arg, c) -> c.with("--verbose").nextFlag((v) -> boptions.verbose = v.booleanValue())
                    , (arg, c) -> c.with("-y", "--yes").nextFlag((v) -> boptions.yes = v.booleanValue())
                    , (arg, c) -> c.with("-z", "--reset").nextFlag((v) -> boptions.reset = v.booleanValue())
                    , (arg, c) -> c.with("--clean").nextFlag((v) -> boptions.clean = v.booleanValue())
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
