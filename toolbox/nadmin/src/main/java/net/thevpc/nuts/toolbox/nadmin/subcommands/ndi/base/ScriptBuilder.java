package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfoType;

public interface ScriptBuilder {
    static FromTemplateScriptBuilder fromTemplate(NdiScriptInfoType type, NutsId anyId, BaseSystemNdi sndi) {
        return new FromTemplateScriptBuilder(type, anyId, sndi, sndi.context.getSession());
    }

    String buildString();

    PathInfo build();

}
