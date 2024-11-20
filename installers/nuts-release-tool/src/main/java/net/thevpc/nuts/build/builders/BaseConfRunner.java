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
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

/**
 * @author vpc
 */
public class BaseConfRunner extends AbstractRunner {

    @Override
    public void configureAfterOptions() {
        if (NBlankable.isBlank(context().root)) {
            context().root = NPath.ofUserDirectory().normalize();
        }
        if (!isValidRoot(context().root)) {
            throw new NIllegalArgumentException(NMsg.ofC("invalid nuts repository root %s", context().root));
        }
    }

    private boolean isValidRoot(NPath NUTS_ROOT_BASE) {
        if (!NUTS_ROOT_BASE.resolve("pom.xml").isRegularFile()) {
            return false;
        }
        NId id = NDescriptorParser.of()
                .setDescriptorStyle(NDescriptorStyle.MAVEN)
                .parse(NUTS_ROOT_BASE.resolve("pom.xml")).get().getId();
        if (!id.getShortName().endsWith("net.thevpc.nuts.builders:nuts-builder")) {
            return false;
        }
        return true;
    }

    @Override
    public boolean configureFirst(NCmdLine cmdLine) {
        NArg c = cmdLine.peek().orNull();
        switch (c.key()) {
            case "--root": {
                cmdLine.withNextEntry((v, a) -> context().root = NPath.of(v).toAbsolute().normalize());
                return true;
            }
            case "--conf": {
                cmdLine.withNextEntry((v, a) -> context().loadConfig(NPath.of(v), cmdLine));
                return true;
            }
            case "--debug": {
                cmdLine.withNextFlag((v, a)
                        -> context().NUTS_DEBUG_ARG = "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"
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

    public BaseConfRunner(NSession session) {
        super(session);
    }

    @Override
    public void configureBeforeOptions(NCmdLine cmdLine) {
        NPath conf = NPath.of("nuts-release.conf");
        if (conf.exists()) {
            context().loadConfig(conf, cmdLine);
        }
    }

    @Override
    public void run() {
    }


}
