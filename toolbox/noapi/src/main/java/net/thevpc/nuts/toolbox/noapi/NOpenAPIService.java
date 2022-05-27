package net.thevpc.nuts.toolbox.noapi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.asciidoctor.AsciiDoctorWriter;
import net.thevpc.nuts.spi.NutsPaths;
import net.thevpc.nuts.text.NutsTextStyle;
import net.thevpc.nuts.text.NutsTexts;
import net.thevpc.nuts.util.NutsStringUtils;
import org.asciidoctor.Asciidoctor;
import org.asciidoctor.OptionsBuilder;
import org.asciidoctor.SafeMode;
import org.yaml.snakeyaml.Yaml;

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
    private int maxExampleInlineLength = 80;
    private Properties httpCodes = new Properties();

    public NOpenAPIService(NutsApplicationContext appContext) {
        this.appContext = appContext;
    }

    public void run(String source, String target, boolean keep) {
        try (InputStream is = getClass().getResourceAsStream("/net/thevpc/nuts/toolbox/noapi/http-codes.properties")) {
            httpCodes.load(is);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
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
                temp = NutsPaths.of(session)
                        .createTempFile("temp.adoc", session).toString();
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
            throw new NutsUnsupportedOperationException(session);
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
        Path path = Paths.get(source).normalize().toAbsolutePath();
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
        Path sourcePath = Paths.get(source).normalize().toAbsolutePath();
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
//            return NutsElements.of(session).json().parse(inputStream, NutsElement.class);
            final Object o = new Yaml().load(inputStream);
            return NutsElements.of(session).toElement(o);
        }
    }

    private static class Vars {
        Map<String, String> m;

        public Vars(Map<String, String> m) {
            this.m = m;
        }

        public String format(String a) {
            return NutsStringUtils.replaceDollarString(a, s -> m.get(s));
        }
    }

    private Vars _fillVars(NutsObjectElement entries) {
        Map<String, String> m = new LinkedHashMap<>();

        NutsOptional<NutsObjectElement> v = entries.getObject("variables");
        if (v.isPresent()) {
            for (NutsElementEntry entry : v.get().entries()) {
                m.put(entry.getKey().toString(), entry.getValue().toString());
            }
        }
        return new Vars(m);
    }

    private void _fillIntroduction(NutsObjectElement entries, List<MdElement> all, Vars vars2) {
        NutsSession session = appContext.getSession();
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, "INTRODUCTION"));
        all.add(MdFactory.endParagraph());
        NutsObjectElement info = entries.getObject("info").orElse(NutsObjectElement.ofEmpty(session));
        all.add(MdFactory.text(info.getString("description").orElse("").trim()));
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, "CONTACT"));
        all.add(MdFactory.text("Any question, comments or request related to this API document should be addressed to :"));
        all.add(MdFactory.endParagraph());
        NutsObjectElement contact = info.getObject("contact").orElse(NutsObjectElement.ofEmpty(session));
        all.add(MdFactory.table()
                .addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("EMAIL"),
                        MdFactory.column().setName("URL")
                )
                .addRows(
                        MdFactory.row().addCells(
                                MdFactory.text(contact.getString("name").orElse("")),
                                MdFactory.text(contact.getString("email").orElse("")),
                                MdFactory.text(contact.getString("url").orElse(""))
                        )
                ).build()
        );
    }

    private void _fillHeaders(NutsObjectElement entries, List<MdElement> all, Vars vars2) {
        NutsSession session = appContext.getSession();
        NutsObjectElement components = entries.getObject("components").orElse(NutsObjectElement.ofEmpty(session));
        if (!components.getObject("headers").isEmpty()) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(3, "HEADERS"));
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.text("This section includes common HTTP Headers to be included in the incoming requests."));
            all.add(MdFactory.endParagraph());
            MdTableBuilder table = MdFactory.table()
                    .addColumns(
                            MdFactory.column().setName("NAME"),
                            MdFactory.column().setName("TYPE"),
                            MdFactory.column().setName("DESCRIPTION")
                    );

            for (NutsElementEntry ee : components.getObject("headers").orElse(NutsObjectElement.ofEmpty(session))) {
                String k = ee.getKey().toString();
                k = k + (ee.getValue().asObject().get(session).getBoolean("deprecated").orElse(false) ? " [DEPRECATED]" : "");
                k = k + MdFactory.text(ee.getValue().asObject().get(session).getBoolean("required").orElse(false) ? " [REQUIRED]" : "");
                table.addRows(
                        MdFactory.row().addCells(
                                MdFactory.codeBacktick3("", k),
                                MdFactory.codeBacktick3("", ee.getValue().asObject().get(session).getObject("schema")
                                        .orElse(NutsObjectElement.ofEmpty(session))
                                        .getString("type").orElse("")),
                                MdFactory.text(ee.getValue().asObject().get(session).getString("description").orElse(""))
                        )
                );
            }
            all.add(table.build());
        }
    }

    private void _fillSecuritySchemes(NutsObjectElement entries, List<MdElement> all, Vars vars2) {
        NutsSession session = appContext.getSession();
        NutsObjectElement components = entries.getObject("components").orElse(NutsObjectElement.ofEmpty(session));
        NutsObjectElement securitySchemes = components.getObject("securitySchemes").orElse(NutsObjectElement.ofEmpty(session));
        if (!securitySchemes.isEmpty()) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(3, "SECURITY AND AUTHENTICATION"));
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.text("This section includes security configuration."));
            for (NutsElementEntry ee : securitySchemes) {
                String type = ee.getValue().asObject().get(session).getString("type").orElse("");
                switch (type) {
                    case "apiKey": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Api Key)"));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.text(vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("NAME"),
                                        MdFactory.column().setName("IN")
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
                        all.add(MdFactory.text(
                                vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("SCHEME"),
                                        MdFactory.column().setName("BEARER")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.text(vars2.format(ee.getValue().asObject().get(session).getString("scheme").orElse(""))),
                                                MdFactory.text(vars2.format(ee.getValue().asObject().get(session).getString("bearerFormat").orElse("")))
                                        ))
                                .build()
                        );
                        break;
                    }
                    case "oauth2": {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (Oauth2)"));
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.text(vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
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
                        all.add(MdFactory.text(ee.getValue().asObject().get(session).getString("description").orElse("")));
                        all.add(MdFactory
                                .table().addColumns(
                                        MdFactory.column().setName("URL")
                                )
                                .addRows(MdFactory.row()
                                        .addCells(
                                                MdFactory.text(ee.getValue().asObject().get(session).getString("openIdConnectUrl").orElse(""))
                                        ))
                                .build()
                        );
                        break;
                    }
                    default: {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.title(4, ee.getKey() + " (" + type + ")"));
                        all.add(MdFactory.text(vars2.format(ee.getValue().asObject().get(session).getString("description").orElse(""))));
                    }
                }
            }
        }

    }


    private void _fillSchemaTypes(NutsObjectElement entries, List<MdElement> all, Vars vars2, List<TypeCrossRef> typeCrossRefs) {
        Map<String, TypeInfo> allTypes = openApiParser.parseTypes(entries, appContext.getSession());
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
                    if (!NutsBlankable.isBlank(d2) && !d2.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                } else if (!NutsBlankable.isBlank(d1)) {
                    all.add(MdFactory.text(d1));
                    if (!NutsBlankable.isBlank(d1) && !d1.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                } else if (!NutsBlankable.isBlank(d2)) {
                    all.add(MdFactory.text(d2));
                    if (!NutsBlankable.isBlank(d2) && !d2.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                }
                List<TypeCrossRef> types = typeCrossRefs.stream().filter(x -> x.type.equals(v.name)).collect(Collectors.toList());
                if (types.size() > 0) {
                    all.add(MdFactory.endParagraph());
                    all.add(MdFactory.text("This type is used in :"));
                    all.add(MdFactory.endParagraph());
                    for (TypeCrossRef type : types) {
                        all.add(MdFactory.ul(1,
                                MdFactory.ofListOrEmpty(
                                        new MdElement[]{
                                                MdFactory.codeBacktick3("", type.url),
                                                MdFactory.text(" (" + type.location + ")"),
                                        }
                                )
                        ));
                    }
                    all.add(MdFactory.endParagraph());
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
                                    jsonText(p.example)
                            )
                    );
                }
                all.add(mdTableBuilder.build());
            }
            if (!NutsBlankable.isBlank(v.example)) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.text("EXAMPLE:"));
                all.add(MdFactory.endParagraph());
                all.add(jsonText(v.example));
            }
        }
    }


    private void _fillApiPaths(NutsObjectElement entries, List<MdElement> all, Vars vars2, List<TypeCrossRef> typeCrossRefs) {
        NutsSession session = appContext.getSession();
        NutsElements prv = NutsElements.of(session);
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, "API PATHS"));
        int apiSize = entries.get(prv.ofString("paths")).flatMap(NutsElement::asObject).get(session).size();
        all.add(MdFactory.text("The exposed API defines "+apiSize+" end-point(s)."));
        all.add(MdFactory.endParagraph());
        for (NutsElementEntry path : entries.get(prv.ofString("paths")).flatMap(NutsElement::asObject).get(session)) {
            String url = path.getKey().asString().get(session);
            all.add(MdFactory.ul(1,MdFactory.codeBacktick3("", url)));
        }
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.text("The following details each of them."));
        NutsObjectElement schemas = entries.getObjectByPath("components", "schemas").orNull();
        for (NutsElementEntry path : entries.get(prv.ofString("paths")).flatMap(NutsElement::asObject).get(session)) {
            String url = path.getKey().asString().get(session);
            Map<String, NutsObjectElement> calls = new HashMap<>();
            String dsummary = null;
            String ddescription = null;
            NutsArrayElement dparameters = null;
            for (NutsElementEntry ss : path.getValue().asObject().get(session)) {
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
            for (Map.Entry<String, NutsObjectElement> ee : calls.entrySet()) {
                _fillApiPathMethod(ee.getKey(), ee.getValue(), all, url, prv, dsummary, ddescription, dparameters, schemas, typeCrossRefs);
            }
        }
    }

    private void _fillServerList(NutsObjectElement entries, List<MdElement> all, Vars vars2) {
        NutsSession session = appContext.getSession();
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, "SERVER LIST"));
        all.add(MdFactory.text("This API end-points are accessible via the following URL(s). Please append any API path to the desired server URL to build a valid request path."));
        NutsElements prv = NutsElements.of(session);
        for (NutsElement srv : entries.getArray(prv.ofString("servers")).orElse(prv.ofEmptyArray())) {
            NutsObjectElement srvObj = (NutsObjectElement) srv.asObject().orElse(prv.ofEmptyObject());
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(4, vars2.format(srvObj.getString("url").orNull())));
            all.add(MdFactory.text(vars2.format(srvObj.getString("description").orNull())));
            NutsElement vars = srvObj.get(prv.ofString("variables")).orNull();
            if (vars != null && !vars.isEmpty()) {
                MdTableBuilder mdTableBuilder = MdFactory.table().addColumns(
                        MdFactory.column().setName("NAME"),
                        MdFactory.column().setName("SPEC"),
                        MdFactory.column().setName("DESCRIPTION")
                );
                for (NutsElementEntry variables : vars.asObject().get(session)) {
                    mdTableBuilder.addRows(
                            MdFactory.row().addCells(
                                    MdFactory.text(variables.getKey().asString().get(session)),
                                    //                                MdFactory.text(variables.getValue().asObject().getString("enum")),
                                    MdFactory.text(vars2.format(variables.getValue().asObject().get(session).getString("default").orNull())),
                                    MdFactory.text(vars2.format(variables.getValue().asObject().get(session).getString("description").orNull()))
                            )
                    );
                }
                all.add(mdTableBuilder.build());
            }
        }
    }

    private MdDocument toMarkdown(InputStream inputStream, boolean json, String folder) {
        NutsSession session = appContext.getSession();
        MdDocumentBuilder doc = new MdDocumentBuilder();
        List<String> options = new ArrayList<>(
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
        if (Files.exists(Paths.get(folder).resolve("logo.png"))) {
            options.add(":title-logo-image: " + Paths.get(folder).resolve("logo.png").normalize().toAbsolutePath().toString());
        }
        doc.setProperty("headers", options.toArray(new String[0]));
        doc.setDate(LocalDate.now());
        doc.setSubTitle("RESTRICTED - INTERNAL");

        NutsElement obj = loadElement(inputStream, json);
        NutsElements prv = NutsElements.of(session);
        List<MdElement> all = new ArrayList<>();
        NutsObjectElement entries = obj.asObject().get(session);
        all.add(MdFactory.endParagraph());
        NutsObjectElement infoObj = entries.getObject("info").orElse(prv.ofEmptyObject());
        String documentTitle = infoObj.getString("title").orNull();
        doc.setTitle(documentTitle);
        String documentVersion = infoObj.getString("version").orNull();
        doc.setVersion(documentVersion);

        all.add(MdFactory.title(1, documentTitle));
//        all.add(new MdImage(null,null,"Logo, 64,64","./logo.png"));
//        all.add(MdFactory.endParagraph());
//        all.add(MdFactory.seq(MdFactory.text("API Reference")));
        Vars vars = _fillVars(entries);
        List<TypeCrossRef> typeCrossRefs = new ArrayList<>();
        _fillIntroduction(entries, all, vars);
        _fillServerList(entries, all, vars);
        _fillHeaders(entries, all, vars);
        _fillSecuritySchemes(entries, all, vars);
        _fillApiPaths(entries, all, vars, typeCrossRefs);
        _fillSchemaTypes(entries, all, vars, typeCrossRefs);
        doc.setContent(MdFactory.seq(all));
        return doc.build();
    }


    private void _fillApiPathMethodParam(List<NutsElement> headerParameters, List<MdElement> all, String url, List<TypeCrossRef> typeCrossRefs, String paramType) {
        NutsSession session = appContext.getSession();
        MdTable tab = new MdTable(
                new MdColumn[]{
                        new MdColumn(MdFactory.text("NAME"), MdHorizontalAlign.LEFT),
                        new MdColumn(MdFactory.text("TYPE"), MdHorizontalAlign.LEFT),
                        new MdColumn(MdFactory.text("DESCRIPTION"), MdHorizontalAlign.LEFT),
                        new MdColumn(MdFactory.text("EXAMPLE"), MdHorizontalAlign.LEFT)
                },
                headerParameters.stream().map(
                        headerParameter -> {
                            NutsObjectElement obj = headerParameter.asObject().orElse(NutsElements.of(session).ofEmptyObject());
                            boolean pdeprecated = obj.getBoolean("pdeprecated").orElse(false);
                            String type = _StringUtils.nvl(obj.getString("type").orNull(), "string")
                                    + (obj.getBoolean("required").orElse(false) ? " [required]" : " [optional]");
                            typeCrossRefs.add(new TypeCrossRef(
                                    obj.getString("type").orElse(""), url, paramType
                            ));
                            return new MdRow(
                                    new MdElement[]{
                                            MdFactory.codeBacktick3("", _StringUtils.nvl(obj.getString("name").orNull(), "unknown")
                                                    + (pdeprecated ? " [DEPRECATED]" : "")
                                            ),
                                            MdFactory.codeBacktick3("", type),
                                            MdFactory.text(_StringUtils.nvl(obj.getString("description").orElse(""), "")),
                                            jsonText(obj.getString("example").orElse("")),
                                    }, false
                            );
                        }
                ).toArray(MdRow[]::new)
        );
        all.add(tab);
    }

    private void _fillApiPathMethod(String method, NutsObjectElement call, List<MdElement> all, String url, NutsElements prv, String dsummary, String ddescription, NutsArrayElement dparameters, NutsObjectElement schemas, List<TypeCrossRef> typeCrossRefs) {
        NutsSession session = appContext.getSession();
        String nsummary = call.getString("summary").orElse(dsummary);
        String ndescription = call.getString("description").orElse(ddescription);
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, method.toUpperCase() + " " + url));
        all.add(MdFactory.text(nsummary));
        if (!NutsBlankable.isBlank(nsummary) && !nsummary.endsWith(".")) {
            all.add(MdFactory.text("."));
        }
        all.add(MdFactory.endParagraph());
        all.add(
                MdFactory.codeBacktick3("", "[" + method.toUpperCase() + "] " + url)
        );
        all.add(MdFactory.endParagraph());
        if (ndescription != null) {
            all.add(MdFactory.text(ndescription));
            if (!NutsBlankable.isBlank(ndescription) && !ndescription.endsWith(".")) {
                all.add(MdFactory.text("."));
            }
            all.add(MdFactory.endParagraph());
        }
        NutsArrayElement parameters = call.getArray(prv.ofString("parameters"))
                .orElseUse(() -> NutsOptional.of(dparameters))
                .orElseGet(() -> NutsArrayElementBuilder.of(session).build());
        List<NutsElement> headerParameters = parameters.stream().filter(x -> "header".equals(x.asObject().get(session).getString("in").orNull())).collect(Collectors.toList());
        List<NutsElement> queryParameters = parameters.stream().filter(x -> "query".equals(x.asObject().get(session).getString("in").orNull())).collect(Collectors.toList());
        List<NutsElement> pathParameters = parameters.stream().filter(x -> "path".equals(x.asObject().get(session).getString("in").orNull())).collect(Collectors.toList());
        NutsObjectElement requestBody = call.getObject("requestBody").orNull();
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
            all.add(MdFactory.title(4, "REQUEST"));

            // paragraph details the expected request parameters and body to be provided by the caller

            if((
                    (withRequestHeaderParameters?1:0)+
                    (withRequestQueryParameters?1:0)+
                    (withRequestPathParameters?1:0)+
                    (withRequestBody?1:0)
            )>1){
                all.add(MdFactory.text("This end-point requires various parameter types to be provided by the caller."));
            }else if(withRequestHeaderParameters){
                all.add(MdFactory.text("This end-point requires Header Parameter(s) to be provided by the caller."));
            }else if(withRequestQueryParameters){
                all.add(MdFactory.text("This end-point requires Query (s) to be provided by the caller."));
            }else if(withRequestPathParameters){
                all.add(MdFactory.text("This end-point requires Path Parameter(s) to be provided by the caller."));
            }else if(withRequestBody){
                all.add(MdFactory.text("This end-point requires some input in its body to be provided by the caller."));
            }

            if (withRequestHeaderParameters) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, "HEADER PARAMETERS"));
                _fillApiPathMethodParam(headerParameters, all, url, typeCrossRefs, "Header Parameter");
            }
            if (withRequestPathParameters) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, "PATH PARAMETERS"));
                _fillApiPathMethodParam(pathParameters, all, url, typeCrossRefs, "Path Parameter");
            }
            if (withRequestQueryParameters) {
                all.add(MdFactory.endParagraph());
                all.add(MdFactory.title(5, "QUERY PARAMETERS"));
                _fillApiPathMethodParam(queryParameters, all, url, typeCrossRefs, "Query Parameter");
            }
            if (withRequestBody) {
                boolean required = requestBody.getBoolean("required").orElse(false);
                String desc = requestBody.getString("description").orElse("");
                NutsObjectElement r = requestBody.getObject("content").orElseGet(() -> NutsObjectElement.ofEmpty(session));
                for (NutsElementEntry ii : r) {
                    all.add(MdFactory.endParagraph());
                    all.add(MdFactory.title(5, "REQUEST BODY - " + ii.getKey() + (required ? " [required]" : "[optional]")));
                    all.add(MdFactory.text(desc));
                    if (!NutsBlankable.isBlank(desc) && !desc.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                    TypeInfo o = openApiParser.parseOneType(ii.getValue().asObject().get(session), null, session);
                    if (o.ref != null) {
                        typeCrossRefs.add(new TypeCrossRef(o.ref, url, "Request Body"));
//                        all.add(MdFactory.endParagraph());
//                        all.add(MdFactory.title(5, "REQUEST TYPE - " + o.ref));
                        all.add(MdFactory.text(" The request body is expected to be of type : "));
                        all.add(MdFactory.codeBacktick3("javascript", o.ref));
                        all.add(MdFactory.text(" which is detailed in 'SCHEMA TYPES' section"));

                        NutsElement s = schemas.get(o.ref).orNull();
                        NutsElement description = null;
                        NutsElement example = null;
                        if (s != null) {
                            description = s.asObject().get().get("description").orNull();
                            example = s.asObject().get().get("example").orNull();
                        }
                        MdTable tab = new MdTable(
                                new MdColumn[]{
                                        new MdColumn(MdFactory.text("NAME"), MdHorizontalAlign.LEFT),
                                        new MdColumn(MdFactory.text("TYPE"), MdHorizontalAlign.LEFT),
                                        new MdColumn(MdFactory.text("DESCRIPTION"), MdHorizontalAlign.LEFT),
                                        new MdColumn(MdFactory.text("EXAMPLE"), MdHorizontalAlign.LEFT)
                                },
                                new MdRow[]{
                                        new MdRow(
                                                new MdElement[]{
                                                        MdFactory.codeBacktick3("", "request-body"),
                                                        MdFactory.codeBacktick3("", o.ref),
                                                        MdFactory.text(_StringUtils.nvl(description == null ? null : description.toString(), "")),
                                                        jsonText(example),
                                                }, false
                                        )
                                }

                        );
                        all.add(tab);

                    } else {
                        all.add(MdFactory.endParagraph());
                        all.add(MdFactory.codeBacktick3("javascript", toCode(o, true, "")));
                    }
                }
            }
        }

        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(4, "RESPONSE"));
        all.add(MdFactory.text("This paragraph details main status codes and content as a response to the "));
        all.add(MdFactory.codeBacktick3("javascript", url));
        all.add(MdFactory.text(" path."));

        call.getObject("responses").get(session).stream()
                .forEach(x -> {
                    NutsElement s = x.getKey();
                    NutsElement v = x.getValue();
                    all.add(MdFactory.endParagraph());
                    String codeDescription = evalCodeDescription(s.toString());
                    all.add(MdFactory.title(5, "STATUS CODE - " + s
                            + (NutsBlankable.isBlank(codeDescription) ? "" : (" - " + codeDescription))
                    ));
                    String description = v.asObject().get(session).getString("description").orElse("");
                    all.add(MdFactory.text(description));
                    if (!NutsBlankable.isBlank(description) && !description.endsWith(".")) {
                        all.add(MdFactory.text("."));
                    }
                    for (NutsElementEntry content : v.asObject().get(session).getObject("content").orElse(NutsObjectElement.ofEmpty(session))) {
                        TypeInfo o = openApiParser.parseOneType(content.getValue().asObject().get(session), null, session);
                        if (o.userType.equals("$ref")) {
                            typeCrossRefs.add(new TypeCrossRef(
                                    o.ref,
                                    url, "Response (" + s + ")"
                            ));
                            if (NutsBlankable.isBlank(o.example)) {
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName("RESPONSE MODEL"),
                                                MdFactory.column().setName("RESPONSE TYPE")
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        MdFactory.text(content.getKey().asString().get(session)),
                                                        MdFactory.text(o.ref)
                                                )
                                        ).build()
                                );
                            } else if (o.example.toString().trim().length() <= maxExampleInlineLength) {
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName("RESPONSE MODEL"),
                                                MdFactory.column().setName("RESPONSE TYPE"),
                                                MdFactory.column().setName("EXAMPLE")
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        MdFactory.text(content.getKey().asString().get(session)),
                                                        MdFactory.text(o.ref),
                                                        jsonText(o.example)
                                                )
                                        ).build()
                                );
                            } else {
                                all.add(MdFactory.table()
                                        .addColumns(
                                                MdFactory.column().setName("RESPONSE MODEL"),
                                                MdFactory.column().setName("RESPONSE TYPE"),
                                                MdFactory.column().setName("EXAMPLE")
                                        )
                                        .addRows(
                                                MdFactory.row().addCells(
                                                        MdFactory.text(content.getKey().asString().get(session)),
                                                        MdFactory.text(o.ref),
                                                        MdFactory.text("SEE BELOW...")
                                                )
                                        ).build()
                                );
                                if (!NutsBlankable.isBlank(o.example)) {
                                    all.add(MdFactory.text("\n"));
                                    all.add(jsonText(o.example));
                                }
                            }
                        } else {
                            all.add(MdFactory.endParagraph());
                            all.add(MdFactory.title(6, "RESPONSE MODEL - " + content.getKey()));
//                        all.add(MdFactory.endParagraph());
                            if (o.ref != null) {
                                all.add(MdFactory.title(6, "RESPONSE TYPE - " + o.ref));
                            } else {
                                all.add(MdFactory.codeBacktick3("javascript", "\n" + toCode(o, true, "")));
                            }
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

    private MdElement jsonText(Object example) {
        if (NutsBlankable.isBlank(example)) {
            return MdFactory.text("");
        }
        String e = jsonTextString(example);
        return MdFactory.codeBacktick3("json", e);
    }

    private String jsonTextString(Object example) {
        if (example instanceof NutsPrimitiveElement) {
            return ((NutsPrimitiveElement) example).toStringLiteral();
        }
        if (example instanceof NutsElementEntry) {
            return
                    jsonTextString(((NutsElementEntry) example).getKey())
                            + " : "
                            + jsonTextString(((NutsElementEntry) example).getValue());
        }
        if (example instanceof NutsArrayElement) {
            StringBuilder sb = new StringBuilder();
            sb.append("[");
            Collection<NutsElement> entries = ((NutsArrayElement) example).items();
            sb.append(
                    entries.stream().map(this::jsonTextString).collect(Collectors.joining(", "))
            );
            sb.append("]");
            return sb.toString();
        }
        if (example instanceof NutsObjectElement) {
            StringBuilder sb = new StringBuilder();
            sb.append("{");
            Collection<NutsElementEntry> entries = ((NutsObjectElement) example).entries();
            sb.append(
                    entries.stream().map(this::jsonTextString).collect(Collectors.joining(", "))
            );
            sb.append("}");
            return sb.toString();
        }
        return example.toString();
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
                case "enum": {
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

    private class TypeCrossRef {
        private String url;
        private String location;
        private String type;

        public TypeCrossRef(String type, String url, String location) {
            this.url = url;
            this.location = location;
            this.type = type;
        }
    }
}
