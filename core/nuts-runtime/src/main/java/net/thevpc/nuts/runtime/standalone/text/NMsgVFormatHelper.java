package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        List<NText> dd = NStringUtils.parseDollarPlaceHolder(ss)
                .map(t -> {
                    switch (t.ttype) {
                        case NToken.TT_DOLLAR:
                        case NToken.TT_DOLLAR_BRACE: {
                            NText x = mapper.apply(t.sval);
                            if (x == null) {
                                throw new IllegalArgumentException("msg var not found " + t.sval);
                            }
                            return x;
                        }
                    }
                    return NText.ofPlain(t.sval);
                }).collect(Collectors.toList());
        NTextBuilder sb = NTextBuilder.of();
        sb.appendAll(dd);
        return sb.build();
    }
}
