package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.cmdline.NCmdLine;

public class BOptionsParser {
    void parseBOptions(BOptions boptions, NCmdLine cmdLine) {

        while (cmdLine.hasNext()) {
            cmdLine.matcher()

                    .when("--optional").asFlag((v) -> boptions.optional = (v.booleanValue()))
                    .when("--app-version").asEntry((v) -> boptions.appVersion = (v.stringValue()))
                    .when("--app-name", "--name").asEntry((v) -> boptions.appName = (v.stringValue()))
                    .when("--app-desc", "--desc").asEntry((v) -> boptions.appDesc = (v.stringValue()))
                    .when("--app-title", "--title").asEntry((v) -> boptions.appTitle = (v.stringValue()))
                    .when("--target").asEntry((v) -> boptions.withTarget = (v.stringValue()))
                    .when("--lib").asEntry((v) -> boptions.lib.add(v.stringValue()))
                    .when("--app").asEntry((v) -> boptions.ids.add(v.stringValue()))
                    .when("--dir", "--as-dir").asTrueFlag((v) -> {
                        boptions.format = BundleType.DIR;
                    })
                    .when("--exploded", "--as-exploded").asTrueFlag((v) -> {
                        boptions.format = BundleType.EXPLODED;
                    })
                    .when("--jar", "--as-jar").asTrueFlag((v) -> {
                        boptions.format = BundleType.JAR;
                    })
                    .when("--zip", "--as-zip").asTrueFlag((v) -> {
                        boptions.format = BundleType.ZIP;
                    })
                    .when("--embedded").asFlag((v) -> boptions.embedded = v.booleanValue())
                    .when("-l","--verbose").asFlag((v) -> boptions.verbose = v.booleanValue())
                    .when("-y", "--yes").asFlag((v) -> boptions.yes = v.booleanValue())
                    .when("-z", "--reset").asFlag((v) -> boptions.reset = v.booleanValue())
                    .when("--clean").asFlag((v) -> boptions.clean = v.booleanValue())
                    .whenNonOption().asArg(v->boptions.ids.add(v.image()))
                    .withDefaults()
                    .require()
            ;
        }
    }

}
