package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.NWorkspace;
import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNCmdLines;
import net.thevpc.nuts.runtime.standalone.format.DefaultNObjectFormat;

public class NativeImageHelper {
    public static void prepare(NWorkspace workspace){
        if(Boolean.getBoolean("EnableGraalVM")){
            new DefaultNCmdLines(workspace);
            new DefaultNObjectFormat(workspace);
        }
    }
}
