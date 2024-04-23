package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.NSession;
import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNCmdLines;
import net.thevpc.nuts.runtime.standalone.format.DefaultNObjectFormat;

public class NativeImageHelper {
    public static void prepare(NSession session){
        if(Boolean.getBoolean("EnableGraalVM")){
            new DefaultNCmdLines(session);
            new DefaultNObjectFormat(session);
        }
    }
}
