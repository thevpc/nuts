package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.pages.MdPage;
import net.thevpc.nuts.lib.doc.util.HtmlBuffer;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.docusaurus.TextReader;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PageToHtml extends BaseNexprNExprFct {
    public PageToHtml() {
        super("pageToHtml");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);

        MdPage str = (MdPage) args.get(0).getValue().orNull();
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(str) + ")");
        if (str == null) {
            return "";
        }
        NStringBuilder sb = new NStringBuilder();
        sb.append(new HtmlBuffer.Tag(str.level == 0 ? "H1"
                : str.level == 1 ? "H2"
                : str.level == 2 ? "H3"
                : "H4"
        ).body(str.title));
        sb.newLine();
        sb.append(md2html(str.getMarkdown()));
        return sb.toString();
    }

    private HtmlBuffer.Node md2html(MdElement markdown) {
        if (markdown == null) {
            return null;
        }
        switch (markdown.type().group()) {
            case TEXT: {
                MdText text = markdown.asText();
                String text1 = text.getText();
                List<HtmlBuffer.Node> textOrUrls = new ArrayList<>();
                Pattern urlPatternFull = Pattern.compile("\\[(?<name>[^\\[\\]]*)\\]\\(\\s*(?<url>(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|])\\s*\\)");
                Pattern urlPattern = Pattern.compile("(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]");
                String text1Remainings = text1;
                while (!text1Remainings.isEmpty()) {
                    Matcher matcher1 = urlPatternFull.matcher(text1Remainings);
                    if (matcher1.find()) {
                        int s = matcher1.start();
                        if (s > 0) {
                            textOrUrls.add(new HtmlBuffer.Plain(text1Remainings.substring(0, s)));
                        }
                        textOrUrls.add((new HtmlBuffer.Tag("a")
                                .attr("href", matcher1.group("url"))
                                .body(matcher1.group("name"))));
                        text1Remainings = text1Remainings.substring(matcher1.end());
                    } else {
                        Matcher matcher = urlPattern.matcher(text1Remainings);
                        if (matcher.find()) {
                            int s = matcher.start();
                            if (s > 0) {
                                textOrUrls.add(new HtmlBuffer.Plain(text1Remainings.substring(0, s)));
                            }
                            textOrUrls.add((new HtmlBuffer.Tag("a")
                                    .attr("href", matcher.group())
                                    .body(matcher.group())));
                            text1Remainings = text1Remainings.substring(matcher.end());
                        } else {
                            textOrUrls.add(new HtmlBuffer.Plain(text1Remainings));
                            text1Remainings = "";
                        }
                    }
                }
                HtmlBuffer.Node n = textOrUrls.size() == 1 ? textOrUrls.get(0) : new HtmlBuffer.TagList(textOrUrls);
                if (!text.isInline()) {
                    n = (new HtmlBuffer.Tag("p")
                            //.attr("class", "lead")
                            .body(n));
                }
                return n;
            }
            case PHRASE: {
                HtmlBuffer.Tag p = new HtmlBuffer.Tag("p");
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
                HtmlBuffer.Tag t = new HtmlBuffer.Tag("H4").body(
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
                return (new HtmlBuffer.Tag("i").body(md2html(markdown.asItalic().getContent())));
            }
            case BOLD: {
                return (new HtmlBuffer.Tag("b").body(md2html(markdown.asBold().getContent())));
            }
            case CODE: {
                MdCode code = markdown.asCode();
                String type = code.getType();
                String language = code.getLanguage();
                if (code.isInline()) {
                    return (new HtmlBuffer.Tag("code").body(escapeCode(code.getValue())));
                }
                return (new HtmlBuffer.Tag("pre").body(
                        (new HtmlBuffer.Tag("code").attr("class", language).body(escapeCode(code.getValue())))
                ));
            }
            case UNNUMBERED_ITEM: {
                HtmlBuffer.Tag li = new HtmlBuffer.Tag("li").body(md2html(markdown.asUnNumItem().getValue()));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child));
                }
                return li;
            }
            case NUMBERED_ITEM: {
                HtmlBuffer.Tag li = new HtmlBuffer.Tag("li").body(md2html(markdown.asNumItem().getValue()));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child));
                }
                return li;
            }
            case UNNUMBERED_LIST: {
                MdUnNumberedList li = markdown.asUnNumList();
                HtmlBuffer.Tag hli = new HtmlBuffer.Tag("ul");
                for (MdUnNumberedItem child : li.getChildren()) {
                    hli.body(md2html(child));
                }
                return hli;
            }
            case NUMBERED_LIST: {
                MdUnNumberedList li = markdown.asUnNumList();
                HtmlBuffer.Tag hli = new HtmlBuffer.Tag("ol");
                for (MdUnNumberedItem child : li.getChildren()) {
                    hli.body(new HtmlBuffer.Tag("li").body(md2html(child)));
                }
                return hli;
            }

        }
        return null;
    }

    private String escapeCode(String value) {
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
