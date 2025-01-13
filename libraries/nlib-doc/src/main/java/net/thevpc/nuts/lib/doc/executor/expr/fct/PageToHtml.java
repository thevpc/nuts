package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.pages.PageGroup;
import net.thevpc.nuts.lib.doc.util.HtmlBuffer;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        PageGroup str = (PageGroup) args.get(0).getValue().orNull();
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
                if(text.isInline()){
                    return new HtmlBuffer.Plain(text.getText());
                }
                return (new HtmlBuffer.Tag("p")
                        //.attr("class", "lead")
                        .body(text.getText()));
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
                List<HtmlBuffer.Node> nnn=new ArrayList<>();
                nnn.add(t);
                for (MdElement child : title.getChildren()) {
                    nnn.add(md2html(child));
                }
                return new HtmlBuffer.TagList(nnn.toArray(new HtmlBuffer.Node[0]));
            }
            case ITALIC:{
                return (new HtmlBuffer.Tag("i").body(md2html(markdown.asItalic().getContent())));
            }
            case BOLD:{
                return (new HtmlBuffer.Tag("b").body(md2html(markdown.asBold().getContent())));
            }
            case CODE:{
                MdCode code = markdown.asCode();
                String type = code.getType();
                String language = code.getLanguage();
                if(NBlankable.isBlank(language)){
                    return (new HtmlBuffer.Tag("code").body(code.getValue()));
                }
                return (new HtmlBuffer.Tag("code").attr("class",language).body(code.getValue()));
//                return (new HtmlBuffer.Tag("pre").body(code.getValue()));
            }
            case UNNUMBERED_ITEM:{
                HtmlBuffer.Tag li = new HtmlBuffer.Tag("li").body(md2html(markdown.asUnNumItem().getValue()));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child));
                }
                return li;
            }
            case NUMBERED_ITEM:{
                HtmlBuffer.Tag li = new HtmlBuffer.Tag("li").body(md2html(markdown.asNumItem().getValue()));
                for (MdElement child : markdown.asUnNumItem().getChildren()) {
                    li.body(md2html(child));
                }
                return li;
            }
            case UNNUMBERED_LIST:{
                MdUnNumberedList li = markdown.asUnNumList();
                HtmlBuffer.Tag hli = new HtmlBuffer.Tag("ul");
                for (MdUnNumberedItem child : li.getChildren()) {
                    hli.body(md2html(child));
                }
                return hli;
            }
            case NUMBERED_LIST:{
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
}
