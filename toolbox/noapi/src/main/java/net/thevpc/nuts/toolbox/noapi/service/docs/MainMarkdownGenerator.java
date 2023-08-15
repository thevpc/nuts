package net.thevpc.nuts.toolbox.noapi.service.docs;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.toolbox.noapi.util.AppMessages;
import net.thevpc.nuts.toolbox.noapi.util.NoApiUtils;
import net.thevpc.nuts.toolbox.noapi.service.OpenApiParser;
import net.thevpc.nuts.toolbox.noapi.util._StringUtils;
import net.thevpc.nuts.toolbox.noapi.model.*;
import net.thevpc.nuts.util.NMaps;
import net.thevpc.nuts.util.NMsg;
import net.thevpc.nuts.util.NOptional;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MainMarkdownGenerator {
    private NSession session;
    private AppMessages msg;
    private Properties httpCodes = new Properties();
    private OpenApiParser openApiParser = new OpenApiParser();
    private int maxExampleInlineLength = 80;

    public MainMarkdownGenerator(NSession session, AppMessages msg) {
        this.session = session;
        this.msg = msg;
        try (InputStream is = getClass().getResourceAsStream("/net/thevpc/nuts/toolbox/noapi/http-codes.properties")) {
            httpCodes.load(is);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public MdDocument createMarkdown(NElement obj, NPath folder, Map<String, String> vars0, List<String> defaultAdocHeaders) {
        MdDocumentBuilder doc = new MdDocumentBuilder();

        List<String> options = new ArrayList<>(defaultAdocHeaders);
        if (folder.resolve("logo.png").exists()) {
            options.add(":title-logo-image: " + folder.resolve("logo.png").normalize().toAbsolute().toString());
        }
        doc.setProperty("headers", options.toArray(new String[0]));
        doc.setDate(LocalDate.now());
        doc.setSubTitle("RESTRICTED - INTERNAL");

        NElements prv = NElements.of(session);
        List<MdElement> all = new ArrayList<>();
        NObjectElement entries = obj.asObject().get(session);
        all.add(MdFactory.endParagraph());
        NObjectElement infoObj = entries.getObject("info").orElse(prv.ofEmptyObject());
        String documentTitle = infoObj.getString("title").orNull();
        doc.setTitle(documentTitle);
        String documentVersion = infoObj.getString("version").orNull();
        doc.setVersion(documentVersion);

        all.add(MdFactory.title(1, documentTitle));
//        all.add(new MdImage(null,null,"Logo, 64,64","./logo.png"));
//        all.add(MdFactory.endParagraph());
//        all.add(MdFactory.seq(NoApiUtils.asText("API Reference")));
        Vars vars = _fillVars(entries, vars0);
        List<TypeCrossRef> typeCrossRefs = new ArrayList<>();
        _fillIntroduction(entries, all, vars);
        _fillConfigVars(entries, all, vars);
        _fillServerList(entries, all, vars);
        _fillHeaders(entries, all, vars);
        _fillSecuritySchemes(entries, all, vars);
        _fillApiPaths(entries, all, vars, typeCrossRefs);
        _fillSchemaTypes(entries, all, vars, typeCrossRefs);
        doc.setContent(MdFactory.seq(all));
        return doc.build();
    }

    private void _fillConfigVars(NObjectElement entries, List<MdElement> all, Vars vars2) {
        String target = "your-company";
        vars2.putDefault("config.target", target);
        List<ConfigVar> configVars = OpenApiParser.loadConfigVars(null, entries, vars2, session);
        if (configVars.isEmpty()) {
            return;
        }
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, msg.get("CONFIGURATION").get()));
        all.add(NoApiUtils.asText(
                NMsg.ofV(msg.get("section.config.master.body").get(), NMaps.of("name", target)
                ).toString()));
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, msg.get("CUSTOM_PARAMETER_LIST").get()));
        all.add(NoApiUtils.asText(
                NMsg.ofV(msg.get("section.config.master.customVars.body").get(), NMaps.of("name", target)
                ).toString()));
        all.add(MdFactory.endParagraph());

        for (ConfigVar configVar : configVars) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(4, "<<" + configVar.getId() + ">> : " + configVar.getName()));
            all.add(NoApiUtils.asText(configVar.getDescription()));
            all.add(MdFactory.endParagraph());
            all.add(NoApiUtils.asText("The following is an example :"));
            all.add(MdFactory.codeBacktick3("", vars2.format(configVar.getExample()), false));
        }
    }

    private Vars _fillVars(NObjectElement entries, Map<String, String> vars) {
        Map<String, String> m = new LinkedHashMap<>();

        NOptional<NObjectElement> v = entries.getObjectByPath("custom", "variables");
        if (v.isPresent()) {
            for (NElementEntry entry : v.get().entries()) {
                m.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        if (vars != null) {
            m.putAll(vars);
        }
        return new Vars(m);
    }

    private void _fillIntroduction(NObjectElement entries, List<MdElement> all, Vars vars) {
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, msg.get("INTRODUCTION").get()));
        all.add(MdFactory.endParagraph());
        NObjectElement info = entries.getObject("info").orElse(NObjectElement.ofEmpty(session));
        all.add(NoApiUtils.asText(info.getString("description").orElse("").trim()));
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, msg.get("CONTACT").get()));
        all.add(NoApiUtils.asText(
                msg.get("section.contact.body").get()
        ));
        all.add(MdFactory.endParagraph());

        NObjectElement contact = info.getObject("contact").orElse(NObjectElement.ofEmpty(session));
        all.add(MdFactory.table()
                .addColumns(
                        MdFactory.column().setName(msg.get("NAME").get()),
                        MdFactory.column().setName(msg.get("EMAIL").get()),
                        MdFactory.column().setName(msg.get("URL").get())
                )
                .addRows(
                        MdFactory.row().addCells(
                                NoApiUtils.asText(contact.getString("name").orElse("")),
                                NoApiUtils.asText(contact.getString("email").orElse("")),
                                NoApiUtils.asText(contact.getString("url").orElse(""))
                        )
                ).build()
        );
        all.add(MdFactory.endParagraph());

        all.add(MdFactory.title(3, msg.get("CHANGES").get()));
        all.add(NoApiUtils.asText(
                msg.get("section.changes.body").get()
        ));
        all.add(MdFactory.endParagraph());

        NArrayElement changeLog = info.getArray("changes").orElse(NArrayElement.ofEmpty(session));
        MdTableBuilder changeLogTable = MdFactory.table()
                .addColumns(
                        MdFactory.column().setName(msg.get("DATE").get()),
                        MdFactory.column().setName(msg.get("VERSION").get()),
                        MdFactory.column().setName(msg.get("DESCRIPTION").get())
                );
        for (NElement nElement : changeLog) {
            changeLogTable.addRows(
                    MdFactory.row().addCells(
                            NoApiUtils.asText(nElement.asObject().get().getString("date").orElse("")),
                            NoApiUtils.asText(nElement.asObject().get().getString("version").orElse("")),
                            NoApiUtils.asText(nElement.asObject().get().getString("title").orElse(""))
                    )
            );
        }

        all.add(changeLogTable.build());
        all.add(MdFactory.endParagraph());
        for (NElement nElement : changeLog) {
            NObjectElement oo = nElement.asObject().get();
            all.add(MdFactory.title(4, "VERSION " + oo.getString("version").get()+" : "+oo.getString("title").get()));
            all.add(MdFactory.endParagraph());
            all.add(NoApiUtils.asText(
                    oo.getString("observations").get()
            ));
            all.add(MdFactory.endParagraph());
            for (NElement item : oo.getArray("details").orElse(NArrayElement.ofEmpty(session))) {
                all.add(MdFactory.ul(1,NoApiUtils.asText(item.asString().get())));
            }
            all.add(MdFactory.endParagraph());
        }
    }

    private void _fillHeaders(NObjectElement entries, List<MdElement> all, Vars vars2) {
        NObjectElement components = entries.getObject("components").orElse(NObjectElement.ofEmpty(session));
        if (!components.getObject("headers").isEmpty()) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(3, msg.get("HEADERS").get()));
            all.add(MdFactory.endParagraph());
            all.add(NoApiUtils.asText(msg.get("section.headers.body").get()));
            all.add(MdFactory.endParagraph());
            MdTableBuilder table = MdFactory.table()
                    .addColumns(
                            MdFactory.column().setName(msg.get("NAME").get()),
                            MdFactory.column().setName(msg.get("TYPE").get()),
                            MdFactory.column().setName(msg.get("DESCRIPTION").get())
                    );

            for (NElementEntry ee : components.getObject("headers").orElse(NObjectElement.ofEmpty(session))) {
                String k = ee.getKey().toString();
                k = k + (ee.getValue().asObject().get(session).getBoolean("deprecated").orElse(false) ? (" [" + msg.get("DEPRECATED").get() + "]") : "");
                k = k + NoApiUtils.asText(requiredSuffix(ee.getValue().asObject().get(session)));
                table.addRows(
                        MdFactory.row().addCells(
                                MdFactory.codeBacktick3("", k),
                                MdFactory.codeBacktick3("", ee.getValue().asObject().get(session).getObject("schema")
                                        .orElse(NObjectElement.ofEmpty(session))
                                        .getString("type").orElse("")),
                                NoApiUtils.asText(ee.getValue().asObject().get(session).getString("description").orElse(""))
                        )
                );
            }
            all.add(table.build());
        }
    }

    private void _fillSecuritySchemes(NObjectElement entries, List<MdElement> all, Vars vars2) {
        NObjectElement components = entries.getObject("components").orElse(NObjectElement.ofEmpty(session));
        NObjectElement securitySchemes = components.getObject("securitySchemes").orElse(NObjectElement.ofEmpty(session));
        if (!securitySchemes.isEmpty()) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(3, msg.get("SECURITY_AND_AUTHENTICATION").get()));
            all.add(MdFactory.endParagraph());
            all.add(NoApiUtils.asText(msg.get("section.security.body").get()));
            for (NElementEntry ee : securitySchemes) {
                String type = ee.getValue().asObject().get(session).getString("type").orElse("");
                switch (type) {
                    case "apiKey": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Api Key)"));
                        all.add(MdFactory.endParagraph());
                        all.add(NoApiUtils.asText(vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName(msg.get("NAME").get()),
                                        MdFactory.column().setName(msg.get("IN").get())
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.codeBacktick3("",
                                                        vars2.format(ee.getValue().asObject().get(session).getString("name").orElse(""))),
                                                MdFactory.codeBacktick3("",
                                                        vars2.format(ee.getValue().asObject().get(session).getString("in").orElse("").toUpperCase())
                                                )
                                        ))
                                .build()
                        );
                        break;
                    }
                    case "http": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Http)"));
                        all.add(MdFactory.endParagraph());
                        all.add(NoApiUtils.asText(
                                vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName(msg.get("SCHEME").get()),
                                        MdFactory.column().setName(msg.get("BEARER").get())
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                NoApiUtils.asText(vars2.format(ee.getValue().asObject().get(session).getString("scheme").orElse(""))),
                                                NoApiUtils.asText(vars2.format(ee.getValue().asObject().get(session).getString("bearerFormat").orElse("")))
                                        ))
                                .build()
                        );
                        break;
                    }
                    case "oauth2": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Oauth2)"));
                        all.add(MdFactory.endParagraph());
                        all.add(NoApiUtils.asText(vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
//                        all.add(MdFactory
//                                .table().addColumns(
//                                        MdFactory.column().setName("SCHEME"),
//                                        MdFactory.column().setName("BEARER")
//                                )
//                                .addRows(MdFactory.row()
//                                        .addCells(
//                                                asText(ee.getValue().asObject().getString("scheme")),
//                                                asText(ee.getValue().asObject().getString("bearerFormat"))
//                                        ))
//                        );
                        break;
                    }
                    case "openIdConnect": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (OpenId Connect)"));
                        all.add(MdFactory.endParagraph());
                        all.add(NoApiUtils.asText(ee.getValue().asObject().get(session).getString("description").orElse("")));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("URL")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                NoApiUtils.asText(ee.getValue().asObject().get(session).getString("openIdConnectUrl").orElse(""))
                                        ))
                                .build()
                        );
                        break;
                    }
                    default: {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (" + type + ")"));
                        all.add(NoApiUtils.asText(vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
                    }
                }
            }
        }

    }


    private void _fillSchemaTypes(NObjectElement entries, List<MdElement> all, Vars vars2, List<TypeCrossRef> typeCrossRefs) {
        Map<String, TypeInfo> allTypes = openApiParser.parseTypes(entries, session);
        if (allTypes.isEmpty()) {
            return;
        }
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, msg.get("SCHEMA_TYPES").get()));
        for (Map.Entry<String, TypeInfo> entry : allTypes.entrySet()) {
            TypeInfo v = entry.getValue();
            if ("object".equals(v.getType())) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(3, entry.getKey()));
                String d1 = v.getDescription();
                String d2 = v.getSummary();
                if (!NBlankable.isBlank(d1) && !NBlankable.isBlank(d2)) {
                    all.add(NoApiUtils.asText(d1));
                    all.add(MdFactory.text(". "));
                    all.add(NoApiUtils.asText(d2));
                    if (!NBlankable.isBlank(d2) && !d2.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                } else if (!NBlankable.isBlank(d1)) {
                    all.add(NoApiUtils.asText(d1));
                    if (!NBlankable.isBlank(d1) && !d1.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                } else if (!NBlankable.isBlank(d2)) {
                    all.add(NoApiUtils.asText(d2));
                    if (!NBlankable.isBlank(d2) && !d2.endsWith(".")) {
                        all.add(NoApiUtils.asText("."));
                    }
                }
                List<TypeCrossRef> types = typeCrossRefs.stream().filter(x -> x.getType().equals(v.getName())).collect(Collectors.toList());
                if (types.size() > 0) {
                    all.add(MdFactory.endParagraph());
                    all.add(NoApiUtils.asText(msg.get("ThisTypeIsUsedIn").get()));
                    all.add(MdFactory.endParagraph());
                    for (TypeCrossRef type : types) {
                        all.add(MdFactory.ul(1,
                                MdFactory.ofListOrEmpty(
                                        new MdElement[]{
                                                MdFactory.codeBacktick3("", type.getUrl()),
                                                NoApiUtils.asText(" (" + type.getLocation() + ")"),
                                        }
                                )
                        ));
                    }
                    all.add(MdFactory.endParagraph());
                }

                MdTableBuilder mdTableBuilder = MdFactory.table().addColumns(
                        MdFactory.column().setName(msg.get("NAME").get()),
                        MdFactory.column().setName(msg.get("TYPE").get()),
                        MdFactory.column().setName(msg.get("DESCRIPTION").get()),
                        MdFactory.column().setName(msg.get("EXAMPLE").get())
                );
                for (FieldInfo p : v.getFields()) {
                    mdTableBuilder.addRows(
                            MdFactory.row().addCells(
                                    NoApiUtils.asText(p.name),
                                    NoApiUtils.codeElement(p.schema, false, requiredSuffix(p.required), msg),
                                    NoApiUtils.asText(p.description == null ? "" : p.description.trim()),
                                    NoApiUtils.jsonTextElementInlined(p.example)
                            )
                    );
                }
                all.add(mdTableBuilder.build());
            }
            if (!NBlankable.isBlank(v.getExample())) {
                all.add(MdFactory.endParagraph());
                all.add(NoApiUtils.asText(msg.get("EXAMPLE").get()));
                all.add(NoApiUtils.asText(":"));
                all.add(MdFactory.endParagraph());
                all.add(NoApiUtils.jsonTextElement(v.getExample()));
            }
        }
    }


    private void _fillApiPaths(NObjectElement entries, List<MdElement> all, Vars vars2, List<TypeCrossRef> typeCrossRefs) {
        Map<String, TypeInfo> allTypes = openApiParser.parseTypes(entries, session);
        NElements prv = NElements.of(session);
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, msg.get("API_PATHS").get()));
        int apiSize = entries.get(prv.ofString("paths")).flatMap(NElement::asObject).get(session).size();
        all.add(NoApiUtils.asText(NMsg.ofV(msg.get("API_PATHS.body").get(), NMaps.of("apiSize", apiSize)).toString()));
        all.add(MdFactory.endParagraph());
        for (NElementEntry path : entries.get(prv.ofString("paths")).flatMap(NElement::asObject).get(session)) {
            String url = path.getKey().asString().get(session);
            all.add(MdFactory.ul(1, MdFactory.codeBacktick3("", url)));
        }
        all.add(MdFactory.endParagraph());
        all.add(NoApiUtils.asText(msg.get("API_PATHS.text").get()));
        NObjectElement schemas = entries.getObjectByPath("components", "schemas").orNull();
        for (NElementEntry path : entries.get(prv.ofString("paths")).flatMap(NElement::asObject).get(session)) {
            String url = path.getKey().asString().get(session);
            Map<String, NObjectElement> calls = new HashMap<>();
            String dsummary = null;
            String ddescription = null;
            NArrayElement dparameters = null;
            for (NElementEntry ss : path.getValue().asObject().get(session)) {
                String k = ss.getKey().asString().get(session);
                switch (k) {
                    case "summary": {
                        dsummary = ss.getValue().asString().get(session);
                        break;
                    }
                    case "description": {
                        ddescription = ss.getValue().asString().get(session);
                        break;
                    }
                    case "parameters": {
                        dparameters = ss.getValue().asArray().get(session);
                        break;
                    }
                    default: {
                        calls.put(k, ss.getValue().asObject().get(session));
                    }
                }
            }
            for (Map.Entry<String, NObjectElement> ee : calls.entrySet()) {
                _fillApiPathMethod(ee.getKey(), ee.getValue(), all, url, prv, dsummary, ddescription, dparameters, schemas, typeCrossRefs,allTypes);
            }
        }
    }

    private void _fillServerList(NObjectElement entries, List<MdElement> all, Vars vars2) {
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, "SERVER LIST"));
        all.add(NoApiUtils.asText(
                msg.get("section.serverlist.body").get()
        ));
        NElements prv = NElements.of(session);
        for (NElement srv : entries.getArray(prv.ofString("servers")).orElse(prv.ofEmptyArray())) {
            NObjectElement srvObj = (NObjectElement) srv.asObject().orElse(prv.ofEmptyObject());
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(4, vars2.format(srvObj.getString("url").orNull())));
            all.add(NoApiUtils.asText(vars2.format(srvObj.getString("description").orNull())));
            NElement vars = srvObj.get(prv.ofString("variables")).orNull();
            if (vars != null && !vars.isEmpty()) {
                MdTableBuilder mdTableBuilder = MdFactory.table().addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("SPEC"),
                        MdFactory.column().setName("DESCRIPTION")
                );
                for (NElementEntry variables : vars.asObject().get(session)) {
                    mdTableBuilder.addRows(
                            MdFactory.row().addCells(
                                    NoApiUtils.asText(variables.getKey().asString().get(session)),
                                    //                                asText(variables.getValue().asObject().getString("enum")),
                                    NoApiUtils.asText(vars2.format(variables.getValue().asObject().get(session).getString("default").orNull())),
                                    NoApiUtils.asText(vars2.format(variables.getValue().asObject().get(session).getString("description").orNull()))
                            )
                    );
                }
                all.add(mdTableBuilder.build());
            }
        }
    }


    private String getSmartTypeName(NObjectElement obj) {
        String e = _StringUtils.nvl(obj.getString("type").orNull(), "string");
        if ("array".equals(e)) {
            NObjectElement items = obj.getObject("items").orNull();
            if (items != null) {
                return getSmartTypeName(items) + "[]";
            } else {
                return e;
            }
        } else {
            return e;
        }
    }

    private void _fillApiPathMethodParam(List<NElement> headerParameters, List<MdElement> all, String url, List<TypeCrossRef> typeCrossRefs, String paramType) {
        MdTable tab = new MdTable(
                new MdColumn[]{
                        new MdColumn(NoApiUtils.asText(msg.get("NAME").get()), MdHorizontalAlign.LEFT),
                        new MdColumn(NoApiUtils.asText(msg.get("TYPE").get()), MdHorizontalAlign.LEFT),
                        new MdColumn(NoApiUtils.asText(msg.get("DESCRIPTION").get()), MdHorizontalAlign.LEFT),
                        new MdColumn(NoApiUtils.asText(msg.get("EXAMPLE").get()), MdHorizontalAlign.LEFT)
                },
                headerParameters.stream().map(
                        headerParameter -> {
                            NObjectElement obj = headerParameter.asObject().orElse(NElements.of(session).ofEmptyObject());
                            String name = obj.getString("name").orNull();
                            boolean pdeprecated = obj.getBoolean("deprecated").orElse(false);
                            String type = getSmartTypeName(obj)
                                    + requiredSuffix(obj);
                            typeCrossRefs.add(new TypeCrossRef(
                                    obj.getString("type").orElse(""), url, paramType
                            ));
                            return new MdRow(
                                    new MdElement[]{
                                            MdFactory.codeBacktick3("", _StringUtils.nvl(name, "unknown")
                                                    + (pdeprecated ? (" [" + msg.get("DEPRECATED").get() + "]") : "")
                                            ),
                                            MdFactory.codeBacktick3("", type),
                                            NoApiUtils.asText(_StringUtils.nvl(obj.getString("description").orElse(""), "")),
                                            NoApiUtils.jsonTextElementInlined(obj.getString("example").orElse("")),
                                    }, false
                            );
                        }
                ).toArray(MdRow[]::new)
        );
        all.add(tab);
    }

    private String requiredSuffix(NObjectElement obj) {
        return requiredSuffix(obj.getBoolean("required").orElse(false));
    }

    private String requiredSuffix(boolean obj) {
        return obj ? (" [" + msg.get("REQUIRED").get() + "]") : (" [" + msg.get("OPTIONAL").get() + "]");
    }

    private void _fillApiPathMethod(String method, NObjectElement call, List<MdElement> all, String url, NElements prv,
                                    String dsummary, String ddescription, NArrayElement dparameters,
                                    NObjectElement schemas, List<TypeCrossRef> typeCrossRefs,Map<String, TypeInfo> allTypes) {

        String nsummary = call.getString("summary").orElse(dsummary);
        String ndescription = call.getString("description").orElse(ddescription);
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, method.toUpperCase() + " " + url));
        all.add(NoApiUtils.asText(nsummary));
        if (!NBlankable.isBlank(nsummary) && !nsummary.endsWith(".")) {
            all.add(NoApiUtils.asText("."));
        }
        all.add(MdFactory.endParagraph());
        all.add(
                MdFactory.codeBacktick3("", "[" + method.toUpperCase() + "] " + url)
        );
        all.add(MdFactory.endParagraph());
        if (ndescription != null) {
            all.add(NoApiUtils.asText(ndescription));
            if (!NBlankable.isBlank(ndescription) && !ndescription.endsWith(".")) {
                all.add(NoApiUtils.asText("."));
            }
            all.add(MdFactory.endParagraph());
        }
        NArrayElement parameters = call.getArray(prv.ofString("parameters"))
                .orElseUse(() -> NOptional.of(dparameters))
                .orElseGet(() -> NArrayElementBuilder.of(session).build());
        List<NElement> headerParameters = parameters.stream().filter(x -> "header".equals(x.asObject().get(session).getString("in").orNull())).collect(Collectors.toList());
        List<NElement> queryParameters = parameters.stream().filter(x -> "query".equals(x.asObject().get(session).getString("in").orNull())).collect(Collectors.toList());
        List<NElement> pathParameters = parameters.stream().filter(x -> "path".equals(x.asObject().get(session).getString("in").orNull())).collect(Collectors.toList());
        NObjectElement requestBody = call.getObject("requestBody").orNull();
        boolean withRequestHeaderParameters = !headerParameters.isEmpty();
        boolean withRequestPathParameters = !pathParameters.isEmpty();
        boolean withRequestQueryParameters = !queryParameters.isEmpty();
        boolean withRequestBody = (requestBody != null && !requestBody.isEmpty());
        if (
                withRequestHeaderParameters
                        || !queryParameters.isEmpty()
                        || withRequestPathParameters
                        || (requestBody != null && !requestBody.isEmpty())

        ) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(4, msg.get("REQUEST").get()));

            // paragraph details the expected request parameters and body to be provided by the caller

            if ((
                    (withRequestHeaderParameters ? 1 : 0) +
                            (withRequestQueryParameters ? 1 : 0) +
                            (withRequestPathParameters ? 1 : 0) +
                            (withRequestBody ? 1 : 0)
            ) > 1) {
                all.add(NoApiUtils.asText(msg.get("endpoint.info.1").get()));
            } else if (withRequestHeaderParameters) {
                all.add(NoApiUtils.asText(msg.get("endpoint.info.2").get()));
            } else if (withRequestQueryParameters) {
                all.add(NoApiUtils.asText(msg.get("endpoint.info.3").get()));
            } else if (withRequestPathParameters) {
                all.add(NoApiUtils.asText(msg.get("endpoint.info.4").get()));
            } else if (withRequestBody) {
                all.add(NoApiUtils.asText(msg.get("endpoint.info.5").get()));
            }

            if (withRequestHeaderParameters) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, msg.get("HEADER_PARAMETERS").get()));
                _fillApiPathMethodParam(headerParameters, all, url, typeCrossRefs, "Header Parameter");
            }
            if (withRequestPathParameters) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, msg.get("PATH_PARAMETERS").get()));
                _fillApiPathMethodParam(pathParameters, all, url, typeCrossRefs, "Path Parameter");
            }
            if (withRequestQueryParameters) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, msg.get("QUERY_PARAMETERS").get()));
                _fillApiPathMethodParam(queryParameters, all, url, typeCrossRefs, "Query Parameter");
            }
            if (withRequestBody) {
                boolean required = requestBody.getBoolean("required").orElse(false);
                String desc = requestBody.getString("description").orElse("");
                NObjectElement r = requestBody.getObject("content").orElseGet(() -> NObjectElement.ofEmpty(session));
                for (NElementEntry ii : r) {
                    all.add(MdFactory.endParagraph());
                    all.add(MdFactory.title(5, msg.get("REQUEST_BODY").get() + " - " + ii.getKey() +
                            requiredSuffix(required)));
                    all.add(NoApiUtils.asText(desc));
                    if (!NBlankable.isBlank(desc) && !desc.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                    TypeInfo o = openApiParser.parseOneType(ii.getValue().asObject().get(session), null, session, allTypes);
                    if (o.getRef() != null) {
                        typeCrossRefs.add(new TypeCrossRef(o.getRef(), url, "Request Body"));
//                        all.add(MdFactory.endParagraph());
//                        all.add(MdFactory.title(5, "REQUEST TYPE - " + o.ref));
                        all.add(NoApiUtils.asText(" "));
                        all.add(NoApiUtils.asText(NMsg.ofV(msg.get("requestType.info").get(), NMaps.of("type", o.getRef())).toString()));
                        NElement s = schemas.get(o.getRef()).orNull();
                        NElement description = null;
                        NElement example = null;
                        if (s != null) {
                            description = s.asObject().get().get("description").orNull();
                            example = s.asObject().get().get("example").orNull();
                        }
                        MdTable tab = new MdTable(
                                new MdColumn[]{
                                        new MdColumn(NoApiUtils.asText(msg.get("NAME").get()), MdHorizontalAlign.LEFT),
                                        new MdColumn(NoApiUtils.asText(msg.get("TYPE").get()), MdHorizontalAlign.LEFT),
                                        new MdColumn(NoApiUtils.asText(msg.get("DESCRIPTION").get()), MdHorizontalAlign.LEFT),
//                                        new MdColumn(NoApiUtils.asText(msg.get("EXAMPLE").get()), MdHorizontalAlign.LEFT)
                                },
                                new MdRow[]{
                                        new MdRow(
                                                new MdElement[]{
                                                        MdFactory.codeBacktick3("", "request-body"),
                                                        MdFactory.codeBacktick3("", o.getRef()),
                                                        NoApiUtils.asText(_StringUtils.nvl(description == null ? null : description.toString(), "")),
//                                                        jsonTextElementInlined(example),
                                                }, false
                                        )
                                }

                        );
                        all.add(tab);
                        if (!NBlankable.isBlank(example)) {
                            all.add(MdFactory.text(msg.get("request.body.example.intro").get()));
                            all.add(MdFactory.text(":\n"));
                            all.add(NoApiUtils.jsonTextElement(example));
                        }

                    } else {
                        all.add(MdFactory.endParagraph());
                        all.add(NoApiUtils.codeElement(o, true, "", msg));
                    }
                }
            }
        }

        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(4, msg.get("RESPONSE").get()));
        all.add(NoApiUtils.asText(NMsg.ofV(msg.get("section.response.body").get(), NMaps.of("path", url)).toString()));

        call.getObject("responses").get(session).stream()
                .forEach(x -> {
                    NElement s = x.getKey();
                    NElement v = x.getValue();
                    all.add(MdFactory.endParagraph());
                    String codeDescription = evalCodeDescription(s.toString());
                    all.add(MdFactory.title(5, msg.get("STATUS_CODE").get() + " - " + s
                            + (NBlankable.isBlank(codeDescription) ? "" : (" - " + codeDescription))
                    ));
                    String description = v.asObject().get(session).getString("description").orElse("");
                    all.add(NoApiUtils.asText(description));
                    if (!NBlankable.isBlank(description) && !description.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                    for (NElementEntry content : v.asObject().get(session).getObject("content").orElse(NObjectElement.ofEmpty(session))) {
                        TypeInfo o = openApiParser.parseOneType(content.getValue().asObject().get(session), null, session, allTypes);
                        if (o.getUserType().equals("$ref")) {
                            typeCrossRefs.add(new TypeCrossRef(
                                    o.getRef(),
                                    url, "Response (" + s + ")"
                            ));
                            if (NBlankable.isBlank(o.getExample())) {
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName(msg.get("RESPONSE_MODEL").get()),
                                                MdFactory.column().setName(msg.get("RESPONSE_TYPE").get())
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        NoApiUtils.asText(content.getKey().asString().get(session)),
                                                        NoApiUtils.asText(o.getRef())
                                                )
                                        ).build()
                                );
                            } else if (o.getExample().toString().trim().length() <= maxExampleInlineLength) {
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName(msg.get("RESPONSE_MODEL").get()),
                                                MdFactory.column().setName(msg.get("RESPONSE_TYPE").get())
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        NoApiUtils.asText(content.getKey().asString().get(session)),
                                                        NoApiUtils.asText(o.getRef())
                                                )
                                        ).build()
                                );
                                if (!NBlankable.isBlank(o.getExample())) {
                                    all.add(MdFactory.text(msg.get("response.body.example.intro").get()));
                                    all.add(MdFactory.text(":\n"));
                                    all.add(NoApiUtils.jsonTextElement(o.getExample()));
                                }
                            } else {
                                all.add(MdFactory.table()
                                                .addColumns(
                                                        MdFactory.column().setName(msg.get("RESPONSE_MODEL").get()),
                                                        MdFactory.column().setName(msg.get("RESPONSE_TYPE").get())//,
//                                                MdFactory.column().setName(msg.get("EXAMPLE").get())
                                                )
                                                .addRows(
                                                        MdFactory.row().addCells(
                                                                NoApiUtils.asText(content.getKey().asString().get(session)),
                                                                NoApiUtils.asText(o.getRef())//,
//                                                        MdFactory.seq(NoApiUtils.asText(msg.get("SEE_BELOW").get()), asText("..."))
                                                        )
                                                ).build()
                                );
                                if (!NBlankable.isBlank(o.getExample())) {
                                    all.add(MdFactory.text(msg.get("response.body.example.intro").get()));
                                    all.add(MdFactory.text(":\n"));
                                    all.add(NoApiUtils.jsonTextElement(o.getExample()));
                                }
                            }
                        } else {
                            all.add(MdFactory.endParagraph());

                            all.add(MdFactory.table()
                                    .addColumns(
                                            MdFactory.column().setName(msg.get("RESPONSE_MODEL").get()),
                                            MdFactory.column().setName(msg.get("RESPONSE_TYPE").get())
                                    )
                                    .addRows(
                                            MdFactory.row().addCells(
                                                    NoApiUtils.asText(content.getKey().asString().get(session)),
                                                    NoApiUtils.codeElement(o, true, "", msg)
                                                    //NoApiUtils.asText(o.getRef())
                                            )
                                    ).build()
                            );
                            if (!NBlankable.isBlank(o.getExample())) {
                                all.add(MdFactory.text(msg.get("response.body.example.intro").get()));
                                all.add(MdFactory.text(":\n"));
                                all.add(NoApiUtils.jsonTextElement(o.getExample()));
                            }
//                            all.add(MdFactory.title(6, msg.get("RESPONSE_MODEL").get() + " - " + content.getKey()));
////                        all.add(MdFactory.endParagraph());
//                            if (o.getRef() != null) {
//                                all.add(MdFactory.title(6, msg.get("RESPONSE_TYPE").get() + " - " + o.getRef()));
//                            } else {
//                                all.add(MdFactory.text("\n"));
//                                all.add(NoApiUtils.codeElement(o, true, "", msg));
//                            }
                        }
                    }
                });
    }


    private String evalCodeDescription(String s) {
        if (s == null) {
            return "";
        }

        String c = httpCodes.getProperty(s.trim());
        if (c != null) {
            return c;
        }
        return "";
    }

}
