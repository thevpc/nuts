package net.thevpc.nuts.lib.doc.executor.expr.fct;

import net.thevpc.nuts.expr.NExprDeclarations;
import net.thevpc.nuts.expr.NExprNodeValue;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.lib.doc.context.NDocContext;
import net.thevpc.nuts.lib.doc.executor.expr.BaseNexprNExprFct;
import net.thevpc.nuts.lib.doc.pages.MdPage;
import net.thevpc.nuts.lib.doc.pages.MdPageLoader;
import net.thevpc.nuts.lib.doc.util.StringUtils;
import net.thevpc.nuts.util.NComparator;
import net.thevpc.nuts.util.NStringUtils;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class FormatDate extends BaseNexprNExprFct {
    public FormatDate() {
        super("formatDate");
    }

    @Override
    public Object eval(String name, List<NExprNodeValue> args, NExprDeclarations context) {
        if (args.size() != 1 && args.size() != 2) {
            throw new IllegalStateException(name + " : invalid arguments count");
        }
        NDocContext fcontext = fcontext(context);

        Object d = args.get(0).getValue().orNull();
        if(d==null){
            return "";
        }
        String dateFormatPattern = "yyyy-MM-dd";
        if(d instanceof Date){
            return new SimpleDateFormat(dateFormatPattern).format((Date) d);
        }
        if(d instanceof Instant){
            return new SimpleDateFormat(dateFormatPattern).format(Date.from((Instant) d));
        }
        return "";
    }
}
