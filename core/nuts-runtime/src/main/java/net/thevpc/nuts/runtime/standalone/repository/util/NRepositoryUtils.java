package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.elem.NElementParser;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.elem.NObjectElement;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.util.Objects;
import java.util.logging.Level;

public class NRepositoryUtils {
    public static NRepositoryRef optionsToRef(NAddRepositoryOptions options) {
        return new NRepositoryRef()
                .setEnabled(options.isEnabled())
                .setFailSafe(options.isFailSafe())
                .setName(options.getName())
                .setLocation(options.getLocation())
                .setDeployWeight(options.getDeployWeight());
    }

    public static NAddRepositoryOptions refToOptions(NRepositoryRef ref) {
        return new NAddRepositoryOptions()
                .setEnabled(ref.isEnabled())
                .setFailSafe(ref.isFailSafe())
                .setName(ref.getName())
                .setLocation(ref.getLocation())
                .setDeployWeight(ref.getDeployWeight())
                .setTemporary(false);
    }

    public static NRepositoryLocation validateLocation(NRepositoryLocation r, NLog nLog) {
        if (NBlankable.isBlank(r.getLocationType()) /*|| NBlankable.isBlank(r.getName())*/) {
            if (r.getFullLocation() != null) {
                NPath r1 = NPath.of(r.getPath()).toAbsolute();
                if (!Objects.equals(r.getPath(),r1.toString())) {
                    r = r.setPath(r1.toString());
                }
                NPath r2 = r1.resolve(".nuts-repository");
                boolean fileExists = false;
                try {
                    if(!r2.exists()){
                        if (nLog != null) {
                            nLog.with().level(Level.CONFIG).verb(NLogVerb.WARNING).log(NMsg.ofC("unable to load %s", r2));
                        }
                    }else {
                        byte[] bytes = r2.readBytes();
                        if (bytes != null) {
                            fileExists = true;
                            NObjectElement jsonObject = NElementParser.ofJson().parse(bytes).asObject().get();
                            if (NBlankable.isBlank(r.getLocationType())) {
                                String o = jsonObject.getStringValue("repositoryType").orNull();
                                if (!NBlankable.isBlank(o)) {
                                    r = r.setLocationType(String.valueOf(o));
                                }
                            }
                            if (NBlankable.isBlank(r.getName())) {
                                String o = jsonObject.getStringValue("repositoryName").orNull();
                                if (!NBlankable.isBlank(o)) {
                                    r = r.setName(String.valueOf(o));
                                }
                            }
                            if (NBlankable.isBlank(r.getName())) {
                                r = r.setName(r.getName());
                            }
                        }
                    }
                } catch (Exception e) {
                    if (nLog != null) {
                        nLog.with().level(Level.CONFIG).verb(NLogVerb.WARNING).log(NMsg.ofC("unable to load %s", r2));
                    }
                }
                if (fileExists) {
                    if (NBlankable.isBlank(r.getLocationType())) {
                        r = r.setLocationType(NConstants.RepoTypes.NUTS);
                    }
                }
                if (NBlankable.isBlank(r.getLocationType())) {
                    NPath p = NPath.of(r.getPath());
                    if (p.isLocal()) {
                        if (!p.exists() || p.isDirectory()) {
                            r = r.setLocationType(NConstants.RepoTypes.NUTS);
                        }
                    }
                }
            }
        }
        return r;
    }

    public static String getRepoType(NRepositoryConfig conf) {
        if (conf != null) {
            NRepositoryLocation loc = conf.getLocation();
            if (loc != null) {
                loc = validateLocation(loc, null);
                if (!NBlankable.isBlank(loc.getLocationType())) {
                    return loc.getLocationType();
                }
            }
        }
        return null;
    }
}
