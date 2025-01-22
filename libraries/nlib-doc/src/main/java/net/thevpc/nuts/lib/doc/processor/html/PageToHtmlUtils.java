package net.thevpc.nuts.lib.doc.processor.html;

import net.thevpc.nuts.lib.doc.processor.pages.MPage;
import net.thevpc.nuts.lib.doc.util.HtmlBuffer;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.NColors;
import net.thevpc.nuts.util.NStringUtils;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PageToHtmlUtils {
    public HtmlBuffer.Node pageContent2html(MPage page) {
        switch (page.getType()) {
            case MARKDOWN:
                return md2html(page.getMarkdownContent());
            case NTF:
                NText ntfContent = page.getNtfContent();
                List<NText> nnormalized = normalizeText(ntfContent);
                List<HtmlBuffer.Node> list =
                        nnormalized.stream()
                                .map(x -> ntf2html(x))
                                .collect(Collectors.toList());
                ;
                return new HtmlBuffer.Tag("pre").body(new HtmlBuffer.TagList(list).setNewLine(false));
        }
        throw new IllegalArgumentException("unsupported page: " + page);
    }

    private List<NText> normalizeText(NText text) {
        return NTexts.of().flatten(text, new NTextTransformConfig()
                        .setFlatten(true)
                        .setNormalize(true)
                        .setApplyTheme(true)
                        .setThemeName(null)//perhaps override
                        .setBasicTrueStyles(true)
                        .setThemeName("whiteboard")
                )
                .toList();
    }

    private HtmlBuffer.Node ntf2html(NText elem) {
        switch (elem.getType()) {
            case PLAIN: {
                return new HtmlBuffer.Plain(((NTextPlain) elem).getText());
            }
            case LINK: {
                NTextLink lnk = (NTextLink) elem;
                return new HtmlBuffer.Tag("a")
                        .attr("href", lnk.getText())
                        .attr("class", "md-link")
                        .body(lnk.getText());
            }
            case TITLE: {
                NTextTitle title = (NTextTitle) elem;
                HtmlBuffer.Tag t = new HtmlBuffer.Tag("H4")
                        .attr("class", "md-title-" + title.getLevel())
                        .body(
                                ntf2html(title.getChild())
                        );
                return t;
            }
            case LIST: {
                List<HtmlBuffer.Node> nnn = new ArrayList<>();
                NTextList ll = (NTextList) elem;
                for (NText child : ll.getChildren()) {
                    nnn.add(ntf2html(child));
                }
                return new HtmlBuffer.TagList(nnn.toArray(new HtmlBuffer.Node[0]));
            }
            case CODE: {
                NTextCode c = (NTextCode) elem;
                String type = "default";
                String language = c.getQualifier();
                String text = c.getText();
                boolean inline = false;
                if (inline) {
                    String value = text;
                    if (value.matches("[a-zA-Z0-9_-]+")) {
                        return (new HtmlBuffer.Tag("mark")
                                .attr("class", "md-code md-code-" + type + " " + language)
                                .body(escapeCode(value)));
                    }
                    return (new HtmlBuffer.Tag("code")
                            .attr("class", "md-code md-code-" + type + " " + language)
                            .body(escapeCode(value)));
                }
                return (new HtmlBuffer.Tag("pre")
                        .attr("class", "md-code md-code-" + type + " " + language)
                        .body(
                                (new HtmlBuffer.Tag("code").attr("class", language).body(escapeCode(text)))
                        ));
            }
            case STYLED: {
                NTextStyled style = (NTextStyled) elem;
                NTextStyles styles = style.getStyles();
                NText c = style.getChild();
                Set<String> hstyles = new HashSet<>();
                Set<String> hclasses = new HashSet<>();
                HtmlBuffer.Tag t = new HtmlBuffer.Tag("span");
//                hstyles.add("display: inline");
                boolean blink = false;
                for (NTextStyle st : styles) {
                    switch (st.getType()) {
                        case BOLD: {
                            hstyles.add("font-weight: bold");
                            break;
                        }
                        case PLAIN: {
                            break;
                        }
                        case ITALIC: {
                            hstyles.add("font-style: italic");
                        }
                        case BLINK: {
                            blink = true;
                            break;
                        }
                        case UNDERLINED: {
                            hstyles.add("text-decoration: underline");
                            break;
                        }
                        case REVERSED: {
                            hstyles.add("-webkit-filter: invert(100%)");
                            hstyles.add("filter: invert(100%)");
                            break;
                        }
                        case STRIKED: {
                            hstyles.add("text-decoration:line-through");
                            break;
                        }
                        case BACK_TRUE_COLOR: {
                            Color cl=new Color(st.getVariant());
                            hstyles.add("background-color: " + NColors.toHtmlHex(cl));
                            break;
                        }
                        case FORE_TRUE_COLOR: {
                            Color cl=new Color(st.getVariant());
                            hstyles.add("color: " + NColors.toHtmlHex(cl));
                            break;
                        }
                        default: {
                            List<NText> ee = normalizeText(elem);
                            hstyles.add("color: red");
                        }
                    }
                }
                t.attr("style", String.join(";", hstyles));
                t.attr("class", String.join(" ", hclasses));
                t.body(ntf2html(style.getChild()));
                if (blink) {
                    t = new HtmlBuffer.Tag("blink").body(t);
                }
                return t;
            }
        }
        return new HtmlBuffer.Plain(elem.toString());
    }


    public HtmlBuffer.Node md2html(MdElement markdown) {
        if (markdown == null) {
            return null;
        }
        switch (markdown.type().group()) {
            case TEXT: {
                MdText text = markdown.asText();
                return new HtmlBuffer.Plain(text.getText());
            }
            case PHRASE: {
                HtmlBuffer.Tag p = new HtmlBuffer.Tag("p")
                        .attr("class", "md-phrase");
                for (MdElement child : markdown.asPhrase().getChildren()) {
                    p.body(md2html(child));
                }
                return p;
            }
            case BODY: {
                return new HtmlBuffer.TagList(
                        Arrays.stream(markdown.asBody().getChildren())
                                .map(x -> md2html(x))
                                .toArray(HtmlBuffer.Node[]::new)
                );
            }
            case TITLE: {
                MdTitle title = markdown.asTitle();
                HtmlBuffer.Tag t = new HtmlBuffer.Tag("H4")
                        .attr("class", "md-title-" + title.getDepth())
                        .body(
                                md2html(title.getValue())
                        );
                List<HtmlBuffer.Node> nnn = new ArrayList<>();
                nnn.add(t);
                for (MdElement child : title.getChildren()) {
                    nnn.add(md2html(child));
                }
                return new HtmlBuffer.TagList(nnn.toArray(new HtmlBuffer.Node[0]));
            }
            case ITALIC: {
                return (new HtmlBuffer.Tag("i")
                        .attr("class", "md-italic")
                        .body(md2html(markdown.asItalic().getContent())));
            }
            case BOLD: {
                return (new HtmlBuffer.Tag("b")
                        .attr("class", "md-bold")
                        .body(md2html(markdown.asBold().getContent())));
            }
            case CODE: {
                MdCode code = markdown.asCode();
                String type = code.getType();
                String language = code.getLanguage();
                if (code.isInline()) {
                    String value = code.getValue();
                    if (value.matches("[a-zA-Z0-9_-]+")) {
                        return (new HtmlBuffer.Tag("mark")
                                .attr("class", "md-code md-code-" + type + " " + language)
                                .body(escapeCode(value)));
                    }
                    return (new HtmlBuffer.Tag("code")
                            .attr("class", "md-code md-code-" + type + " " + language)
                            .body(escapeCode(value)));
                }
                return (new HtmlBuffer.Tag("pre")
                        .attr("class", "md-code md-code-" + type + " " + language)
                        .body(
                                (new HtmlBuffer.Tag("code").attr("class", language).body(escapeCode(code.getValue())))
                        ));
            }
            case UNNUMBERED_ITEM: {
                HtmlBuffer.Tag li = new HtmlBuffer.Tag("li")
                        .attr("class", "md-uli")
                        .body(md2html(markdown.asUnNumItem().getValue()));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child));
                }
                return li;
            }
            case NUMBERED_ITEM: {
                HtmlBuffer.Tag li = new HtmlBuffer.Tag("li")
                        .attr("class", "md-oli")
                        .body(md2html(markdown.asNumItem().getValue()));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child));
                }
                return li;
            }
            case UNNUMBERED_LIST: {
                MdUnNumberedList li = markdown.asUnNumList();
                HtmlBuffer.Tag hli = new HtmlBuffer.Tag("ul")
                        .attr("class", "md-ul");
                for (MdUnNumberedItem child : li.getChildren()) {
                    hli.body(md2html(child));
                }
                return hli;
            }
            case NUMBERED_LIST: {
                MdUnNumberedList li = markdown.asUnNumList();
                HtmlBuffer.Tag hli = new HtmlBuffer.Tag("ol")
                        .attr("class", "md-ol");
                for (MdUnNumberedItem child : li.getChildren()) {
                    hli.body(new HtmlBuffer.Tag("li")
                            .attr("class", "md-oli")
                            .body(md2html(child)));
                }
                return hli;
            }
            case IMAGE: {
                MdImage li = markdown.asImage();
                return new HtmlBuffer.Tag("p").body(
                        new HtmlBuffer.Tag("img")
                                .attr("src", li.getImageUrl())
                                .attr("class", "md-img img-fluid border")
                                .attr("alt", li.getImageTitle())

                )
                        ;
            }
            case LINK: {
                MdLink li = markdown.asLink();
                return new HtmlBuffer.Tag("a")
                        .attr("href", li.getLinkUrl())
                        .attr("class", "md-link")
                        .body(li.getLinkTitle());
            }
            case HORIZONTAL_RULE: {
                return new HtmlBuffer.Tag("hr").attr("class", "divider")
                        .attr("class", "md-hr")
                        .setNoEnd(true);
            }
            case LINE_BREAK: {
                return new HtmlBuffer.Tag("hr").attr("class", "divider")
                        .attr("class", "md-br")
                        .setNoEnd(true);
            }
            case ADMONITION: {
                MdAdmonition li = markdown.asAdmonition();
                switch (li.getType()) {
                    case WARNING: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-warning mb-4")
                                .body(new HtmlBuffer.TagList(
                                        new HtmlBuffer.Tag("span").attr("class", "badge badge-danger text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                    case DANGER: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-danger mb-4")
                                .body(new HtmlBuffer.TagList(
                                        new HtmlBuffer.Tag("span").attr("class", "badge badge-danger text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                    case INFO: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-info mb-4")
                                .body(new HtmlBuffer.TagList(
//                                        new HtmlBuffer.Tag("span").attr("class","badge badge-danger text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
//                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                    case TIP: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-success mb-4")
                                .body(new HtmlBuffer.TagList(
//                                        new HtmlBuffer.Tag("span").attr("class","badge badge-danger text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
//                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                    case IMPORTANT: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-success mb-4")
                                .body(new HtmlBuffer.TagList(
//                                        new HtmlBuffer.Tag("span").attr("class","badge badge-danger text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
//                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                    case NOTE: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-info mb-4")
                                .body(new HtmlBuffer.TagList(
                                        new HtmlBuffer.Tag("span").attr("class", "badge badge-success text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                    case CAUTION: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-info mb-4")
                                .body(new HtmlBuffer.TagList(
                                        new HtmlBuffer.Tag("span").attr("class", "badge badge-danger text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                    default: {
                        return new HtmlBuffer.Tag("div").attr("class", "alert alert-info mb-4")
                                .body(new HtmlBuffer.TagList(
                                        new HtmlBuffer.Tag("span").attr("class", "badge badge-danger text-uppercase").body(new HtmlBuffer.Plain("NOTE:")),
                                        new HtmlBuffer.Plain(" "),
                                        md2html(li.getContent())
                                ));
                    }
                }

            }
            case TABLE: {
                MdTable t = markdown.asTable();
                HtmlBuffer.Tag table = new HtmlBuffer.Tag("table").attr("class", "table table-bordered table-striped");
                MdColumn[] columns = t.getColumns();
                List<HtmlBuffer.Node> rows = new ArrayList<>();

                HtmlBuffer.Tag th = new HtmlBuffer.Tag("tr");
                th.body(new HtmlBuffer.TagList(
                        Arrays.stream(columns).map(x -> new HtmlBuffer.Tag("th").body(md2html(x.getName()))).collect(Collectors.toList())
                ));
                rows.add(th);
                for (MdRow row : t.getRows()) {
                    HtmlBuffer.Tag tr = new HtmlBuffer.Tag("tr");
                    tr.body(new HtmlBuffer.TagList(
                            Arrays.stream(row.getCells()).map(x -> new HtmlBuffer.Tag("td").body(md2html(x))).collect(Collectors.toList())
                    ));
                    rows.add(tr);
                }
                table.body(new HtmlBuffer.TagList(rows));
                return table;
            }
            case XML: {
                MdXml xml = markdown.asXml();
                switch (xml.getTag().toLowerCase()) {
                    case "tabs": {
                        return md2htmlXmlTabs(xml);
                    }
                }
                return null;
            }
        }
        return null;
    }

    private HtmlBuffer.Node md2htmlXmlTabs(MdXml xml) {
        String dv = xml.getProperties().get("defaultValue");
        String valuesString = xml.getProperties().get("values");
        //Map<String, String> map = valuesString == null ? null : NElements.of().json().parse(valuesString, Map.class);
        List<HtmlBuffer.Node> allHeader = new ArrayList<>();
        List<HtmlBuffer.Node> allContent = new ArrayList<>();
        String newUuid = "id" + UUID.randomUUID().toString().replace("-", "");
        MdElement[] children = xml.getContent().asBody().getChildren();
        for (int i = 0; i < children.length; i++) {
            MdElement c = children[i];
            MdXml cx = c.asXml();
            String tabValue = cx.getProperties().get("value");
            String tabLabel = NStringUtils.firstNonBlank(tabValue, cx.getProperties().get("label"));
            String active = Objects.equals(dv, tabValue) ? "active" : "";
            String currId = newUuid + i;
            {
                HtmlBuffer.Tag h = new HtmlBuffer.Tag("li").attr("class", "nav-item").attr("role", "presentation");
                HtmlBuffer.Tag a = new HtmlBuffer.Tag("a").attr("class", "nav-link " + active).attr("id", currId + "-tab").attr("data-toggle", "tab")
                        .attr("href", "#" + newUuid + i).attr("role", "tab").attr("aria-controls", currId).attr("aria-selected", active.equals("active") ? "true" : "false")
                        .body(new HtmlBuffer.Plain(tabLabel));
                h.body(a);
                allHeader.add(h);
            }
            {
                HtmlBuffer.Tag h = new HtmlBuffer.Tag("div").attr("class", "tab-pane fade show " + active).attr("id", currId).attr("role", "tabpanel").attr("aria-labelledby", currId + "-tab");
                h.body(md2html(cx.getContent()));
                allContent.add(h);
            }

        }
        return new HtmlBuffer.TagList(
                new HtmlBuffer.Tag("ul").attr("class", "nav nav-tabs").attr("role", "tablist")
                        .body(new HtmlBuffer.TagList(allHeader)),
                new HtmlBuffer.Tag("div").attr("class", "tab-content my-3")
                        .body(new HtmlBuffer.TagList(allContent))
        );
    }

    private static String escapeCode(String value) {
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            switch (c) {
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '&':
                    sb.append("&amp;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }
}
