package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script;

import net.thevpc.nuts.NutsIOException;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsShellFamily;
import net.thevpc.nuts.runtime.core.shell.AbstractScriptBuilder;
import net.thevpc.nuts.runtime.core.shell.NutsShellHelper;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.BaseSystemNdi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimpleScriptBuilder extends AbstractScriptBuilder {
    private BaseSystemNdi sndi;
    private List<String> lines=new ArrayList<>();

    public SimpleScriptBuilder(NutsShellFamily shellFamily,String type, NutsId anyId, BaseSystemNdi sndi, NutsSession session) {
        super(shellFamily,type, anyId,session);
        this.sndi = sndi;
    }

    public SimpleScriptBuilder printCall(String line, String... args) {
        return println(NutsShellHelper.of(getShellFamily()).getCallScriptCommand(line,args));
    }

    public SimpleScriptBuilder printSet(String var, String value) {
        return println(NutsShellHelper.of(getShellFamily()).getSetVarCommand(var, value));
    }

    public SimpleScriptBuilder printSetStatic(String var, String value) {
        return println(NutsShellHelper.of(getShellFamily()).getSetVarStaticCommand(var, value));
    }

    public SimpleScriptBuilder printComment(String line) {
        for (String s : _split(line)) {
            println(NutsShellHelper.of(getShellFamily()).toCommentLine(s));
        }
        return this;
    }

    private List<String> _split(String line) {
        List<String> a = new ArrayList<>();
        BufferedReader br = new BufferedReader(new StringReader(line));
        String c;
        try {
            while ((c = br.readLine()) != null) {
                a.add(c);
            }
        } catch (IOException e) {
            throw new NutsIOException(getSession(),e);
        }
        return a;

    }

    private List<String> currBody() {
        return lines;
    }

    public SimpleScriptBuilder println(String line) {
        currBody().addAll(_split(line));
        return this;
    }

    @Override
    public String buildString() {
        return String.join(NutsShellHelper.of(getShellFamily()).newlineString(),lines);
    }
    public SimpleScriptBuilder setPath(Path path) {
        return (SimpleScriptBuilder) super.setPath(path);
    }

    public SimpleScriptBuilder setPath(String preferredName) {
        return (SimpleScriptBuilder) super.setPath(preferredName);
    }
}
