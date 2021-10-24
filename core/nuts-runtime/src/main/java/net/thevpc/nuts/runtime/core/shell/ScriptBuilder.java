package net.thevpc.nuts.runtime.core.shell;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsShellFamily;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.PathInfo;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script.FromTemplateScriptBuilder;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script.SimpleScriptBuilder;

public interface ScriptBuilder {
    static FromTemplateScriptBuilder fromTemplate(String templateName, NutsShellFamily shellFamily, String type, NutsId anyId, BaseSystemNdi sndi, NdiScriptOptions options) {
        return new FromTemplateScriptBuilder(templateName, shellFamily, type, anyId, sndi, options, sndi.getSession());
    }

    static SimpleScriptBuilder simple(NutsShellFamily shellFamily, String type, NutsId anyId, BaseSystemNdi sndi) {
        return new SimpleScriptBuilder(shellFamily,type, anyId, sndi, sndi.getSession());
    }

    String buildString();

    PathInfo build();

}
