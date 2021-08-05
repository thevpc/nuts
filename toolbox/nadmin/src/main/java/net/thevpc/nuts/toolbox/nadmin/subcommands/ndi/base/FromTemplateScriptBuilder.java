package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base;

import net.thevpc.nuts.NutsDefinition;
import net.thevpc.nuts.NutsId;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsTextStyle;
import net.thevpc.nuts.toolbox.nadmin.PathInfo;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.NdiScriptInfoType;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.util.NdiUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class FromTemplateScriptBuilder implements ScriptBuilder {
    private NutsSession session;
    private NutsId anyId;
    private String path;
    private NdiScriptInfoType type;
    private BaseSystemNdi sndi;
    private String templateName = "body";
    private List<String> body = new ArrayList<>();
    private Function<String, String> mapper;

    public FromTemplateScriptBuilder(NdiScriptInfoType type, NutsId anyId, BaseSystemNdi sndi, NutsSession session) {
        this.session = session;
        this.anyId = anyId;
        this.type = type;
        this.sndi = sndi;
    }

    public FromTemplateScriptBuilder printCall(String line,String... args) {
        return println(sndi.getCallScriptCommand(line,args));
    }

    public FromTemplateScriptBuilder printSet(String var, String value) {
        return println(sndi.getSetVarCommand(var, value));
    }

    public FromTemplateScriptBuilder printSetStatic(String var, String value) {
        return println(sndi.getSetVarStaticCommand(var, value));
    }

    public FromTemplateScriptBuilder printComment(String line) {
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

    public FromTemplateScriptBuilder println(String line) {
        body.addAll(_split(line));
        return this;
    }

    public Function<String, String> getMapper() {
        return mapper;
    }

    public FromTemplateScriptBuilder setMapper(Function<String, String> mapper) {
        this.mapper = mapper;
        return this;
    }

    public String getTemplateName() {
        return templateName;
    }

    public FromTemplateScriptBuilder setTemplateName(String templateName) {
        this.templateName = templateName;
        return this;
    }

    public String getPath() {
        return path;
    }

    public FromTemplateScriptBuilder setPath(Path path) {
        this.path = path == null ? null : path.toString();
        return this;
    }

    public FromTemplateScriptBuilder setPreferredName(String preferredName) {
        this.path = preferredName;
        return this;
    }

    @Override
    public String buildString() {
        try {
            //Path script = getScriptFile(name);
            NutsDefinition anyIdDef = session.getWorkspace().search().addId(anyId).setLatest(true).getResultDefinitions().singleton();
            NutsId anyId = anyIdDef.getId();
            StringWriter bos = new StringWriter();
            try (BufferedWriter w = new BufferedWriter(bos)) {
                NdiUtils.generateScript("/net/thevpc/nuts/toolbox/nadmin/" + sndi.getTemplateName(templateName),
                        w, new Function<String, String>() {
                            @Override
                            public String apply(String s) {
                                String v = mapper==null?null:mapper.apply(s);
                                if (v != null) {
                                    return v;
                                }
                                switch (s) {
                                    case "NUTS_ID":
                                        return anyId.toString();
                                    case "BODY":
                                        return String.join(sndi.newlineString(),body);
                                    case "GENERATOR":
                                        return session.getAppId().toString();
                                }
                                return s;
                            }
                        });
            }
            return bos.toString();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public PathInfo build() {
        try {
            //Path script = getScriptFile(name);
            NutsDefinition anyIdDef = session.getWorkspace().search().addId(anyId).setLatest(true).getResultDefinitions().singleton();
            anyId = anyIdDef.getId();
            path = new NameBuilder(anyId,
                    path == null ? "%n" : path
                    , anyIdDef.getDescriptor()).buildName();
            Path script = Paths.get(path);
            boolean alreadyExists = Files.exists(script);
            boolean requireWrite = false;
            String oldContent = "";
            String newContent = buildString();
            if (alreadyExists) {
                try {
                    oldContent = new String(Files.readAllBytes(script));
                } catch (Exception ex) {
                    //ignore
                }
                if (newContent.equals(oldContent)) {
                    return new PathInfo(type, anyId, script, PathInfo.Status.DISCARDED);
                }
                if (session.getTerminal().ask()
                        .resetLine()
                        .setDefaultValue(true).setSession(session)
                        .forBoolean("override existing script %s ?",
                                session.getWorkspace().text().forStyled(
                                        NdiUtils.betterPath(script.toString()), NutsTextStyle.path()
                                )
                        ).getBooleanValue()) {
                    requireWrite = true;
                }
            } else {
                requireWrite = true;
            }
            if (script.getParent() != null) {
                if (!Files.exists(script.getParent())) {
                    Files.createDirectories(script.getParent());
                }
            }
            Files.write(script, newContent.getBytes());
            NdiUtils.setExecutable(script);
            if (requireWrite) {
                if (alreadyExists) {
                    return new PathInfo(type, anyId, script, PathInfo.Status.OVERRIDDEN);
                } else {
                    return new PathInfo(type, anyId, script, PathInfo.Status.CREATED);
                }
            } else {
                return new PathInfo(type, anyId, script, PathInfo.Status.DISCARDED);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
