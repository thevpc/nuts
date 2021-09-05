package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.BaseSystemNdi;

public interface ScriptBuilder {
    static FromTemplateScriptBuilder fromTemplate(String templateName, String type, NutsId anyId, BaseSystemNdi sndi, NdiScriptOptions options) {
        return new FromTemplateScriptBuilder(templateName,type, anyId, sndi, options,sndi.getSession());
    }

    static SimpleScriptBuilder simple(String type, NutsId anyId, BaseSystemNdi sndi) {
        return new SimpleScriptBuilder(type, anyId, sndi,sndi.getSession());
    }

    String buildString();

    PathInfo build();

}
