package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base;

import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptInfoType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SimpleScriptBuilder extends AbstractScriptBuilder {
    private BaseSystemNdi sndi;
    private List<String> lines=new ArrayList<>();

    public SimpleScriptBuilder(NdiScriptInfoType type, NutsId anyId, BaseSystemNdi sndi, NutsSession session) {
        super(type, anyId,session);
        this.sndi = sndi;
    }

    public SimpleScriptBuilder printCall(String line, String... args) {
        return println(sndi.getCallScriptCommand(line,args));
    }

    public SimpleScriptBuilder printSet(String var, String value) {
        return println(sndi.getSetVarCommand(var, value));
    }

    public SimpleScriptBuilder printSetStatic(String var, String value) {
        return println(sndi.getSetVarStaticCommand(var, value));
    }

    public SimpleScriptBuilder printComment(String line) {
        for (String s : _split(line)) {
            println(sndi.toCommentLine(s));
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
            throw new UncheckedIOException(e);
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
        return String.join(sndi.newlineString(),lines);
    }
    public SimpleScriptBuilder setPath(Path path) {
        return (SimpleScriptBuilder) super.setPath(path);
    }

    public SimpleScriptBuilder setPath(String preferredName) {
        return (SimpleScriptBuilder) super.setPath(preferredName);
    }
}
