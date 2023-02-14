package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.util.NLogOp;

import java.util.List;
import java.util.logging.Level;

public class NIdLocationUtils {
    public static boolean fetch(NId id, List<NIdLocation> locations, String localFile, NSession session) {
        for (NIdLocation location : locations) {
            if (CoreFilterUtils.acceptClassifier(location, id.getClassifier())) {
                try {
                    NCp.of(session).from(NPath.of(location.getUrl(),session)).to(NPath.of(localFile,session)).addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
                    return true;
                } catch (Exception ex) {
                    NLogOp.of(NIdLocationUtils.class, session)
                            .level(Level.SEVERE).error(ex)
                            .log(NMsg.ofJ("unable to download location for id {0} in location {1} : {2}", id, location.getUrl(), ex));
                }
            }
        }
        return false;
    }
}
