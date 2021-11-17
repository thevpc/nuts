package net.thevpc.nuts.runtime.standalone.shell;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsShellFamily;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script.FromTemplateScriptBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script.SimpleScriptBuilder;

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
