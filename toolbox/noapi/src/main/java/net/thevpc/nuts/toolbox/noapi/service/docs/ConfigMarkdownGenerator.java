package net.thevpc.nuts.toolbox.noapi.service.docs;

import net.thevpc.nuts.*;
import net.thevpc.nuts.elem.NutsElements;
import net.thevpc.nuts.elem.NutsObjectElement;
import net.thevpc.nuts.io.NutsPath;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.toolbox.noapi.util.AppMessages;
import net.thevpc.nuts.toolbox.noapi.util.NoApiUtils;
import net.thevpc.nuts.toolbox.noapi.service.OpenApiParser;
import net.thevpc.nuts.toolbox.noapi.model.ConfigVar;
import net.thevpc.nuts.toolbox.noapi.model.Vars;
import net.thevpc.nuts.util.NutsMaps;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ConfigMarkdownGenerator {
    private NutsApplicationContext appContext;
    private AppMessages msg;
    private OpenApiParser openApiParser = new OpenApiParser();
    private int maxExampleInlineLength = 80;

    public ConfigMarkdownGenerator(NutsApplicationContext appContext, AppMessages msg) {
        this.appContext = appContext;
        this.msg = msg;
    }

    public MdDocument createMarkdown(
            NutsObjectElement configElement,
            NutsObjectElement apiElement,
            NutsPath targetFolder,
            NutsPath sourceFolder, String apiDocumentFileName,
            Map<String, String> vars0, List<String> defaultAdocHeaders) {
        NutsSession session = appContext.getSession();
        NutsObjectElement openApiEntries = apiElement.asObject().get(session);
        NutsElements prv = NutsElements.of(session);
        NutsObjectElement infoObj = openApiEntries.getObject("info").orElse(prv.ofEmptyObject());
        String apiDocumentTitle = infoObj.getString("title").orNull();
        String apiDocumentVersion = infoObj.getString("version").orNull();
        String configDocumentVersion = configElement.getString("version").orNull();
        if (NutsBlankable.isBlank(configDocumentVersion)) {
            configDocumentVersion = apiDocumentVersion;
        }
        MdDocumentBuilder doc = new MdDocumentBuilder();
        String targetName = configElement.getString("target-name").get(session);
        String targetId = configElement.getString("target-id").get(session);
        String apiDocumentIdFromConfig = configElement.getString("openapi-document-id").get(session);
        String apiDocumentIdFromApi = apiElement.getStringByPath("custom", "openapi-document-id").get(session);
        if (!NutsBlankable.isBlank(apiDocumentIdFromConfig)) {
            if (!Objects.equals(apiDocumentIdFromConfig, apiDocumentIdFromApi)) {
                throw new NutsIllegalArgumentException(session, NutsMessage.ofCstyle("invalid api version %s <> %s", apiDocumentIdFromConfig, apiDocumentIdFromApi));
            }
        }
        List<String> options = new ArrayList<>(defaultAdocHeaders);
        if (sourceFolder.resolve("logo.png").exists()) {
            options.add(":title-logo-image: " + sourceFolder.resolve("logo.png").normalize().toAbsolute().toString());
        }
        doc.setProperty("headers", options.toArray(new String[0]));
        doc.setDate(LocalDate.now());
        doc.setSubTitle("RESTRICTED - INTERNAL");

        List<MdElement> all = new ArrayList<>();
        all.add(MdFactory.endParagraph());
        String configDocumentTitle = apiDocumentTitle + " Configuration : " + targetName;
        doc.setTitle(configDocumentTitle);
        doc.setVersion(configDocumentVersion);

        all.add(MdFactory.title(1, configDocumentTitle));
//        all.add(new MdImage(null,null,"Logo, 64,64","./logo.png"));
//        all.add(MdFactory.endParagraph());
//        all.add(MdFactory.seq(NoApiUtils.asText("API Reference")));

        Vars vars = OpenApiParser._fillVars(openApiEntries, vars0);

        List<ConfigVar> configVars = OpenApiParser.loadConfigVars(configElement, apiElement, vars, session);
        _fillIntroduction(configElement, openApiEntries, all, vars, apiDocumentFileName);
        _fillConfigVars(configElement, all, vars, configVars);
        doc.setContent(MdFactory.seq(all));
        return doc.build();
    }

    private void _fillIntroduction(NutsObjectElement configElement, NutsObjectElement apiElement,
                                   List<MdElement> all, Vars vars, String apiDocumentFileName) {
        NutsSession session = appContext.getSession();
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, msg.get("INTRODUCTION").get()));
        all.add(MdFactory.endParagraph());
        NutsObjectElement info = apiElement.getObject("info").orElse(NutsObjectElement.ofEmpty(session));
        all.add(NoApiUtils.asText(apiElement.getStringByPath("custom", "config", "description").orElse("").trim()));
        all.add(MdFactory.endParagraph());
        String targetName = configElement.getString("target-name").get(session);
        all.add(NoApiUtils.asText(
                NutsMessage.ofVstyle(msg.get("section.config.introduction.body").get(), NutsMaps.of("name", targetName)
                ).toString()));

        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, msg.get("CONTACT").get()));
        all.add(NoApiUtils.asText(
                msg.get("section.contact.body").get()
        ));
        all.add(MdFactory.endParagraph());
        NutsObjectElement contact = info.getObject("contact").orElse(NutsObjectElement.ofEmpty(session));
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
        all.add(MdFactory.title(3, msg.get("REFERENCE_DOCUMENTS").get()));
        all.add(NoApiUtils.asText(
                msg.get("section.reference-document.body").get()
        ));

        NutsObjectElement infoObj = apiElement.getObject("info").orElse(NutsObjectElement.ofEmpty(session));
        String apiDocumentTitle = infoObj.getString("title").orNull();
        String apiDocumentVersion = infoObj.getString("version").orNull();

        all.add(MdFactory.endParagraph());
        all.add(MdFactory.table()
                .addColumns(
                        MdFactory.column().setName(msg.get("NAME").get()),
                        MdFactory.column().setName(msg.get("VERSION").get()),
                        MdFactory.column().setName(msg.get("DOCUMENT").get())
                )
                .addRows(
                        MdFactory.row().addCells(
                                NoApiUtils.asText(apiDocumentTitle),
                                NoApiUtils.asText(apiDocumentVersion),
                                NoApiUtils.asText(apiDocumentFileName)
                        )
                ).build()
        );
    }


    private void _fillConfigVars(NutsObjectElement entries, List<MdElement> all, Vars vars, List<ConfigVar> configVars) {
        NutsSession session = appContext.getSession();
        String targetName = entries.getString("target-name").get(session);
        String observations = entries.getString("observations").orElse("");
        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(2, msg.get("CONFIGURATION").get()));
        all.add(NoApiUtils.asText(
                NutsMessage.ofVstyle(msg.get("section.config.body").get(), NutsMaps.of("name", targetName)
                ).toString()));

        if (!NutsBlankable.isBlank(observations)) {
            all.add(MdFactory.newLine());
            all.add(NoApiUtils.asText(vars.format(observations)));
        }

        all.add(MdFactory.endParagraph());
        all.add(MdFactory.title(3, msg.get("CUSTOM_PARAMETER_LIST").get()));
        all.add(NoApiUtils.asText(
                NutsMessage.ofVstyle(msg.get("section.config.customVars.body").get(), NutsMaps.of("name", targetName)
                ).toString()));
        all.add(MdFactory.endParagraph());

        for (ConfigVar configVar : configVars) {
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.title(4, configVar.getName()));
            all.add(NoApiUtils.asText(configVar.getDescription()));
            if (!NutsBlankable.isBlank(configVar.getObservations())) {
                all.add(MdFactory.endParagraph());
                all.add(NoApiUtils.asText(configVar.getObservations()));
            }
            all.add(MdFactory.endParagraph());
            all.add(MdFactory.codeBacktick3("", vars.format(configVar.getValue()), false));
        }
    }
}
