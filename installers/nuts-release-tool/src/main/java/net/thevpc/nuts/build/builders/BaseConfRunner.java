/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package net.thevpc.nuts.build.builders;

import net.thevpc.nuts.*;
import net.thevpc.nuts.build.util.*;
import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.cmdline.NCmdLine;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NMsg;

/**
 * @author vpc
 */
public class BaseConfRunner extends AbstractRunner {


    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "--root": {
                cmdLine.withNextEntry((v, a) -> {
                    //already processed
                    //context().root = NPath.of(v).toAbsolute().normalize()
                });
                return true;
            }
            case "--conf": {
                cmdLine.withNextEntry((v, a) -> context().loadConfig(NPath.of(v), cmdLine));
                return true;
            }
            case "--debug": {
                cmdLine.withNextFlag((v, a)
                        -> context().nutsDebugArg = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
                );
                return true;
            }
            case "--trace": {
                cmdLine.withNextFlag((v, a) -> context().trace = v);
                return true;
            }
            case "--verbose": {
                cmdLine.withNextFlag((v, a) -> context().verbose = v);
                return true;
            }
            // actions
            case "publish": {
                cmdLine.withNextFlag((v, a) -> context().publish = v);
                return true;
            }
        }
        return false;
    }

    public BaseConfRunner() {
        super();
    }

    @Override
    public void configureBeforeOptions(NCmdLine cmdLine) {
        cmdLine.lookupNextEntry((a, c) -> {
            NPath newRoot = NPath.of(a).toAbsolute().normalize();
            NReleaseUtils.ensureNutsRepoFolder(newRoot);
            context().nutsRootFolder = newRoot;
        }, "--root");
        if (context().nutsRootFolder == null) {
            NPath newRoot = NPath.ofUserDirectory();
            NReleaseUtils.ensureNutsRepoFolder(newRoot);
            context().nutsRootFolder = newRoot;
        }
        NPath conf = context().nutsRootFolder.resolve("installers/nuts-release-tool/nuts-release.conf");
        if (conf.exists()) {
            context().loadConfig(conf, cmdLine);
        } else {
            throw new NIllegalArgumentException(NMsg.ofC("missing %s", conf));
        }
        context().websiteProjectFolder = context().nutsRootFolder.resolve("documentation/website");
        context().repositoryProjectFolder = context().nutsRootFolder.resolve("documentation/repo");
        context().setVar("root", context().nutsRootFolder.toString());
    }

    @Override
    public void configureAfterOptions() {
        NAssert.requireNonBlank(context().nutsStableApiVersion, "nutsStableApiVersion");
        NAssert.requireNonBlank(context().nutsStableAppVersion, "nutsStableAppVersion");
        NAssert.requireNonBlank(context().nutsStableRuntimeVersion, "nutsStableRuntimeVersion");
    }

    @Override
    public void run() {

    }


}
