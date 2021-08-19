package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptInfoType;

public interface ScriptBuilder {
    static FromTemplateScriptBuilder fromTemplate(String templateName, NdiScriptInfoType type, NutsId anyId, BaseSystemNdi sndi, NutsEnvInfo env) {
        return new FromTemplateScriptBuilder(templateName,type, anyId, sndi, env,sndi.session);
    }

    static SimpleScriptBuilder simple(NdiScriptInfoType type, NutsId anyId, BaseSystemNdi sndi) {
        return new SimpleScriptBuilder(type, anyId, sndi,sndi.session);
    }

    String buildString();

    PathInfo build();

}
