package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import java.util.List;
import java.util.logging.Level;

public class NutsIdLocationUtils {
    public static boolean fetch(NutsId id, List<NutsIdLocation> locations, String localFile, NutsSession session) {
        for (NutsIdLocation location : locations) {
            if (CoreFilterUtils.acceptClassifier(location, id.getClassifier())) {
                try {
                    NutsCp.of(session).from(location.getUrl()).to(localFile).addOptions(NutsPathOption.SAFE, NutsPathOption.LOG, NutsPathOption.TRACE).run();
                    return true;
                } catch (Exception ex) {
                    NutsLoggerOp.of(NutsIdLocationUtils.class, session)
                            .level(Level.SEVERE).error(ex)
                            .log(NutsMessage.jstyle("unable to download location for id {0} in location {1} : {2}", id, location.getUrl(), ex));
                }
            }
        }
        return false;
    }
}
