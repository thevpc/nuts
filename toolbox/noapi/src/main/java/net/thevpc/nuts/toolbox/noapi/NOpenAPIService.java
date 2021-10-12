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
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NOpenAPIService {

    private NutsApplicationContext appContext;

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
        if (targetType.equals("adoc")) {
            writeAdoc(md, target, appContext.getSession().isPlainTrace());
        } else if (targetType.equals("pdf")) {
            String temp = null;
            if (keep) {
                temp = addExtension(source, "adoc").toString();
            } else {
                temp = appContext.getSession().io().tmp()
                        .createTempFile("temp.adoc").toString();
            }
            writeAdoc(md, temp, keep && appContext.getSession().isPlainTrace());
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
            if (appContext.getSession().isPlainTrace()) {
                appContext.getSession().out().printf("generated pdf %s\n",
                        appContext.getSession().text().ofStyled(
                                target, NutsTextStyle.primary4()
                        )
                );
            }
            if (!keep) {
                new File(temp).delete();
            }
        } else {
            throw new NutsIllegalArgumentException(appContext.getSession(), NutsMessage.cstyle("unsupported"));
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
                    appContext.getSession().text().ofStyled(
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
        try (BufferedReader r = Files.newBufferedReader(Paths.get(source))) {
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
        try (InputStream inputStream = Files.newInputStream(Paths.get(source))) {
            return toMarkdown(inputStream, json);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    private NutsElement loadElement(InputStream inputStream, boolean json) {
        if (json) {
            return appContext.getSession().elem().setContentType(NutsContentType.JSON).parse(inputStream, NutsElement.class);
        } else {
            return appContext.getSession().elem().setContentType(NutsContentType.YAML).parse(inputStream, NutsElement.class);
//            final Object o = new Yaml().load(inputStream);
//            return appContext.getWorkspace().elem().toElement(o);
        }
    }

    private MdDocument toMarkdown(InputStream inputStream, boolean json) {
        NutsElementFormat prv = appContext.getSession().elem();
        MdDocumentBuilder doc = new MdDocumentBuilder();
        doc.setProperty("headers", new String[]{
            ":source-highlighter: coderay",
            ":icons: font",
            ":icon-set: pf",
            ":doctype: book",
            ":toc:",
            ":toclevels: 3",
            ":appendix-caption: Appx",
            ":sectnums:",
            ":chapter-label:"
        });
        doc.setDate(LocalDate.now());
        doc.setSubTitle("RESTRICTED - INTERNAL");

        NutsElement obj = loadElement(inputStream, json);
        List<MdElement> all = new ArrayList<>();
        NutsObjectElement entries = obj.asObject();
        String documentTitle = entries.getObject("info").getString("title");
        doc.setTitle(documentTitle);
        String documentVersion = entries.getObject("info").getString("version");
        doc.setVersion(documentVersion);

        all.add(MdFactory.title(1, documentTitle));
        all.add(MdFactory.seq(
                MdFactory.text("API Reference")
        ));
        all.add(MdFactory.title(2, "INTRODUCTION"));
        all.add(MdFactory.text(entries.getObject("info").getString("description").trim()));
        all.add(MdFactory.title(3, "CONTACT"));
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

        all.add(MdFactory.title(3, "SERVER LIST"));
        for (NutsElement srv : entries.getArray(prv.forString("servers"))) {
            NutsObjectElement srvObj = (NutsObjectElement) srv.asObject();
            all.add(MdFactory.title(4, srvObj.getString("url")));
            all.add(MdFactory.text(srvObj.getString("description")));
            NutsElement vars = srvObj.get(prv.forString("variables"));
            if (vars!=null && !vars.isEmpty()) {
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

        if (!entries.getObject("components").getObject("headers").isEmpty()) {
            all.add(MdFactory.title(3, "HEADERS"));
            all.add(MdFactory.text("This section includes common Headers to be included in the incoming requests."));
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
        if (!entries.getObject("components").getObject("securitySchemes").isEmpty()) {
            all.add(MdFactory.title(3, "SECURITY AND AUTHENTICATION"));
            all.add(MdFactory.text("This section includes security configurations."));
            for (NutsElementEntry ee : entries.getObject("components").getObject("securitySchemes")) {
                String type = ee.getValue().asObject().getString("type");
                switch (type) {
                    case "apiKey": {
                        all.add(MdFactory.title(4, ee.getKey() + " (Api Key)"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
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
                        all.add(MdFactory.title(4, ee.getKey() + " (Http)"));
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
                        all.add(MdFactory.title(4, ee.getKey() + " (Oauth2)"));
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
                        all.add(MdFactory.title(4, ee.getKey() + " (OpenId Connect)"));
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
                        all.add(MdFactory.title(4, ee.getKey() + " (" + type + ")"));
                        all.add(MdFactory.text(ee.getValue().asObject().getString("description")));
                    }
                }
            }
        }
        all.add(MdFactory.title(2, "API"));
        for (NutsElementEntry path : entries.get(prv.forString("paths")).asObject()) {
            String url = path.getKey().asString();
            for (NutsElementEntry ss : path.getValue().asObject()) {
                String method = ss.getKey().asString();
                NutsObjectElement call = ss.getValue().asObject();
                all.add(MdFactory.title(3, method.toUpperCase() + " " + url));
                all.add(MdFactory.text(call.getString("summary")));
                all.add(
                        MdFactory.codeBacktick3("", "[" + method.toUpperCase() + "] " + url)
                );
                all.add(MdFactory.text(call.getString("description")));
                all.add(MdFactory.title(4, "REQUEST"));
                List<NutsElement> headerParameters = call.getArray(prv.forString("parameters")).stream().filter(x -> x.asObject().getString("in").equals("header")).collect(Collectors.toList());
                List<NutsElement> queryParameters = call.getArray(prv.forString("parameters")).stream().filter(x -> x.asObject().getString("in").equals("query")).collect(Collectors.toList());
                if (!headerParameters.isEmpty()) {
                    all.add(MdFactory.title(5, "HEADER PARAMETERS"));
                    MdTable tab = new MdTable(
                            new MdColumn[]{new MdColumn(MdFactory.text("NAME"), MdHorizontalAlign.LEFT),
                                new MdColumn(MdFactory.text("TYPE"), MdHorizontalAlign.LEFT),
                                new MdColumn(MdFactory.text("REQUIRED"), MdHorizontalAlign.LEFT),
                                new MdColumn(MdFactory.text("DESCRIPTION"), MdHorizontalAlign.LEFT)},
                            headerParameters.stream().map(
                                    headerParameter -> new MdRow(
                                            new MdElement[]{
                                                MdFactory.codeBacktick3("", headerParameter.asObject().getString("name")),
                                                MdFactory.codeBacktick3("", headerParameter.asObject().getString("type")),
                                                MdFactory.text(headerParameter.asObject().getBoolean("required") ? "required" : ""),
                                                MdFactory.text(headerParameter.asObject().getString("description"))
                                            }, false
                                    )
                            ).toArray(MdRow[]::new)
                    );
                    all.add(tab);
                }
                if (!queryParameters.isEmpty()) {
                    all.add(MdFactory.title(5, "QUERY PARAMETERS"));
                    MdTable tab = new MdTable(
                            new MdColumn[]{new MdColumn(MdFactory.text("NAME"), MdHorizontalAlign.LEFT),
                                new MdColumn(MdFactory.text("TYPE"), MdHorizontalAlign.LEFT),
                                new MdColumn(MdFactory.text("REQUIRED"), MdHorizontalAlign.LEFT),
                                new MdColumn(MdFactory.text("DESCRIPTION"), MdHorizontalAlign.LEFT)},
                            queryParameters.stream().map(
                                    headerParameter -> new MdRow(
                                            new MdElement[]{
                                                MdFactory.codeBacktick3("", headerParameter.asObject().getString("name")),
                                                MdFactory.codeBacktick3("", headerParameter.asObject().getString("type")),
                                                MdFactory.text(headerParameter.asObject().getBoolean("required") ? "required" : ""),
                                                MdFactory.text(headerParameter.asObject().getString("description"))
                                            }, false
                                    )
                            ).toArray(MdRow[]::new)
                    );
                    all.add(tab);
                }
                NutsObjectElement requestBody = call.getObject("requestBody");
                if (requestBody!=null && !requestBody.isEmpty()) {
                    boolean required = requestBody.getBoolean("required");
                    String desc = requestBody.getString("description");
                    NutsObjectElement r = requestBody.getObject("content");
                    for (NutsElementEntry ii : r) {
                        all.add(MdFactory.title(5, "REQUEST BODY - " + ii.getKey() + (required ? " [required]" : "")));
                        all.add(MdFactory.text(desc));
                        all.add(MdFactory.codeBacktick3("javascript", toCode(ii.getValue(), "")));
                    }
                }

                all.add(MdFactory.title(4, "RESPONSE"));
                call.getObject("responses").stream()
                        .forEach(x -> {
                            NutsElement s = x.getKey();
                            NutsElement v = x.getValue();
                            all.add(MdFactory.title(5, "STATUS CODE - " + s));
                            all.add(MdFactory.text(v.asObject().getString("description")));
                            for (NutsElementEntry content : v.asObject().getObject("content")) {
                                all.add(MdFactory.title(6, "RESPONSE MODEL - " + content.getKey()));
                                all.add(MdFactory.codeBacktick3("javascript", toCode(content.getValue(), "")));
                            }
                        });
            }
        }
        doc.setContent(MdFactory.seq(all));
        return doc.build();
    }

    private String toCode(NutsElement o, String indent) {
        NutsElementFormat prv = appContext.getSession().elem();
        String descSep = " // ";
        if (o.isObject()) {
            NutsElement a = o.asObject().get(prv.forString("schema"));
            if (a!=null && a.isObject()) {
                NutsObjectElement schema = o.asObject().getObject("schema");
                String t = schema.getString("type");
                if (t.equals("object")) {
                    StringBuilder sb = new StringBuilder("{");
                    for (NutsElementEntry p : schema.getObject("properties")) {
                        sb.append("\n" + indent + "  " + p.getKey() + ": " + toCode(p.getValue(), indent + "  "));
                    }
                    sb.append("\n" + indent + "}");
                    NutsElement desc = o.asObject().get(prv.forString("description"));
                    if (desc!=null && !desc.asString().isEmpty()) {
                        return sb + descSep + desc.asString();
                    }
                    return sb.toString();
                }
            } else if (o.asObject().get(prv.forString("type")).isString()) {
                String t = o.asObject().get(prv.forString("type")).asString();
                if (t.equals("object")) {
                    StringBuilder sb = new StringBuilder("{");
                    for (NutsElementEntry p : o.asObject().getObject("properties")) {
                        sb.append("\n" + indent + "  " + p.getKey() + ": " + toCode(p.getValue(), indent + "  "));
                    }
                    sb.append("\n" + indent + "}");
                    NutsElement desc = o.asObject().get(prv.forString("description"));
                    if (desc!=null && !desc.asString().isEmpty()) {
                        return sb + descSep + desc.asString();
                    }
                    return sb.toString();
                } else {
                    NutsElement anEnum = o.asObject().get(prv.forString("enum"));
                    if (t.equals("boolean")) {
                        NutsElement ee = o.asObject().get(prv.forString("example"));
                        if (ee!=null && !ee.isNull()) {
                            if (ee.isString()) {
                                return ee.asString();
                            }
                            return ee.asString();
                        }
                        NutsElement desc = o.asObject().get(prv.forString("description"));
                        NutsArrayElement en = anEnum==null?null:anEnum.asArray();
                        if (en!=null && en.isEmpty()) {
                            String r = "boolean ALLOWED:" + en.stream().map(x -> x.isNull() ? "null" : x.asString()).collect(Collectors.joining(", "));
                            if (!desc.asString().isEmpty()) {
                                return r + descSep + desc.asString();
                            }
                            return r;
                        } else {
                            if (desc!=null && !desc.asString().isEmpty()) {
                                return "boolean" + descSep + desc.asString();
                            }
                            return "boolean";
                        }
                    } else if (t.equals("string")) {
                        NutsElement ee = o.asObject().get(prv.forString("example"));
                        if (ee!=null && !ee.isNull()) {
                            if (ee.isString()) {
                                return "\'" + ee.asString() + "\'";
                            }
                            return "\'" + ee.asString() + "\'";
                        }
                        NutsElement desc = o.asObject().get(prv.forString("description"));
                        NutsArrayElement en = anEnum==null?null:anEnum.asArray();
                        if (en!=null && !en.isEmpty()) {
                            String r = "string ALLOWED:" + en.stream().map(x -> x.isNull() ? "null" : x.asString()).collect(Collectors.joining(", "));
                            if (desc!=null && !desc.asString().isEmpty()) {
                                return r + descSep + desc.asString();
                            }
                            return r;
                        } else {
                            if (desc!=null && !desc.asString().isEmpty()) {
                                return "string" + descSep + desc.asString();
                            }
                            return "string";
                        }
                    } else if (t.equals("integer")) {
                        NutsElement desc = o.asObject().get(prv.forString("description"));
                        NutsArrayElement en = anEnum==null?null:anEnum.asArray();
                        if (en!=null && !en.isEmpty()) {
                            String r = "integer ALLOWED:" + en.stream().map(x -> x.isNull() ? "null" : x.asString()).collect(Collectors.joining(", "));
                            if (!desc.asString().isEmpty()) {
                                return r + descSep + desc.asString();
                            }
                            return r;
                        } else {
                            if (desc!=null && !desc.asString().isEmpty()) {
                                return "integer" + descSep + desc.asString();
                            }
                            return "integer";
                        }
                    }
                }
            } else if (o.asObject().get(prv.forString("type"))==null || o.asObject().get(prv.forString("type")).isNull()) {
                NutsElement desc = o.asObject().get(prv.forString("description"));
                if (desc!=null && !desc.asString().isEmpty()) {
                    return "null" + "\n" + desc.asString();
                }
                return "null";
            }
        }
        return "";
    }

}
