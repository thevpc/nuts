package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.asciidoctor.AsciiDoctorWriter;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
//import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class NOpenAPIService {

    private NutsApplicationContext appContext;
    private OpenApiParser openApiParser = new OpenApiParser();
    private int maxExampleInlineLength=80;
    public NOpenAPIService(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    public void run(String source, String target, boolean keep) {

//        Path path = Paths.get("/data/from-git/RapiPdf/docs/specs/maghrebia-api-1.1.2.yml");
        String targetType = "pdf";
        if (target == null) {
            target = addExtension(source, "pdf").toString();
            targetType = "pdf";
        } else if (target.equals(".pdf")) {
            target = addExtension(source, "pdf").toString();
            targetType = "pdf";
        } else if (target.equals(".adoc")) {
            target = addExtension(source, "adoc").toString();
            targetType = "adoc";
        }
        MdDocument md = toMarkdown(source);
        NutsSession session = appContext.getSession();
        if (targetType.equals("adoc")) {
            writeAdoc(md, target, session.isPlainTrace());
        } else if (targetType.equals("pdf")) {
            String temp = null;
            if (keep) {
                temp = addExtension(source, "adoc").toString();
            } else {
                temp = NutsTmp.of(session)
                        .createTempFile("temp.adoc").toString();
            }
            writeAdoc(md, temp, keep && session.isPlainTrace());
            if (new File(target).getParentFile() != null) {
                new File(target).getParentFile().mkdirs();
            }
            Asciidoctor asciidoctor = Asciidoctor.Factory.create();
            String outfile = asciidoctor.convertFile(new File(temp),
                    OptionsBuilder.options()
                            //                            .inPlace(true)
                            .backend("pdf")
                            .safe(SafeMode.UNSAFE)
                            .toFile(new File(target))
            );
            if (session.isPlainTrace()) {
                session.out().printf("generated pdf %s\n",
                        NutsTexts.of(session).ofStyled(
                                target, NutsTextStyle.primary4()
                        )
                );
            }
            if (!keep) {
                new File(temp).delete();
            }
        } else {
            throw new NutsIllegalArgumentException(session, NutsMessage.cstyle("unsupported"));
        }
    }

    private void writeAdoc(MdDocument md, String target, boolean trace) {
        try (MdWriter mw = new AsciiDoctorWriter(new FileWriter(target))) {
            mw.write(md);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        if (trace) {
            appContext.getSession().out().printf("generated src %s\n",
                    NutsTexts.of(appContext.getSession()).ofStyled(
                            target, NutsTextStyle.primary4()
                    )
            );
        }
    }

    private Path addExtension(String source, String ext) {
        Path path = Paths.get(source);
        String n = path.getFileName().toString();
        if (n.endsWith(".json")) {
            n = n.substring(0, n.length() - ".json".length()) + "." + ext;
        } else if (n.endsWith(".yml")) {
            n = n.substring(0, n.length() - ".yml".length()) + "." + ext;
        } else if (n.endsWith(".yaml")) {
            n = n.substring(0, n.length() - ".yaml".length()) + "." + ext;
        } else {
            n = n + "." + ext;
        }
        return path.getParent().resolve(n);
    }

    private MdDocument toMarkdown(String source) {
        boolean json = false;
        Path sourcePath = Paths.get(source);
        try (BufferedReader r = Files.newBufferedReader(sourcePath)) {
            String t;
            while ((t = r.readLine()) != null) {
                t = t.trim();
                if (t.length() > 0) {
                    if (t.startsWith("{")) {
                        json = true;
                    }
                    break;
                }
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
        try (InputStream inputStream = Files.newInputStream(sourcePath)) {
            return toMarkdown(inputStream, json, sourcePath.getParent().toString());
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private NutsElement loadElement(InputStream inputStream, boolean json) {
        NutsSession session = appContext.getSession();
        if (json) {
            return NutsElements.of(session).json().parse(inputStream, NutsElement.class);
        } else {
            return NutsElements.of(session).json().parse(inputStream, NutsElement.class);
//            final Object o = new Yaml().load(inputStream);
//            return appContext.getWorkspace().elem().toElement(o);
        }
    }

    private void _fillIntroduction(NutsObjectElement entries, List<MdElement> all) {
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, "INTRODUCTION"));
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.text(entries.getObject("info").getString("description").trim()));
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, "CONTACT"));
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.table()
                .addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("EMAIL"),
                        MdFactory.column().setName("URL")
                )
                .addRows(
                        MdFactory.row().addCells(
                                MdFactory.text(entries.getObject("info").getObject("contact").getString("name")),
                                MdFactory.text(entries.getObject("info").getObject("contact").getString("email")),
                                MdFactory.text(entries.getObject("info").getObject("contact").getString("url"))
                        )
                ).build()
        );
    }

    private void _fillHeaders(NutsObjectElement entries, List<MdElement> all) {
        if (!entries.getObject("components").getObject("headers").isEmpty()) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(3, "HEADERS"));
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.text("This section includes common Headers to be included in the incoming requests."));
            all.add(MdFactory.endParagraph());
            MdTableBuilder table = MdFactory.table()
                    .addColumns(
                            MdFactory.column().setName("NAME"),
                            MdFactory.column().setName("TYPE"),
                            MdFactory.column().setName("REQUIRED"),
                            MdFactory.column().setName("DESCRIPTION")
                    );

            for (NutsElementEntry ee : entries.getObject("components").getObject("headers")) {
                table.addRows(
                        MdFactory.row().addCells(
                                MdFactory.codeBacktick3("", ee.getKey() + (ee.getValue().asObject().getBoolean("deprecated") ? " (DEPRECATED)" : "")),
                                MdFactory.codeBacktick3("", ee.getValue().asObject().getObject("schema").getString("type")),
                                MdFactory.text(ee.getValue().asObject().getBoolean("required") ? "required" : ""),
                                MdFactory.text(ee.getValue().asObject().getString("description"))
                        )
                );
            }
            all.add(table.build());
        }
    }

    private void _fillSecuritySchemes(NutsObjectElement entries, List<MdElement> all) {
        if (!entries.getObject("components").getObject("securitySchemes").isEmpty()) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(3, "SECURITY AND AUTHENTICATION"));
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.text("This section includes security configurations."));
            for (NutsElementEntry ee : entries.getObject("components").getObject("securitySchemes")) {
                String type = ee.getValue().asObject().getString("type");
                switch (type) {
                    case "apiKey": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Api Key)"));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("NAME"),
                                        MdFactory.column().setName("IN")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.codeBacktick3("", ee.getValue().asObject().getString("name")),
                                                MdFactory.codeBacktick3("", ee.getValue().asObject().getString("in").toUpperCase())
                                        ))
                                .build()
                        );
                        break;
                    }
                    case "http": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Http)"));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("SCHEME"),
                                        MdFactory.column().setName("BEARER")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.text(ee.getValue().asObject().getString("scheme")),
                                                MdFactory.text(ee.getValue().asObject().getString("bearerFormat"))
                                        ))
                                .build()
                        );
                        break;
                    }
                    case "oauth2": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Oauth2)"));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
