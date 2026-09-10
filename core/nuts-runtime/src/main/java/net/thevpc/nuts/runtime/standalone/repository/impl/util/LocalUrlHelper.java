package net.thevpc.nuts.runtime.standalone.repository.impl.util;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.core.NWorkspace;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.log.NLog;
import net.thevpc.nuts.log.NMsgIntent;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NOptional;

public class LocalUrlHelper {
    public static NPath getOverridePath(String localCachePath) {
        if(localCachePath.startsWith("file://")){
            return NPath.of(localCachePath);
        }
        if(localCachePath.startsWith("http://")){
            String s = localCachePath.substring("http://".length());
            NOptional<NPath> k = getLocalPath(s);
            if(k.isPresent()){
                return k.get();
            }
            return NPath.of(localCachePath);
        }
        if(localCachePath.startsWith("https://")){
            String s = localCachePath.substring("https://".length());
            NOptional<NPath> k = getLocalPath(s);
            if(k.isPresent()){
                return k.get();
            }
            return NPath.of(localCachePath);
        }
        return NPath.of(localCachePath);
    }

    public static NOptional<NPath> getLocalPath(String localCachePath) {
        NOptional<NArg> nArgNOptional = NWorkspace.of().bootOptions().customOptionArg("---local-urls");
        if (nArgNOptional.isPresent()) {
            String value = nArgNOptional.get().getStringValue().orNull();
            NPath s = NBlankable.isBlank(value) ? NPath.ofUserHome().resolve(".nuts/local-urls") : NPath.of(value);
            NPath q = s.resolve(localCachePath);
            if (q.exists()) {
                NLog.of(LocalUrlHelper.class).log(NMsg.ofC("lookup proxied url %s as %s (FOUND!)", localCachePath,s).withIntent(NMsgIntent.SUCCESS).asDebug());
                return NOptional.of(q);
            }
            NLog.of(LocalUrlHelper.class).log(NMsg.ofC("lookup proxied url %s as %s (NOT FOUND!)", localCachePath,s).withIntent(NMsgIntent.FAIL).asDebug());
        }
        return NOptional.ofNamedEmpty(NMsg.ofC("local url %s", localCachePath));
    }
}
