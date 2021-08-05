package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfoType;

public interface ScriptBuilder {
    static FromTemplateScriptBuilder fromTemplate(String templateName,NdiScriptInfoType type, NutsId anyId, BaseSystemNdi sndi,NutsEnvInfo env) {
        return new FromTemplateScriptBuilder(templateName,type, anyId, sndi, env,sndi.context.getSession());
    }

    static SimpleScriptBuilder simple(NdiScriptInfoType type, NutsId anyId, BaseSystemNdi sndi) {
        return new SimpleScriptBuilder(type, anyId, sndi,sndi.context.getSession());
    }

    String buildString();

    PathInfo build();

}
