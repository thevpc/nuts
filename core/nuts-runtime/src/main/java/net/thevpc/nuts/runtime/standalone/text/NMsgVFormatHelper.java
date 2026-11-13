package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Function;

public class NMsgVFormatHelper extends AbstractNMsgFormatHelper {

    private Function<String, NText> mapper = null;

    public NMsgVFormatHelper(NMsg m) {
        super(m);
        Object param = params == null ? (Collections.emptyMap()) : params[0];
        if (param instanceof Map) {
            mapper = x -> {
                Object u = ((Map<String, ?>) param).get(x);
                if (u == null) {
                    return null;
                }
                return NText.of(u);
            };
        } else {
            Function<String, ?> f = (Function<String, ?>) param;
            mapper = x -> {
                Object u = f.apply(x);
                if (u == null) {
                    Object v = applyPlaceholder(x);
                    if (v != null) {
                        u = v;
                    }
                }
                u=resolvePlaceholder(u);
                return NText.of(u);
            };
        }
    }


    protected NText formatPlain(String ss) {
        if (ss == null) {
            return NText.ofBlank();
        }
        NTextBuilder sb = NTextBuilder.of();
        NStringUtils.parseDollarPlaceHolder(ss).forEach(t -> {
            switch (t.ttype) {
                case NToken.TT_DOLLAR:
                case NToken.TT_DOLLAR_BRACE: {
                    NText x = mapper.apply(t.sval);
                    if (x == null) {
                        // unknown variable: keep the literal text, as NMsg.toString() does
                        sb.append(NText.ofPlain(t.image));
                    } else {
                        // NText values are preserved as-is (including their styles)
                        sb.append(x);
                    }
                    break;
                }
                default:
                    sb.append(NText.ofPlain(t.sval));
            }
        });
        return sb.build();
    }
}
