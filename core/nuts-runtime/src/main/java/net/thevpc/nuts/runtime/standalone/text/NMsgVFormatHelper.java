package net.thevpc.nuts.runtime.standalone.text;

import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NMsgVFormatHelper extends AbstractNMsgFormatHelper {

    private Function<String, NText> mapper = null;

    public NMsgVFormatHelper(NMsg m, NTexts txt) {
        super(m, txt);
        Object param = params == null ? (Collections.emptyMap()) : params[0];
        if (param instanceof Map) {
            mapper = x -> {
                Object u = ((Map<String, ?>) param).get(x);
                if (u == null) {
                    return null;
                }
                return txt.of(u);
            };
        } else {
            Function<String, ?> f = (Function<String, ?>) param;
            mapper = x -> {
                Object u = f.apply(x);
                if (u == null) {
                    return null;
                }
                return txt.of(u);
            };
        }
    }


    protected NText formatPlain(String ss) {
        if (ss == null) {
            return txt.of("");
        }
        List<NText> dd = NStringUtils.parseDollarPlaceHolder(ss)
                .map(t -> {
                    switch (t.ttype) {
                        case NToken.TT_DOLLAR:
                        case NToken.TT_DOLLAR_BRACE: {
                            NText x = mapper.apply(t.sval);
                            if (x == null) {
                                throw new IllegalArgumentException("var not found " + t.sval);
                            }
                            return x;
                        }
                    }
                    return txt.ofPlain(t.sval);
                }).collect(Collectors.toList());
        NTextBuilder sb = NTextBuilder.of();
        sb.appendAll(dd);
        return sb.build();
    }
}
