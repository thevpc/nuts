package net.thevpc.nuts.lib.doc.executor.expr.fct;

import com.google.common.reflect.TypeToken;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.pages.MdPage;
import net.thevpc.nuts.lib.doc.pages.MdToHtml;
import net.thevpc.nuts.lib.doc.util.HtmlBuffer;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.lib.md.*;
import net.thevpc.nuts.lib.md.docusaurus.TextReader;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NStringBuilder;
import net.thevpc.nuts.util.NStringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PageToHtml extends BaseNexprNExprFct {
    public PageToHtml() {
        super("pageToHtml");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1 && args.size() != 2) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);

        MdPage title = (MdPage) args.get(0).getValue().orNull();
        Object titlePrefix = args.size() > 1 ? args.get(1).getValue().orNull() : null;
        fcontext.getLog().debug("eval", name + "(" + StringUtils.toLiteralString(title) + ")");
        if (title == null) {
            return "";
        }
        NStringBuilder sb = new NStringBuilder();
        sb.append(new HtmlBuffer.Tag(title.level == 0 ? "H1"
                : title.level == 1 ? "H2"
                : title.level == 2 ? "H3"
                : "H4"
        ).body((titlePrefix != null ? (String.valueOf(titlePrefix) + " ") : "") + title.title));
        sb.newLine();
        sb.append(fcontext.md2Html().md2html(title.getMarkdown()));
        return sb.toString();
    }



}
