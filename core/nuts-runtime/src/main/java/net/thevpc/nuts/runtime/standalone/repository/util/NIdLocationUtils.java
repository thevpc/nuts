package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.artifact.NIdLocation;
import net.thevpc.nuts.io.NCp;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPathOption;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.runtime.standalone.repository.impl.AbstractNRepository;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;

import net.thevpc.nuts.text.NMsg;

import java.io.File;
import java.util.List;

public class NIdLocationUtils {
    public static NPath fetch(NId id, List<NIdLocation> locations, AbstractNRepository repository) {
        for (NIdLocation location : locations) {
            if (CoreFilterUtils.acceptClassifier(location, id.getClassifier())) {
                try {
                    NPath locationPath = NPath.of(location.getUrl());
                    if(locationPath.isLocal()){
                        return locationPath;
                    }else{
                        NPath localPath = NPath.ofTempRepositoryFile(new File(repository.getIdFilename(id)).getName(), repository);
                        NCp.of().from(locationPath).to(localPath).addOptions(NPathOption.SAFE, NPathOption.LOG, NPathOption.TRACE).run();
                        return localPath;
                    }
                } catch (Exception ex) {
                    NLog.of(NIdLocationUtils.class)
                            .log(NMsg.ofC("unable to download location for id %s in location %s : %s", id, location.getUrl(), ex).asError(ex));
                }
            }
        }
        return null;
    }
}
