package net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.script;

import net.thevpc.nuts.io.NIOException;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.NShellFamily;
import net.thevpc.nuts.runtime.standalone.xtra.shell.AbstractScriptBuilder;
import net.thevpc.nuts.runtime.standalone.xtra.shell.NShellHelper;
import net.thevpc.nuts.runtime.standalone.workspace.cmd.settings.ndi.base.BaseSystemNdi;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimpleScriptBuilder extends AbstractScriptBuilder {
    private BaseSystemNdi sndi;
    private List<String> lines=new ArrayList<>();

    public SimpleScriptBuilder(NShellFamily shellFamily, String type, NId anyId, BaseSystemNdi sndi) {
        super(shellFamily,type, anyId);
        this.sndi = sndi;
    }

    public SimpleScriptBuilder printCall(String line, String... args) {
        return println(NShellHelper.of(getShellFamily()).getCallScriptCommand(line,args));
    }

    public SimpleScriptBuilder printSet(String var, String value) {
        return println(NShellHelper.of(getShellFamily()).getSetVarCommand(var, value));
    }

    public SimpleScriptBuilder printSetStatic(String var, String value) {
        return println(NShellHelper.of(getShellFamily()).getSetVarStaticCommand(var, value));
    }

    public SimpleScriptBuilder printComment(String line) {
        for (String s : _split(line)) {
            println(NShellHelper.of(getShellFamily()).toCommentLine(s));
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
            throw new NIOException(e);
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
        return String.join(NShellHelper.of(getShellFamily()).newlineString(),lines);
    }
    public SimpleScriptBuilder setPath(Path path) {
        return (SimpleScriptBuilder) super.setPath(path);
    }

    public SimpleScriptBuilder setPath(String preferredName) {
        return (SimpleScriptBuilder) super.setPath(preferredName);
    }
}
