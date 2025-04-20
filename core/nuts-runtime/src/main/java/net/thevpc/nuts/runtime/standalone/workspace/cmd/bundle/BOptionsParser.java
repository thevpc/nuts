package net.thevpc.nuts.runtime.standalone.workspace.cmd.bundle;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.cmdline.NCmdLine;

public class BOptionsParser {
    void parseBOptions(BOptions boptions, NCmdLine cmdLine) {
        while (cmdLine.hasNext()) {
            if(!cmdLine.withFirst(
                    c->c.with("--optional").consumeFlag((v, ar) -> boptions.optional = (v))
                    ,c->c.with("--app-version").consumeEntry((v, ar) -> boptions.appVersion = (v))
                    ,c->c.with("--app-name","--name").consumeEntry((v, ar) -> boptions.appName = (v))
                    ,c->c.with("--app-desc","--desc").consumeEntry((v, ar) -> boptions.appDesc = (v))
                    ,c->c.with("--app-title","--title").consumeEntry((v, ar) -> boptions.appTitle = (v))
                    ,c->c.with("--target").consumeEntry((v, ar) -> boptions.withTarget = (v))
                    ,c->c.with("--lib").consumeEntry((v, ar) -> boptions.lib.add(v))
                    ,c->c.with("--app").consumeEntry((v, ar) -> boptions.ids.add(v))
                    ,c->c.with("--dir","--as-dir").consumeFlag((v, ar) -> {if(v)boptions.format = BundleType.DIR ;})
                    ,c->c.with("--exploded","--as-exploded").consumeFlag((v, ar) -> {if(v)boptions.format = BundleType.EXPLODED ;})
                    ,c->c.with("--jar","--as-jar").consumeFlag((v, ar) -> {if(v)boptions.format = BundleType.JAR;})
                    ,c->c.with("--zip","--as-zip").consumeFlag((v, ar) -> {if(v)boptions.format = BundleType.ZIP ;})
                    ,c->c.with("--embedded").consumeFlag((v, ar) -> boptions.embedded = v)
                    ,c->c.with("--verbose").consumeFlag((v, ar) -> boptions.verbose = v)
                    ,c->c.with("-y","--yes").consumeFlag((v, ar) -> boptions.yes = v)
                    ,c->c.with("-z","--reset").consumeFlag((v, ar) -> boptions.reset = v)
                    ,c->c.with("--clean").consumeFlag((v, ar) -> boptions.clean = v)
            )){
                if (cmdLine.peekNonOption().isPresent()) {
                    boptions.ids.add(cmdLine.next().get().toString());
                }else {
                    NSession.of().configureLast(cmdLine);
                }
            }
        }
    }

}