//                        all.add(MdFactory
//                                .table().addColumns(
//                                        MdFactory.column().setName("SCHEME"),
//                                        MdFactory.column().setName("BEARER")
//                                )
//                                .addRows(MdFactory.row()
//                                        .addCells(
//                                                MdFactory.text(ee.getValue().asObject().getString("scheme")),
//                                                MdFactory.text(ee.getValue().asObject().getString("bearerFormat"))
//                                        ))
//                        );
                        break;
                    }
                    case "openIdConnect": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (OpenId Connect)"));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("URL")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.text(ee.getValue().asObject().getString("openIdConnectUrl"))
                                        ))
                                .build()
                        );
                        break;
                    }
                    default: {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (" + type + ")"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                    }
                }
            }
        }

    }


    private void _fillSchemaTypes(NutsObjectElement entries, List<MdElement> all) {
        Map<String, TypeInfo> allTypes = openApiParser.parseTypes(entries);
        if (allTypes.isEmpty()) {
            return;
        }
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, "SCHEMA TYPES"));
        for (Map.Entry<String, TypeInfo> entry : allTypes.entrySet()) {
            TypeInfo v = entry.getValue();
            if ("object".equals(v.type)) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(3, entry.getKey()));
                String d1 = v.description;
                String d2 = v.summary;
                if (!NutsBlankable.isBlank(d1) && !NutsBlankable.isBlank(d2)) {
                    all.add(MdFactory.text(d1));
                    all.add(MdFactory.text(". "));
                    all.add(MdFactory.text(d2));
                } else if (!NutsBlankable.isBlank(d1)) {
                    all.add(MdFactory.text(d1));
                } else if (!NutsBlankable.isBlank(d2)) {
                    all.add(MdFactory.text(d2));
                }
                MdTableBuilder mdTableBuilder = MdFactory.table().addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("TYPE"),
                        MdFactory.column().setName("DESCRIPTION"),
                        MdFactory.column().setName("EXAMPLE")
                );
                for (FieldInfo p : v.fields) {
                    mdTableBuilder.addRows(
                            MdFactory.row().addCells(
                                    MdFactory.text(p.name),
                                    MdFactory.codeBacktick3("", toCode(p.schema, false, "") + (p.required ? " [required]" : " [optional]")),
                                    MdFactory.text(p.description == null ? "" : p.description.trim()),
                                    MdFactory.text(p.example == null ? "" : p.example.trim())
                            )
                    );
                }
                all.add(mdTableBuilder.build());
            }
            if (!NutsBlankable.isBlank(v.example)) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.text("EXAMPLE:"));
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.codeBacktick3("", v.example.toString()));
            }
        }
    }

    private void _fillApiPaths(NutsObjectElement entries, List<MdElement> all) {
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, "API PATHS"));
        NutsObjectElement schemas = entries.getSafeObject("components").getSafeObject("schemas");
        NutsElements prv = NutsElements.of(appContext.getSession());
        for (NutsElementEntry path : entries.get(prv.ofString("paths")).asObject()) {
            String url = path.getKey().asString();
            Map<String, NutsObjectElement> calls = new HashMap<>();
            String dsummary = null;
            String ddescription = null;
            NutsArrayElement dparameters = null;
            for (NutsElementEntry ss : path.getValue().asObject()) {
                String k = ss.getKey().asString();
                switch (k) {
                    case "summary": {
                        dsummary = ss.getValue().asString();
                        break;
                    }
                    case "description": {
                        ddescription = ss.getValue().asString();
                        break;
                    }
                    case "parameters": {
                        dparameters = ss.getValue().asArray();
                        break;
                    }
                    default: {
                        calls.put(k, ss.getValue().asObject());
                    }
                }
            }
            for (Map.Entry<String, NutsObjectElement> ee : calls.entrySet()) {
                _fillApiPathMethod(ee.getKey(), ee.getValue(), all, url, prv, dsummary, ddescription, dparameters, schemas);
            }
        }
    }

    private void _fillServerList(NutsObjectElement entries, List<MdElement> all) {
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, "SERVER LIST"));
        NutsElements prv = NutsElements.of(appContext.getSession());
        for (NutsElement srv : entries.getArray(prv.ofString("servers"))) {
            NutsObjectElement srvObj = (NutsObjectElement) srv.asObject();
            all.add(MdFactory.title(4, srvObj.getString("url")));
            all.add(MdFactory.text(srvObj.getString("description")));
            NutsElement vars = srvObj.get(prv.ofString("variables"));
            if (vars != null && !vars.isEmpty()) {
                MdTableBuilder mdTableBuilder = MdFactory.table().addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("SPEC"),
                        MdFactory.column().setName("DESCRIPTION")
                );
                for (NutsElementEntry variables : vars.asObject()) {
                    mdTableBuilder.addRows(
                            MdFactory.row().addCells(
                                    MdFactory.text(variables.getKey().asString()),
                                    //                                MdFactory.text(variables.getValue().asObject().getString("enum")),
                                    MdFactory.text(variables.getValue().asObject().getString("default")),
                                    MdFactory.text(variables.getValue().asObject().getString("description"))
                            )
                    );
                }
                all.add(mdTableBuilder.build());
            }
        }
    }

    private MdDocument toMarkdown(InputStream inputStream, boolean json,String folder) {
        MdDocumentBuilder doc = new MdDocumentBuilder();
        List<String> options=new ArrayList<>(
                Arrays.asList(
                        ":source-highlighter: coderay",
                        ":icons: font",
                        ":icon-set: pf",
                        ":doctype: book",
                        ":toc:",
                        ":toclevels: 3",
                        ":appendix-caption: Appx",
                        ":sectnums:",
                        ":chapter-label:"
                )
        );
        if(Files.exists(Paths.get(folder).resolve("logo.png"))){
            options.add(":title-logo-image: logo.png");
        }
        doc.setProperty("headers", options.toArray(new String[0]));
        doc.setDate(LocalDate.now());
        doc.setSubTitle("RESTRICTED - INTERNAL");

        NutsElement obj = loadElement(inputStream, json);
        List<MdElement> all = new ArrayList<>();
        NutsObjectElement entries = obj.asObject();
        all.add(MdFactory.endParagraph());
        String documentTitle = entries.getObject("info").getString("title");
        doc.setTitle(documentTitle);
        String documentVersion = entries.getObject("info").getString("version");
        doc.setVersion(documentVersion);

        all.add(MdFactory.title(1, documentTitle));
//        all.add(new MdImage(null,null,"Logo, 64,64","./logo.png"));
//        all.add(MdFactory.endParagraph());
//        all.add(MdFactory.seq(MdFactory.text("API Reference")));
        _fillIntroduction(entries, all);
        _fillServerList(entries, all);
        _fillHeaders(entries, all);
        _fillSecuritySchemes(entries, all);
        _fillApiPaths(entries, all);
        _fillSchemaTypes(entries, all);
        doc.setContent(MdFactory.seq(all));
        return doc.build();
    }


    private void _fillApiPathMethodParam(List<NutsElement> headerParameters, List<MdElement> all) {
        MdTable tab = new MdTable(
                new MdColumn[]{
                        new MdColumn(MdFactory.text("NAME"), MdHorizontalAlign.LEFT),
                        new MdColumn(MdFactory.text("TYPE"), MdHorizontalAlign.LEFT),
                        new MdColumn(MdFactory.text("DESCRIPTION"), MdHorizontalAlign.LEFT),
                        new MdColumn(MdFactory.text("EXAMPLE"), MdHorizontalAlign.LEFT)
                },
                headerParameters.stream().map(
                        headerParameter -> {
                            boolean pdeprecated=Boolean.parseBoolean(headerParameter.asObject().getString("pdeprecated"));
                            String type = _StringUtils.nvl(headerParameter.asObject().getString("type"), "string")
                                    + (headerParameter.asObject().getBoolean("required") ? " [required]" : " [optional]")
                                    ;
                            return new MdRow(
                                    new MdElement[]{
                                            MdFactory.codeBacktick3("", _StringUtils.nvl(headerParameter.asObject().getString("name"),"unknown")
                                                    + (pdeprecated?" [DEPRECATED]":"")
                                            ),
                                            MdFactory.codeBacktick3("", type),
                                            MdFactory.text(_StringUtils.nvl(headerParameter.asObject().getString("description"),"")),
                                            MdFactory.text(_StringUtils.nvl(headerParameter.asObject().getString("example"),"")),
                                    }, false
                            );
                        }
                ).toArray(MdRow[]::new)
        );
        all.add(tab);
    }

    private void _fillApiPathMethod(String method, NutsObjectElement call, List<MdElement> all, String url, NutsElements prv, String dsummary, String ddescription, NutsArrayElement dparameters, NutsObjectElement schemas) {
        String nsummary = call.getString("summary");
        if (nsummary == null) {
            nsummary = dsummary;
        }
        String ndescription = call.getString("description");
        if (ndescription == null) {
            ndescription = ddescription;
        }
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, method.toUpperCase() + " " + url));
        all.add(MdFactory.text(nsummary));
        all.add(MdFactory.endParagraph());
        all.add(
                MdFactory.codeBacktick3("", "[" + method.toUpperCase() + "] " + url)
        );
        all.add(MdFactory.endParagraph());
        if (ndescription != null) {
            all.add(MdFactory.text(ndescription));
            all.add(MdFactory.endParagraph());
        }
        NutsArrayElement parameters = call.getArray(prv.ofString("parameters"));
        if (parameters == null) {
            parameters = dparameters;
        }
        if (parameters == null) {
            parameters = NutsArrayElementBuilder.of(appContext.getSession()).build();
        }
        List<NutsElement> headerParameters = parameters.stream().filter(x -> "header".equals(x.asObject().getString("in"))).collect(Collectors.toList());
        List<NutsElement> queryParameters = parameters.stream().filter(x -> "query".equals(x.asObject().getString("in"))).collect(Collectors.toList());
        List<NutsElement> pathParameters = parameters.stream().filter(x -> "path".equals(x.asObject().getString("in"))).collect(Collectors.toList());
        NutsObjectElement requestBody = call.getObject("requestBody");
        if (
                !headerParameters.isEmpty()
                        || !queryParameters.isEmpty()
                        || !pathParameters.isEmpty()
                        || (requestBody != null && !requestBody.isEmpty())

        ) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(4, "REQUEST"));

            if (!headerParameters.isEmpty()) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, "HEADER PARAMETERS"));
                _fillApiPathMethodParam(headerParameters, all);
            }
            if (!pathParameters.isEmpty()) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, "PATH PARAMETERS"));
                _fillApiPathMethodParam(pathParameters, all);
            }
            if (!queryParameters.isEmpty()) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, "QUERY PARAMETERS"));
                _fillApiPathMethodParam(queryParameters, all);
            }
            if (requestBody != null && !requestBody.isEmpty()) {
                boolean required = requestBody.getBoolean("required");
                String desc = requestBody.getString("description");
                NutsObjectElement r = requestBody.getObject("content");
                for (NutsElementEntry ii : r) {
                    all.add(MdFactory.endParagraph());
                    all.add(MdFactory.title(5, "REQUEST BODY - " + ii.getKey() + (required ? " [required]" : "[optional]")));
                    all.add(MdFactory.text(desc));
                    TypeInfo o = openApiParser.parseOneType(ii.getValue().asObject(), null);
                    if (o.ref != null) {
                        all.add(MdFactory.title(5, "REQUEST TYPE - " + o.ref));
                    } else {
                        all.add(MdFactory.codeBacktick3("javascript", toCode(o, true, "")));
                    }
                }
            }
        }

        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(4, "RESPONSE"));
        call.getObject("responses").stream()
                .forEach(x -> {
                    NutsElement s = x.getKey();
                    NutsElement v = x.getValue();
                    all.add(MdFactory.endParagraph());
                    all.add(MdFactory.title(5, "STATUS CODE - " + s));
                    all.add(MdFactory.text(v.asObject().getString("description")));
                    for (NutsElementEntry content : v.asObject().getObject("content")) {
                        TypeInfo o = openApiParser.parseOneType(content.getValue().asObject(), null);
                        if (o.userType.equals("$ref")) {
                            if(NutsBlankable.isBlank(o.example)) {
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName("RESPONSE MODEL"),
                                                MdFactory.column().setName("RESPONSE TYPE")
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        MdFactory.text(content.getKey().asString()),
                                                        MdFactory.text(o.ref)
                                                )
                                        ).build()
                                );
                            }else if(o.example.toString().trim().length()<=maxExampleInlineLength){
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName("RESPONSE MODEL"),
                                                MdFactory.column().setName("RESPONSE TYPE"),
                                                MdFactory.column().setName("EXAMPLE")
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        MdFactory.text(content.getKey().asString()),
                                                        MdFactory.text(o.ref),
                                                        NutsBlankable.isBlank(o.example) ? MdFactory.text("") : MdFactory.codeBacktick3("json", o.example.toString())
                                                )
                                        ).build()
                                );
                            }else{
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName("RESPONSE MODEL"),
                                                MdFactory.column().setName("RESPONSE TYPE"),
                                                MdFactory.column().setName("EXAMPLE")
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        MdFactory.text(content.getKey().asString()),
                                                        MdFactory.text(o.ref),
                                                        MdFactory.text("SEE BELOW...")
                                                )
                                        ).build()
                                );
                                all.add(MdFactory.codeBacktick3("json", "\n"+o.example.toString()));
                            }
                        } else {
                            all.add(MdFactory.endParagraph());
                            all.add(MdFactory.title(6, "RESPONSE MODEL - " + content.getKey()));
//                        all.add(MdFactory.endParagraph());
                            if (o.ref != null) {
                                all.add(MdFactory.title(6, "RESPONSE TYPE - " + o.ref));
                            } else {
                                all.add(MdFactory.codeBacktick3("javascript", "\n"+toCode(o, true, "")));
                            }
                        }
                    }
                });
    }

    private String toCode(TypeInfo o, boolean includeDesc, String indent) {
        String descSep = "";
        if (includeDesc) {
            if (!NutsBlankable.isBlank(o.description)) {
                descSep = " // " + o.description;
            }
        }
        if (o.ref != null) {
            return o.ref + descSep;
        } else if (o.userType.equals("object")) {
            StringBuilder sb = new StringBuilder("{");
            for (FieldInfo p : o.fields) {
                sb.append("\n").append(indent).append("  ").append(p.name).append(": ").append(toCode(p.schema, includeDesc, indent + "  "));
            }
            sb.append("\n").append(indent).append("}");
            sb.append(descSep);
            return sb.toString();
        } else {
            String type = o.userType;
            switch (o.userType) {
                case "string":
                case "enum":
                {
                    if (!NutsBlankable.isBlank(o.minLength) && !NutsBlankable.isBlank(o.maxLength)) {
                        type += ("[" + o.minLength.trim() + "," + o.maxLength.trim() + "]");
                    } else if (!NutsBlankable.isBlank(o.minLength)) {
                        type += (">=" + o.minLength.trim());
                    } else if (!NutsBlankable.isBlank(o.maxLength)) {
                        type += ("<=" + o.minLength.trim());
                    }
                    if (o.enumValues != null && o.enumValues.size() > 0) {
                        type += " ALLOWED {";
                        type += o.enumValues.stream().map(x -> x == null ? "null" : ("'" + x + "'")).collect(Collectors.joining(", "));
                        type += "}";
                    }
                    break;
                }
                case "integer":
                case "number": {
                    if (!NutsBlankable.isBlank(o.minLength) && !NutsBlankable.isBlank(o.maxLength)) {
                        type += ("[" + o.minLength.trim() + "," + o.maxLength.trim() + "]");
                    } else if (!NutsBlankable.isBlank(o.minLength)) {
                        type += (">=" + o.minLength.trim());
                    } else if (!NutsBlankable.isBlank(o.maxLength)) {
                        type += ("<=" + o.minLength.trim());
                    }
                    if (o.enumValues != null && o.enumValues.size() > 0) {
                        type += " ALLOWED {";
                        type += o.enumValues.stream().map(x -> x == null ? "null" : x).collect(Collectors.joining(", "));
                        type += "}";
                    }
                    break;
                }
                case "boolean": {
                    if (o.enumValues != null && o.enumValues.size() > 0) {
                        type += " ALLOWED {";
                        type += o.enumValues.stream().map(x -> x == null ? "null" : x).collect(Collectors.joining(", "));
                        type += "}";
                    }
                }
            }
            return type + descSep;
        }
    }

}
