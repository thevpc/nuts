package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.script;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptOptions;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.BaseSystemNdi;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.util.NdiUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class FromTemplateScriptBuilder extends AbstractScriptBuilder {
    private final BaseSystemNdi sndi;
    private String templateName;
    private final Map<String, List<String>> bodyMap = new LinkedHashMap<>();
    private String currBody = "BODY";
    private Function<String, String> mapper;
    private final NdiScriptOptions options;

    public FromTemplateScriptBuilder(String templateName, String type, NutsId anyId, BaseSystemNdi sndi, NdiScriptOptions options, NutsSession session) {
        super(type, anyId, session);
        this.sndi = sndi;
        this.options = options;
        this.templateName = templateName;
    }

    public FromTemplateScriptBuilder printCall(String line, String... args) {
        return println(sndi.getCallScriptCommand(line, args));
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

    private List<String> currBody() {
        return bodyMap.computeIfAbsent(currBody, v -> new ArrayList<>());
    }

    public FromTemplateScriptBuilder withBody(String b) {
        if (b == null || b.isEmpty()) {
            b = "BODY";
        }
        this.currBody = b;
        return this;
    }

    public FromTemplateScriptBuilder println(String line) {
        currBody().addAll(_split(line));
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

    public String buildString() {
        try {
            //Path script = getScriptFile(name);
            NutsDefinition anyIdDef = getSession().search().addId(getAnyId()).setLatest(true).getResultDefinitions().singleton();
            NutsId anyId = anyIdDef.getId();
            StringWriter bos = new StringWriter();
            try (BufferedWriter w = new BufferedWriter(bos)) {
                NdiUtils.generateScript("/net/thevpc/nuts/runtime/settings/" + sndi.getTemplateName(templateName),
                        w, new Function<String, String>() {
                            @Override
                            public String apply(String s) {
                                String v = mapper == null ? null : mapper.apply(s);
                                if (v != null) {
                                    return v;
                                }
                                switch (s) {
                                    case "NUTS_ID":
                                        return anyId.getLongName();
                                    case "GENERATOR": {
//                                        NutsId appId = getSession().getAppId();
//                                        if(appId!=null){
//                                            return appId.getLongName();
//                                        }
//                                        appId=getSession().getWorkspace().getRuntimeId();
//                                        return appId.getLongName();
                                        return getSession().getWorkspace().getRuntimeId().getLongName();
                                    }
                                    case "SCRIPT_NUTS":
                                        return sndi.getNutsStart(options).path().toString();
                                    case "SCRIPT_NUTS_TERM_INIT":
                                        return sndi.getNutsTermInit(options).path().toString();
                                    case "SCRIPT_NUTS_TERM":
                                        return sndi.getNutsTerm(options).path().toString();
                                    case "SCRIPT_NUTS_INIT":
                                        return sndi.getNutsInit(options).path().toString();
                                    case "SCRIPT_NUTS_ENV":
                                        return sndi.getNutsEnv(options).path().toString();
                                    case "NUTS_JAR":
                                        return options.resolveNutsApiJarPath().toString();
                                    case "BIN_FOLDER":
                                        return options.resolveBinFolder().toString();
                                    case "INC_FOLDER":
                                        return options.resolveIncFolder().toString();
                                    case "NUTS_API_VERSION":
                                        return options.getNutsApiVersion().toString();
                                    case "NUTS_API_ID":
                                        return options.resolveNutsApiId().toString();
                                    case "NUTS_VERSION":
                                        return getSession().getWorkspace().getApiVersion().toString();
                                    case "NUTS_WORKSPACE":
                                        return getSession().locations().getWorkspaceLocation();
                                    case "NUTS_WORKSPACE_APPS":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.APPS);
                                    case "NUTS_WORKSPACE_CONFIG":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.CONFIG);
                                    case "NUTS_WORKSPACE_CACHE":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.CACHE);
                                    case "NUTS_WORKSPACE_LIB":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.LIB);
                                    case "NUTS_WORKSPACE_LOG":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.LOG);
                                    case "NUTS_WORKSPACE_RUN":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.RUN);
                                    case "NUTS_WORKSPACE_TEMP":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.TEMP);
                                    case "NUTS_WORKSPACE_VAR":
                                        return getSession().locations().getStoreLocation(NutsStoreLocation.VAR);
                                    case "NUTS_JAR_EXPR": {
                                        String NUTS_JAR_PATH = options.resolveNutsApiJarPath().toString();
                                        if (NUTS_JAR_PATH.startsWith(getSession().locations().getStoreLocation(NutsStoreLocation.LIB))) {
                                            String pp = NUTS_JAR_PATH.substring(getSession().locations().getStoreLocation(NutsStoreLocation.LIB).length());
                                            return sndi.varRef("NUTS_WORKSPACE_LIB") + pp;
                                        } else {
                                            return NUTS_JAR_PATH;
                                        }
                                    }
                                    case "NUTS_WORKSPACE_BINDIR_EXPR": {
                                        //="${NUTS_WORKSPACE_APPS}/id/net/thevpc/nuts/nuts/0.8.2/bin"
                                        return sndi.varRef("NUTS_WORKSPACE_APPS") + options.resolveBinFolder().toString().substring(
                                                getSession().locations().getStoreLocation(NutsStoreLocation.APPS).length()
                                        );
                                    }
                                    default: {
                                        List<String> q = bodyMap.get(s);
                                        if (q != null) {
                                            return String.join(sndi.newlineString(), q);
                                        }
                                        break;
                                    }
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

    public FromTemplateScriptBuilder setPath(Path path) {
        return (FromTemplateScriptBuilder) super.setPath(path);
    }

    public FromTemplateScriptBuilder setPath(String preferredName) {
        return (FromTemplateScriptBuilder) super.setPath(preferredName);
    }

}
