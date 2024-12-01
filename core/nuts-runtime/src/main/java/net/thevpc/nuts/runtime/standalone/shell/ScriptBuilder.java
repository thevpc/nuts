package net.thevpc.nuts.runtime.standalone.shell;

import net.thevpc.nuts.NId;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.util.PathInfo;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script.FromTemplateScriptBuilder;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script.SimpleScriptBuilder;

public interface ScriptBuilder {
    static FromTemplateScriptBuilder fromTemplate(String templateName, NShellFamily shellFamily, String type, NId anyId, BaseSystemNdi sndi, NdiScriptOptions options) {
        return new FromTemplateScriptBuilder(templateName, shellFamily, type, anyId, sndi, options, sndi.getWorkspace());
    }

    static SimpleScriptBuilder simple(NShellFamily shellFamily, String type, NId anyId, BaseSystemNdi sndi) {
        return new SimpleScriptBuilder(shellFamily,type, anyId, sndi, sndi.getWorkspace());
    }

    String buildString();

    PathInfo build();

}
