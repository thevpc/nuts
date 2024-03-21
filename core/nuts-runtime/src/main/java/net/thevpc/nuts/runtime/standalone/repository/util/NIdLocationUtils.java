package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.runtime.standalone.repository.impl.AbstractNRepository;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.log.NLogOp;
import net.thevpc.nuts.util.NMsg;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

public class NIdLocationUtils {
    public static NPath fetch(NId id, List<NIdLocation> locations, AbstractNRepository repository, NSession session) {
        for (NIdLocation location : locations) {
            if (CoreFilterUtils.acceptClassifier(location, id.getClassifier())) {
                try {
                    NPath locationPath = NPath.of(location.getUrl(), session);
                    if(locationPath.isLocal()){
                        return locationPath;
                    }else{
                        NPath localPath = NPath.ofTempRepositoryFile(new File(repository.getIdFilename(id, session)).getName(), repository, session);
                        NCp.of(session).from(locationPath).to(localPath).addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
                        return localPath;
                    }
                } catch (Exception ex) {
                    NLogOp.of(NIdLocationUtils.class, session)
                            .level(Level.SEVERE).error(ex)
                            .log(NMsg.ofJ("unable to download location for id {0} in location {1} : {2}", id, location.getUrl(), ex));
                }
            }
        }
        return null;
    }
}
