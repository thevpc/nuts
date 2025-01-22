/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NMsg;

/**
 * @author vpc
 */
public class ReposRunner extends AbstractRunner {

    boolean repoPreview = false;
    boolean repoPublic = false;

    @Override
    public void configureAfterOptions() {

    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "build-repo-nuts-preview": {
                cmdLine.withNextFlag((v, a) -> repoPreview = v);
                return true;
            }
            case "build-repo-nuts-public": {
                cmdLine.withNextFlag((v, a) -> repoPublic = v);
                return true;
            }
        }
        return false;
    }

    public ReposRunner() {
        super();
    }

    @Override
    public void configureBeforeOptions(NCmdLine cmdLine) {
        //do nothing
    }

    @Override
    public void run() {
        if (repoPreview) {
            echoV("**** $v (nuts settings update stats)...", NMaps.of("v", NMsg.ofStyledKeyword("build-nuts-preview")));
            NExecCmd.of()
                    .addCommand("settings", "update", "stats")
                    .addCommand(context().nutsRootFolder.resolve("../nuts-preview"))
                    .failFast()
                    .run();
        }
        if (repoPublic) {
            echoV("**** $v (nuts settings update stats)...", NMaps.of("v", NMsg.ofStyledKeyword("build-nuts-public")));
            NExecCmd.of()
                    .addCommand("settings", "update", "stats")
                    .addCommand(context().nutsRootFolder.resolve("../nuts-public"))
                    .failFast()
                    .run();
        }
    }

}
