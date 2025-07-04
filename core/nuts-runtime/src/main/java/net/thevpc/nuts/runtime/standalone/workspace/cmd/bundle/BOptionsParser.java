package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.cmdline.NCmdLine;

public class BOptionsParser {
    void parseBOptions(BOptions boptions, NCmdLine cmdLine) {

        while (cmdLine.hasNext()) {
            cmdLine.matcher()

                    .with("--optional").matchFlag((v) -> boptions.optional = (v.booleanValue()))
                    .with("--app-version").matchEntry((v) -> boptions.appVersion = (v.stringValue()))
                    .with("--app-name", "--name").matchEntry((v) -> boptions.appName = (v.stringValue()))
                    .with("--app-desc", "--desc").matchEntry((v) -> boptions.appDesc = (v.stringValue()))
                    .with("--app-title", "--title").matchEntry((v) -> boptions.appTitle = (v.stringValue()))
                    .with("--target").matchEntry((v) -> boptions.withTarget = (v.stringValue()))
                    .with("--lib").matchEntry((v) -> boptions.lib.add(v.stringValue()))
                    .with("--app").matchEntry((v) -> boptions.ids.add(v.stringValue()))
                    .with("--dir", "--as-dir").matchTrueFlag((v) -> {
                        boptions.format = BundleType.DIR;
                    })
                    .with("--exploded", "--as-exploded").matchTrueFlag((v) -> {
                        boptions.format = BundleType.EXPLODED;
                    })
                    .with("--jar", "--as-jar").matchTrueFlag((v) -> {
                        boptions.format = BundleType.JAR;
                    })
                    .with("--zip", "--as-zip").matchTrueFlag((v) -> {
                        boptions.format = BundleType.ZIP;
                    })
                    .with("--embedded").matchFlag((v) -> boptions.embedded = v.booleanValue())
                    .with("--verbose").matchFlag((v) -> boptions.verbose = v.booleanValue())
                    .with("-y", "--yes").matchFlag((v) -> boptions.yes = v.booleanValue())
                    .with("-z", "--reset").matchFlag((v) -> boptions.reset = v.booleanValue())
                    .with("--clean").matchFlag((v) -> boptions.clean = v.booleanValue())
                    .withNonOption().matchAny(v->boptions.ids.add(v.getImage()))
                    .requireWithDefault();
        }
    }

}
