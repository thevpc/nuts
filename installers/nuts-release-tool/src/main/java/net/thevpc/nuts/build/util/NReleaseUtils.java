package net.thevpc.nuts.build.util;

import net.thevpc.nuts.NDescriptor;
import net.thevpc.nuts.NDescriptorParser;
import net.thevpc.nuts.NDescriptorStyle;
import net.thevpc.nuts.NIllegalArgumentException;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

public class NReleaseUtils {
    public static void ensureNutsRepoFolder(NPath currentDir) {
        if (!isNutsRepoFolder(currentDir)) {
            throw new NIllegalArgumentException(NMsg.ofC("you must run release tool under nuts github repository root folder. Now we are under %s", currentDir));
        }
    }

    public static boolean isNutsRepoFolder(NPath currentDir) {
        if (!currentDir.isDirectory()) {
            return false;
        }
        if (!currentDir.resolve("pom.xml").isRegularFile()) {
            return false;
        }
        NOptional<NDescriptor> desc = NDescriptorParser.of().setDescriptorStyle(NDescriptorStyle.MAVEN).parse(currentDir.resolve("pom.xml"));
        if (!desc.isPresent()) {
            return false;
        }
        if (!desc.get().getId().getShortName().equals("net.thevpc.nuts.builders:nuts-builder")) {
            return false;
        }
        if (!currentDir.resolve("documentation/website").isDirectory()) {
            return false;
        }
        if (!currentDir.resolve("documentation/repo").isDirectory()) {
            return false;
        }
        return true;
    }
}
