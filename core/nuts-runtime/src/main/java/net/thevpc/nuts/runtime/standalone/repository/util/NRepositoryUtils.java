package net.thevpc.nuts.runtime.standalone.repository.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.reserved.parser.NReservedJsonParser;
import net.thevpc.nuts.reserved.io.NReservedPath;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NLogVerb;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NMsg;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.util.Map;
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

    public static class RepoExtraInfos {

    }

    public static NRepositoryLocation validateLocation(NRepositoryLocation r, NLog nLog) {
        if (NBlankable.isBlank(r.getLocationType()) || NBlankable.isBlank(r.getName())) {
            if (r.getFullLocation() != null) {
                NReservedPath r1 = new NReservedPath(r.getPath()).toAbsolute();
                if (!Objects.equals(r.getPath(),r1.getPath())) {
                    r = r.setPath(r1.getPath());
                }
                NReservedPath r2 = r1.resolve(".nuts-repository");
                NReservedJsonParser parser = null;
                boolean fileExists = false;
                try {
                    byte[] bytes = r2.readAllBytes(nLog);
                    if (bytes != null) {
                        fileExists = true;
                        parser = new NReservedJsonParser(new InputStreamReader(new ByteArrayInputStream(bytes)));
                        Map<String, Object> jsonObject = parser.parseObject();
                        if (NBlankable.isBlank(r.getLocationType())) {
                            Object o = jsonObject.get("repositoryType");
                            if (o instanceof String && !NBlankable.isBlank(o)) {
                                r = r.setLocationType(String.valueOf(o));
                            }
                        }
                        if (NBlankable.isBlank(r.getName())) {
                            Object o = jsonObject.get("repositoryName");
                            if (o instanceof String && !NBlankable.isBlank(o)) {
                                r = r.setName(String.valueOf(o));
                            }
                        }
                        if (NBlankable.isBlank(r.getName())) {
                            r = r.setName(r.getName());
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
