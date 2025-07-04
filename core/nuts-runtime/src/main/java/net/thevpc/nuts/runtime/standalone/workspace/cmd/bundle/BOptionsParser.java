package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.cmdline.NCmdLine;

public class BOptionsParser {
    void parseBOptions(BOptions boptions, NCmdLine cmdLine) {

        while (cmdLine.hasNext()) {
            cmdLine.selector()

                    .with("--optional").nextFlag((v) -> boptions.optional = (v.booleanValue()))
                    .with("--app-version").nextEntry((v) -> boptions.appVersion = (v.stringValue()))
                    .with("--app-name", "--name").nextEntry((v) -> boptions.appName = (v.stringValue()))
                    .with("--app-desc", "--desc").nextEntry((v) -> boptions.appDesc = (v.stringValue()))
                    .with("--app-title", "--title").nextEntry((v) -> boptions.appTitle = (v.stringValue()))
                    .with("--target").nextEntry((v) -> boptions.withTarget = (v.stringValue()))
                    .with("--lib").nextEntry((v) -> boptions.lib.add(v.stringValue()))
                    .with("--app").nextEntry((v) -> boptions.ids.add(v.stringValue()))
                    .with("--dir", "--as-dir").nextTrueFlag((v) -> {
                        boptions.format = BundleType.DIR;
                    })
                    .with("--exploded", "--as-exploded").nextTrueFlag((v) -> {
                        boptions.format = BundleType.EXPLODED;
                    })
                    .with("--jar", "--as-jar").nextTrueFlag((v) -> {
                        boptions.format = BundleType.JAR;
                    })
                    .with("--zip", "--as-zip").nextTrueFlag((v) -> {
                        boptions.format = BundleType.ZIP;
                    })
                    .with("--embedded").nextFlag((v) -> boptions.embedded = v.booleanValue())
                    .with("--verbose").nextFlag((v) -> boptions.verbose = v.booleanValue())
                    .with("-y", "--yes").nextFlag((v) -> boptions.yes = v.booleanValue())
                    .with("-z", "--reset").nextFlag((v) -> boptions.reset = v.booleanValue())
                    .with("--clean").nextFlag((v) -> boptions.clean = v.booleanValue())
                    .withNonOption().next(v->boptions.ids.add(v.getImage()))
                    .requireWithDefault();
        }
    }

}
